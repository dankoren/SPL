package bgu.spl181.net.api.bidi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl181.net.DataAccess.User;
import bgu.spl181.net.srv.bidi.ConnectionHandler;



public class ConnectionsImp<T> implements Connections<T> {

    protected ConcurrentHashMap<Integer, ConnectionHandler<T>> connections= new ConcurrentHashMap<Integer,ConnectionHandler<T>>();

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> myClient = connections.get(connectionId);
        try{
            myClient.send(msg);
            return true;}
            catch (Exception ex){
                return false;}
    }

    @Override
    public void broadcast(T msg) {
        for(Map.Entry<Integer,ConnectionHandler<T>> myConnection : connections.entrySet())
            myConnection.getValue().send(msg);

    }

    public void broadcastToLoggedUsers(T msg, Map<Integer,User> loggedUsersByConnections){
        for(Map.Entry<Integer,User> myConnection : loggedUsersByConnections.entrySet())
            connections.get(myConnection.getKey()).send(msg);
    }

    @Override
    public void disconnect(int connectionId) {
        connections.remove(connectionId);

    }

    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getConnections() {
        return connections;
    }


}
