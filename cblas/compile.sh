jextract -C "-D FORCE_OPENBLAS_COMPLEX_STRUCT" \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include \
  -l openblas -t blas /usr/local/opt/openblas/include/cblas.h

