FILES = $(shell find ./src/* | grep .java)

server:
	@javac -d classes $(FILES)

clean:
	rm -rf classes/*

run:
	java -cp classes ghostlab.MainServer 1337