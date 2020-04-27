jextract \
   -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include \
   -l lapacke -t lapack \
   --filter lapacke.h \
   /usr/local/opt/lapack/include/lapacke.h 
