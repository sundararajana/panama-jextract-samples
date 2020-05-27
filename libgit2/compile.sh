jextract -t com.github -lgit2 \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/ \
  -I ${LIBGIT2_HOME}/include/ \
  -I ${LIBGIT2_HOME}/include/git2 \
  ${LIBGIT2_HOME}/include/git2.h
