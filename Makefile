JAVAFILES = $(shell find ./server/src/* | grep .java)

.PHONY: all
all: server client

.PHONY: server
server:
	@javac -d classes $(JAVAFILES)

.PHONY: client
client:
	@mkdir -p bin
	@gcc -ggdb3 -Werror client/*.c -o bin/client -lncurses -lform

clean:
	@rm -rf classes/*
	@rm bin/client

runserver: server
	@java -cp classes ghostlab.MainServer 1337

runclient: client
	@bin/client
