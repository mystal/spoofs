all: client master storage test

out = classes
srcdir = cs5204/fs

client = ${srcdir}/client/SClient.java
masterserver = ${srcdir}/master/MasterLauncher.java
storageserver = ${srcdir}/storage/StorageServer.java
test = ${srcdir}/test/SimpleClient.java

classes:
	mkdir ${out}

client: ${out} ${client}
	javac -d ${out} ${client}

master: ${out} ${masterserver}
	javac -d ${out} ${masterserver}

storage: ${out} ${storageserver}
	javac -d ${out} ${storageserver}
	
test: ${out} ${test}
	javac -d ${out} ${test}

.PHONY: clean
clean:
	rm -rf ${out}
