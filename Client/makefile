CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread

all: client
	g++ -o bin/TFTPclient bin/connectionHandler.o bin/client.o bin/Packets.o bin/bytesEncDec.o bin/readingFromSocket.o $(LDFLAGS) 

client: bin/connectionHandler.o bin/client.o bin/Packets.o bin/bytesEncDec.o bin/readingFromSocket.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/client.o: src/client.cpp
	g++ $(CFLAGS) -o bin/client.o src/client.cpp

bin/Packets.o: src/Packets.cpp
	g++ $(CFLAGS) -o bin/Packets.o src/Packets.cpp

bin/bytesEncDec.o: src/bytesEncDec.cpp
	g++ $(CFLAGS) -o bin/bytesEncDec.o src/bytesEncDec.cpp

bin/readingFromSocket.o: src/readingFromSocket.cpp
	g++ $(CFLAGS) -o bin/readingFromSocket.o src/readingFromSocket.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
