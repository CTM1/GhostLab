JAVAFILES = $(shell find ./server/src/* | grep .java)

.PHONY: all
all: server client

.PHONY: server
server:
	@javac -d classes $(JAVAFILES)

.PHONY: client
client:
	@mkdir -p bin
	@gcc -ggdb3 -Werror client/*.c -o bin/client -lncurses -lpthread

clean:
	@rm -rf classes/*
	@rm bin/client

runserver: server
	@VERBOSE=true java -cp classes ghostlab.MainServer 1337

runclient: client
	@bin/client 2>/dev/null

runclientverbose: client
	@bin/client 2>client.log

testlab: server
	@java -cp classes ghostlab.LabTest

format: server
	git ls-files -m | grep "java" | xargs google-java-format -i
