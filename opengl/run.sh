ln -s /System/Library/Frameworks/GLUT.framework/Versions/Current/GLUT libGLUT.dylib

java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=.:/System/Library/Frameworks/OpenGL.framework/Versions/Current/Libraries/ Teapot.java $*
