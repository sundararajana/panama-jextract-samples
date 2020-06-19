jextract \
   -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
   -l lapacke -t lapack \
   --filter lapacke.h \
   /usr/local/opt/lapack/include/lapacke.h 
