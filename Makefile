all: client master storage

out = classes
srcdir = cs5204/fs

client = ${srcdir}/client/Filesystem.java
masterserver = ${srcdir}/master/MasterServer.java
storageserver = ${srcdir}/storage/StorageServer.java

classes:
	mkdir ${out}

client: ${out} ${client}
	javac -d ${out} ${client}

master: ${out} ${masterserver}
	javac -d ${out} ${masterserver}

storage: ${out} ${storageserver}
	javac -d ${out} ${storageserver}

.PHONY: clean
clean:
	rm -r ${out}
