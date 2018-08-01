package bgu.spl181.net.api.bidi;

import bgu.spl181.net.DataAccess.SharedData;
import bgu.spl181.net.DataAccess.SharedMoviesData;

import java.util.Arrays;

/**
 * class represents protocol for the Block Buster server
 * @param <T>
 */
public class MovieRentalProtocol<T> extends BidiMessagingProtocolImp<T> {


    public MovieRentalProtocol(SharedMoviesData sharedData) {
        super(sharedData);
    }

    public void process(T message) {
        if (message instanceof String) {
            String[] splittedMsg = ((String) message).split(" ");
            String command = splittedMsg[0];//TODO : What if msg is empty
            if (command.equals("REGISTER") || command.equals("LOGIN") || command.equals("SIGNOUT")) {
                super.process(message);
            } else if (command.equals("REQUEST")) {
                request(splittedMsg);
            }else{
                connections.send(connectionId,(T)"ERROR: Invalid input!");
            }
            sharedData.writeToJson();
        }
    }

    private void request(String[] splittedMsg) {
        SharedMoviesData sharedMoviesData = (SharedMoviesData)this.sharedData;
        if (splittedMsg.length >= 2) { // input validation
            if (sharedData.getLoggedUsersByConnections().get(connectionId) != null) { // Check if user is logged in by connection id
                    String myUserType = sharedData.getLoggedUsersByConnections().get(connectionId).getType();
                        if (splittedMsg[1].equals("balance") && splittedMsg[2].equals("info"))
                            connections.send(connectionId,(T)sharedMoviesData.requestBalanceInfo(connectionId));
                        else if (splittedMsg[1].equals("balance") && splittedMsg[2].equals("add"))
                            connections.send(connectionId,(T)sharedMoviesData.requestBalanceAdd(connectionId, splittedMsg));
                        else if (splittedMsg[1].equals("info")) {
                            if(splittedMsg.length > 2)
                                connections.send(connectionId,(T)sharedMoviesData.requestMovieInfo(connectionId,splittedMsg));
                            else // splittedMsg.length == 2
                                connections.send(connectionId,(T)sharedMoviesData.requestMoviesInfo());
                        }
                        else if (splittedMsg[1].equals("rent")) {
                            String responseAndBroadcast = sharedMoviesData.requestMovieRent(connectionId,splittedMsg);
                            sendresponseAndBroadCast(responseAndBroadcast);

                        }
                        else if (splittedMsg[1].equals("return")){
                            String responseAndBroadcast = sharedMoviesData.requestMovieReturn(connectionId,splittedMsg);
                            sendresponseAndBroadCast(responseAndBroadcast);
                        }
                        else { // Admin Requests
                            //if(myUserType.equals("Admin")) {
                            if (splittedMsg[1].equals("addmovie")) {
                                if (myUserType.equals("admin")) {
                                    String responseAndBroadCast = sharedMoviesData.requestAddMovie(splittedMsg);
                                    sendresponseAndBroadCast(responseAndBroadCast);
                                }
                                else // User is not admin
                                    connections.send(connectionId, (T) ("ERROR request " + splittedMsg[1] + " failed"));
                            } else if (splittedMsg[1].equals("remmovie")) {
                                if (myUserType.equals("admin")) {
                                    String responseAndBroadCast = sharedMoviesData.requestRemoveMovie(splittedMsg);
                                    sendresponseAndBroadCast(responseAndBroadCast);
                                }
                                else// User is not admin
                                    connections.send(connectionId, (T) ("ERROR request " + splittedMsg[1] + " failed"));
                            } else if (splittedMsg[1].equals("changeprice")) {
                                if (myUserType.equals("admin")) {
                                    String responseAndBroadCast = sharedMoviesData.requestChangePrice(splittedMsg);
                                    sendresponseAndBroadCast(responseAndBroadCast);
                                }
                                else// User is not admin
                                    connections.send(connectionId, (T) ("ERROR request " + splittedMsg[1] + " failed"));
                            }
                        }
                }
            else {
                connections.send(connectionId, (T) ("ERROR request " + splittedMsg[1] + " failed"));
            }
        }
        else{ //Invalid Input
            connections.send(connectionId,(T)"ERROR: Invalid input!");
        }
    }


    /**
     * send the msg to the client and broadcst it if needed
     * @param responseAndBroadcast
     */
    protected void sendresponseAndBroadCast(String responseAndBroadcast){
        connections.send(connectionId,(T)responseAndBroadcast.split("#IMHERETOSPLIT#")[0]);
        if(responseAndBroadcast.charAt(0)=='A')  //if response is successful
            broadcast((T)responseAndBroadcast.split("#IMHERETOSPLIT#")[1]);

    }


}
