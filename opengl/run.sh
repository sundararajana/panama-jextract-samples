java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/ Teapot.java $*
