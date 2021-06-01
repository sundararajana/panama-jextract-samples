java --enable-native-access=ALL-UNNAMED \
   --add-modules jdk.incubator.foreign \
   -Djava.library.path=/usr/local/Cellar/ffmpeg/4.3.1_4/lib LibffmpegMain.java $*
