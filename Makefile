server:
	javac -d classes *.java messages/clientMessages/*.java messages/serverMessages/*.java 

clean:
	rm classes/*

run:
	java -cp classes MainServer 1337