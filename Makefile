JAVAFILES = $(shell find ./server/src/* | grep .java)

.PHONY: all
all: serv cli

.PHONY: server
server:
	@javac -d classes $(JAVAFILES)

.PHONY: client
client:
	@gcc -lncurses -lform -Werror client/*.c -o bin/client

clean:
	@rm -rf classes/*
	@rm bin/client

runserver:
	@java -cp classes ghostlab.MainServer 1337

runclient:
	@bin/client

crun: server run