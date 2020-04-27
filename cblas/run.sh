java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/usr/local/opt/openblas/lib \
    TestBlas.java
