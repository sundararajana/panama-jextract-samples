/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import jdk.incubator.foreign.*;
import libffmpeg.AVCodecContext;
import libffmpeg.AVFormatContext;
import libffmpeg.AVFrame;
import libffmpeg.AVPacket;
import libffmpeg.AVStream;
import static libffmpeg.Libffmpeg.*;
import static jdk.incubator.foreign.MemoryAddress.*;
import static jdk.incubator.foreign.CLinker.*;

/*
 * This sample is based on C sample from the ffmpeg tutorial at
 * http://dranger.com/ffmpeg/tutorial01.html
 *
 * This sample extracts first five frames of the video stream
 * from a given .mp4 file and stores those as .ppm image files.
*/
public class LibffmpegMain {
    private static int NUM_FRAMES_TO_CAPTURE = 5;

    static class ExitException extends RuntimeException {
        final int exitCode;
        ExitException(int exitCode, String msg) {
            super(msg);
            this.exitCode = exitCode;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("please pass a .mp4 file");
            System.exit(1);
        }

        av_register_all();

        try (var scope = ResourceScope.newConfinedScope()) {
            // AVFormatContext *ppFormatCtx;
            var ppFormatCtx = MemorySegment.allocateNative(C_POINTER, scope);
            // char* fileName;
            var fileName = toCString(args[0], scope);

            // open video file
            if (avformat_open_input(ppFormatCtx, fileName, NULL, NULL) != 0) {
                System.err.println("cannot open " + args[0]);
                System.exit(1);
            }
            System.out.println("opened " + args[0]);
            // AVFormatContext *pFormatCtx;
            var pFormatCtx = MemoryAccess.getAddress(ppFormatCtx);

            // Retrieve stream info
            if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
                throw new ExitException(1, "Could not find stream information");
            }

            // Dump AV format info on stderr
            av_dump_format(pFormatCtx, 0, fileName, 0);

            // Find the first video stream
            int videoStream = -1;
            // AVFrameContext formatCtx;
            var formatCtx = pFormatCtx.asSegment(AVFormatContext.sizeof(), scope);
            // formatCtx.nb_streams
            int nb_streams = AVFormatContext.nb_streams$get(formatCtx);
            System.out.println("number of streams: " + nb_streams);
            // formatCtx.streams
            var pStreams = AVFormatContext.streams$get(formatCtx);
            var streamsArray = pStreams.asSegment(nb_streams * C_POINTER.byteSize(), scope);

            // AVCodecContext* pVideoCodecCtx;
            var pVideoCodecCtx = NULL;
            // AVCodec* pCodec;
            var pCodec = NULL;
            for (int i = 0; i < nb_streams; i++) {
                // AVStream* pStream;
                var pStream = MemoryAccess.getAddressAtIndex(streamsArray, i);
                // AVStream stream;
                var stream = pStream.asSegment(AVStream.sizeof(), scope);
                // AVCodecContext* pCodecCtx;
                var pCodecCtx = AVStream.codec$get(stream);
                var avcodecCtx = pCodecCtx.asSegment(AVCodecContext.sizeof(), scope);
                if (AVCodecContext.codec_type$get(avcodecCtx) == AVMEDIA_TYPE_VIDEO()) {
                    videoStream = i;
                    pVideoCodecCtx = pCodecCtx;
                    // Find the decoder for the video stream
                    pCodec = avcodec_find_decoder(AVCodecContext.codec_id$get(avcodecCtx));
                    break;
                }
            }

            if (videoStream == -1) {
                throw new ExitException(1, "Didn't find a video stream");
            } else {
                System.out.println("Found video stream (index: " + videoStream + ")");
            }

            if (pCodec.equals(NULL)) {
                throw new ExitException(1, "Unsupported codec");
            }

            // Copy context
            // AVCodecContext *pCodecCtxOrig;
            var pCodecCtxOrig = pVideoCodecCtx;
            // AVCodecContext *pCodecCtx;
            var pCodecCtx = avcodec_alloc_context3(pCodec);
            if (avcodec_copy_context(pCodecCtx, pCodecCtxOrig) != 0) {
                throw new ExitException(1, "Cannot copy context");
            }

            // Open codec
            if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
                throw new ExitException(1, "Cannot open codec");
            }

            // Allocate video frame
            // AVFrame* pFrame;
            var pFrame = av_frame_alloc();
            var frame = pFrame.asSegment(AVFrame.sizeof(), scope);
            // Allocate an AVFrame structure
            // AVFrame* pFrameRGB;
            var pFrameRGB = av_frame_alloc();
            if (pFrameRGB.equals(NULL)) {
                throw new ExitException(1, "Cannot allocate RGB frame");
            }
            var frameRGB = pFrameRGB.asSegment(AVFrame.sizeof(), scope);

