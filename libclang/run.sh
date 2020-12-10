java -Djava.library.path=${LIBCLANG_HOME}/lib -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    ASTPrinter.java $*
