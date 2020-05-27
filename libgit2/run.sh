java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=${LIBGIT2_HOME}/build/ \
    GitClone.java $*
