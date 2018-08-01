package bgu.spl181.net.api.bidi;

import bgu.spl181.net.DataAccess.SharedData;
import bgu.spl181.net.DataAccess.User;
import bgu.spl181.net.srv.bidi.ConnectionHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class represents a generic protocol for server
 * @param <T>
 */
public  class BidiMessagingProtocolImp<T> implements BidiMessagingProtocol<T> {

    protected Integer connectionId;
    protected Connections<T> connections;
    protected boolean shouldTerminate;
    protected SharedData sharedData;

    protected Object broadcastLocker;

    public BidiMessagingProtocolImp (SharedData sharedData){
        this.sharedData=sharedData;
        this.shouldTerminate=false;
        this.broadcastLocker = new Object();
    }

    @Override
    public void start(int connectionId, Connections connections) {
        this.connectionId=connectionId;
        this.connections=connections;
    }

    @Override
    public void process(T message) {
        if(message instanceof String){
            String[] splittedMsg =((String) message).split(" ");
            String command= splittedMsg[0];//TODO : What if msg is empty
            switch(command){//TODO: Implement all commands
                case "REGISTER":
                    register(splittedMsg);
                    break;
                case "LOGIN":
                    login(splittedMsg);
                    break;
                case "SIGNOUT":
                    signOut();
                        break;

            }


        }
    }

    @Override
    public boolean shouldTerminate() {

        return shouldTerminate;
    }

    public void setShouldTerminate(boolean shouldTerminate) {
        this.shouldTerminate = shouldTerminate;
    }

    public ConnectionHandler myConnectionHandler(){
        return (ConnectionHandler) ((ConnectionsImp)connections).getConnections().get(connectionId);
    }


    /**
     * broadcast msg to all logged in users
     * @param msg - the message that needs to be sended
     */
    public void broadcast(T msg) {
        synchronized (broadcastLocker) {
            ConcurrentHashMap<Integer, User> loggedUsersByConnections = sharedData.getLoggedUsersByConnections();
            for (Map.Entry<Integer, User> entry : loggedUsersByConnections.entrySet()) {
                int curConnectionId = entry.getKey();
                connections.send(curConnectionId, msg);
            }
        }
    }


    /**
     * reigster if possible
     * @param splittedMsg
     * @return an error msg if registration failed , else a success msg
     */
    private void register(String[] splittedMsg){
        String response = sharedData.register(splittedMsg,connectionId);
        if(response.charAt(0) == 'A') // response returned succeeded
        connections.send(connectionId,(T)response);
    }


    private void login(String[] splittedMsg){
        String response = sharedData.login(splittedMsg, connectionId);
        connections.send(connectionId, (T) response);
    }

    private void signOut(){
        String response = sharedData.signOut(connectionId);
        connections.send(connectionId,(T) response);
        if(response.equals("ACK signout succeeded")){
            connections.disconnect(connectionId);
            setShouldTerminate(true);
        }
    }







}
