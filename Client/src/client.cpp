#include <stdlib.h>
#include <boost/thread.hpp>
#include "readingFromSocket.h"


void createLDRWPacket(std::string& nameOfPacket, std::string& name, Packet*& packet, bool& packetInitialized, bool*& isRRQ, bool*& isWRQ, std::string*& RRQFileName, std::string*& WRQFileName){
	if (nameOfPacket.compare("LOGRQ") == 0){
		packet = new logRQPacket(name);
		packetInitialized =true;
	}
	else if (nameOfPacket.compare("DELRQ") == 0){
		packet = new delRQPacket(name);
		packetInitialized = true;
	}
	else if (nameOfPacket.compare("RRQ") == 0){
		packet = new RRQPacket(name);
		packetInitialized  = true;
		(*isRRQ) = true;
		(*RRQFileName) = name;
	}
	else if (nameOfPacket.compare("WRQ") == 0){
		packet = new WRQPacket(name);
		packetInitialized = true;
		(*isWRQ) = true;
		(*WRQFileName) = name;
	}
}

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    //Fields to flag the readerThread
    bool* isRRQ = new bool(false);
	bool* isWRQ = new bool(false);
	bool* isDIRQ = new bool(false);
	bool* terminateThread= new bool(false);
    std::string* RRQFileName = new std::string("");
    std::string* WRQFileName = new std::string("");

    //Start a new thread to run on socketReader
    readingFromSocket socketReader(connectionHandler);
    boost::thread readerThread(&readingFromSocket::run, socketReader, RRQFileName, WRQFileName, isDIRQ, isRRQ, isWRQ, terminateThread);

    while (1) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
		std::string line(buf);
		size_t space = line.find_first_of(' ');
		Packet* packet;
		bool packetInitialized = false;
		if (space != std::string::npos){
			std::string nameOfPacket = line.substr(0,space);// The command to run by name
			std::string name = line.substr(space+1); // The file name or user name

			//Create a Packet (one of: LOGRQ, DELRQ, RRQ, WRQ)
			createLDRWPacket(nameOfPacket, name, packet, packetInitialized, isRRQ, isWRQ, RRQFileName, WRQFileName);
		}
		else if (line.compare("DIRQ") == 0){ // line is a command, if it's DIRQ create packet for it
			packet = new DIRQPacket();
			packetInitialized = true;
			(*isDIRQ) = true;
		}
		else if(line.compare("DISC") == 0)// if line is DISC command, send a DISCPacket and disconnect
		{
			packet = new DISCPacket();
			packet->sendData(connectionHandler);
			(*terminateThread) = true;
			readerThread.join();
			delete packet;
			break;
		}
		if (packetInitialized == true){
			packet->sendData(connectionHandler);
			delete packet;
		}
    }
    //Close all pointers used as flags
    delete isRRQ;
    delete isWRQ;
    delete isDIRQ;
    delete terminateThread;
    delete RRQFileName;
    delete WRQFileName;

    return 0;
}
