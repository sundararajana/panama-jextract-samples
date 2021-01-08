ANACONDA3_HOME=/opt/anaconda3

java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=${ANACONDA3_HOME}/lib \
    PythonMain.java
