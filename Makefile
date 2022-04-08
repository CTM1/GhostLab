JAVAFILES = $(shell find ./server/src/* | grep .java)

.PHONY: all
all: server client

.PHONY: server
server:
	@javac -d classes $(JAVAFILES)

.PHONY: client
client:
	mkdir -p bin
	@gcc -lncurses -lform -Werror client/*.c -o bin/client

clean:
	@rm -rf classes/*
	@rm bin/client

runserver: server
	@java -cp classes ghostlab.MainServer 1337

runclient: client
	@bin/client
