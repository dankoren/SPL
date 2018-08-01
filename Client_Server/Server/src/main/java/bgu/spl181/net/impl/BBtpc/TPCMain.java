package bgu.spl181.net.impl.BBtpc;

import bgu.spl181.net.DataAccess.SharedMoviesData;
import bgu.spl181.net.api.MessageEncoderDecoderImp;
import bgu.spl181.net.api.bidi.MovieRentalProtocol;
import bgu.spl181.net.srv.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TPCMain {
    public static void main(String[] args){

        try {
            System.out.println(InetAddress.getLocalHost());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        SharedMoviesData sharedMoviesData = new SharedMoviesData();
        sharedMoviesData.readFromJson();
        Server myServer = Server.threadPerClient(
                Integer.decode(args[0]).intValue(),
                ()-> new MovieRentalProtocol<>(sharedMoviesData),
                ()->new MessageEncoderDecoderImp<>()
                );
        myServer.serve();
    }
}
