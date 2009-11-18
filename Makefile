all: master storage

out = classes
srcdir = cs5204/fs

masterserver = ${srcdir}/master/MasterServer.java
storageserver = ${srcdir}/storage/StorageServer.java

classes:
	mkdir classes

master: ${out} ${masterserver}
	javac -d ${out} ${masterserver}

storage: ${out} ${storageserver}
	javac -d ${out} ${storageserver}

.PHONY: clean
clean:
	rm -r ${out}