            // Determine required buffer size and allocate buffer
            var codecCtx = pCodecCtx.asSegment(AVCodecContext.sizeof(), scope);
            int width = AVCodecContext.width$get(codecCtx);
            int height = AVCodecContext.height$get(codecCtx);
            int numBytes = avpicture_get_size(AV_PIX_FMT_RGB24(), width, height);
            var buffer = av_malloc(numBytes * C_CHAR.byteSize());

            // Assign appropriate parts of buffer to image planes in pFrameRGB
            // Note that pFrameRGB is an AVFrame, but AVFrame is a superset
            // of AVPicture
            avpicture_fill(pFrameRGB, buffer, AV_PIX_FMT_RGB24(), width, height);

            // initialize SWS context for software scaling
            int pix_fmt = AVCodecContext.pix_fmt$get(codecCtx);
            var sws_ctx = sws_getContext(width, height, pix_fmt, width, height,
                AV_PIX_FMT_RGB24(), SWS_BILINEAR(), NULL, NULL, NULL);

            int i = 0;
            // ACPacket packet;
            var packet = AVPacket.allocate(scope);
            // int* pFrameFinished;
            var pFrameFinished = MemorySegment.allocateNative(C_INT, scope);

            while (av_read_frame(pFormatCtx, packet) >= 0) {
                // Is this a packet from the video stream?
                // packet.stream_index == videoStream
                if (AVPacket.stream_index$get(packet) == videoStream) {
 	            // Decode video frame
                    avcodec_decode_video2(pCodecCtx, pFrame, pFrameFinished, packet);

                    int frameFinished = MemoryAccess.getInt(pFrameFinished);
                    // Did we get a video frame?
                    if (frameFinished != 0) {
                        // Convert the image from its native format to RGB
                        sws_scale(sws_ctx, AVFrame.data$slice(frame),
		            AVFrame.linesize$slice(frame), 0, height,
                            AVFrame.data$slice(frameRGB), AVFrame.linesize$slice(frameRGB));
	
                        // Save the frame to disk
                        if (++i <= NUM_FRAMES_TO_CAPTURE) {
                            try {
                                saveFrame(frameRGB, scope, width, height, i);
                            } catch (IOException exp) {
                                exp.printStackTrace();
                                throw new ExitException(1, "file writing failed for frame " + i);
                            }
                        }
                     }
                 }

                 // Free the packet that was allocated by av_read_frame
                 av_free_packet(packet);
            }

            scope.addCloseAction(()-> {
                // clean-up everything

                // Free the RGB image
                if (!buffer.equals(NULL)) {
                    av_free(buffer);
                }

                if (!pFrameRGB.equals(NULL)) {
                    av_free(pFrameRGB);
                }

                // Free the YUV frame
                if (!pFrame.equals(NULL)) {
                    av_free(pFrame);
                }

                // Close the codecs
                if (!pCodecCtx.equals(NULL)) {
                    avcodec_close(pCodecCtx);
                }

                if (!pCodecCtxOrig.equals(NULL)) {
                    avcodec_close(pCodecCtxOrig);
                }

                // Close the video file
                if (!pFormatCtx.equals(NULL)) {
                    avformat_close_input(ppFormatCtx);
                }
            });

            throw new ExitException(0, "Good bye!");
        } catch (ExitException ee) {
            System.err.println(ee.getMessage());
            System.exit(ee.exitCode);
        }
    }

    private static void saveFrame(MemorySegment frameRGB, ResourceScope scope,
            int width, int height, int iFrame)
            throws IOException {
        var header = String.format("P6\n%d %d\n255\n", width, height);
        var path = Paths.get("frame" + iFrame + ".ppm");
        try (var os = Files.newOutputStream(path)) {
            System.out.println("writing " + path.toString());
            os.write(header.getBytes());
            // Write pixel data
            for (int y = 0; y < height; y++) {
                var data = AVFrame.data$slice(frameRGB);
                // frameRGB.data[0]
                var pdata = MemoryAccess.getAddressAtIndex(data, 0);
                // frameRGB.linespace[0]
                var linesize = MemoryAccess.getIntAtIndex(AVFrame.linesize$slice(frameRGB), 0);
                // frameRGB.data[0] + y*frameRGB.linesize[0] is the pointer. And 3*width size of data
                var pixelArray = pdata.addOffset(y*linesize).asSegment(3*width, scope);
                // dump the pixel byte buffer to file
                os.write(pixelArray.toByteArray());
            }
        }
    }
}
