jextract --source -t org.unix \
  -l /usr/lib/libSystem.dylib \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
   /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/time.h

javac --add-modules jdk.incubator.foreign org/unix/*.java
