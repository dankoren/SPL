#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <boost/thread.hpp>
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

static bool isConnected = true;

class writerTask { //write to server
private:
    ConnectionHandler *_myCH;
public:
    writerTask(ConnectionHandler *ch) : _myCH(ch) {}

    void operator()() {
        while (isConnected&&!std::cin.eof()) {
           try {
                const short bufsize = 1024;
                char buf[bufsize];
                std::cin.getline(buf, bufsize);
                std::string line(buf);
                if (!_myCH->sendLine(line)) {
                    
                    break;
                }
            }catch (boost::thread_interrupted const &){
                break;
           }
        }
    }
};


class writerReader {//read from server
private:
    ConnectionHandler *_myCH;
public:
    writerReader(ConnectionHandler *ch) : _myCH(ch) {}

    void operator()() {
        while (isConnected) {
            std::string answer;
            if (!_myCH->getLine(answer)) {
                
                break;
            }
            int len = answer.length();
            answer.resize(len - 1);
            std::cout << answer << std::endl << std::endl;
            if (answer == "ACK signout succeeded") {
                
                isConnected = false;
                break;
            }
        }
    }
};


int main(int argc, char *argv[]) {
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
    writerTask writer(&connectionHandler);
    writerReader reader(&connectionHandler);
    boost::thread writerThread(writer);
    boost::thread readerThread(reader);
    readerThread.join();

    return 0;
}

