rm -fr bin/*
javac -d bin -cp ../zookeeper-3.4.5.jar:../lib/jline-0.9.94.jar:../lib/log4j-1.2.15.jar:../lib/slf4j-api-1.6.1.jar:../lib/slf4j-log4j12-1.6.1.jar:..:/conf:. -sourcepath src src/zk_sync/and/SyncPrimitive.java
