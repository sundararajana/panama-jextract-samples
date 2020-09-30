jextract --source \
  -J-Duse.foreign.jrtfs=true \
  -J-Dforeign.jrtfs.debug=true \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
  -t org.openjdk \
  --filter jimage.h \
  jimage.h

javac --add-modules jdk.incubator.foreign org/openjdk/*.java
    
