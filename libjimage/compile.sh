jextract \
  -l jimage \
  -J-Duse.foreign.jrtfs=true \
  -J-Dforeign.jrtfs.debug=true \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
  -t org.openjdk \
  jimage.h
    
