CFLAGS:= -g -Wall -Weffc++ -std=c++11 -I -I./include

IFLAGS	= -I./usr/include/boost/

LFLAGS	= -L./boost/stage/lib/ -lboost_system -lboost_thread

Objects = bin/connectionHandler.o bin/BBClient.o

all: bin/BBClient

bin/BBClient: $(Objects)
	g++ -o bin/BBClient $(Objects) $(IFLAGS) $(LFLAGS)

bin/connectionHandler.o: src/connectionHandler.cpp include/connectionHandler.h
	g++ $(CFLAGS) -c -o bin/connectionHandler.o src/connectionHandler.cpp

bin/BBClient.o: src/BBClient.cpp
	g++ $(CFLAGS) -c -o bin/BBClient.o src/BBClient.cpp

clean:
	rm -f bin/*