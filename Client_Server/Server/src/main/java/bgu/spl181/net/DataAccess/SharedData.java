package bgu.spl181.net.DataAccess;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class represents data for the generic protocol
 */
public class SharedData {
    protected ConcurrentHashMap<String, User> users;
    protected ConcurrentHashMap<Integer, User> loggedUsersByConnections;

    protected Object loginLocker = new Object();
    protected Object registerLocker = new Object();


    public SharedData(){
        this.users = new ConcurrentHashMap<>();
        this.loggedUsersByConnections = new ConcurrentHashMap<>();
    }


    public SharedData(ConcurrentHashMap<String, User> users, ConcurrentHashMap<Integer, User> loggedUsersByConnections) {
        this.users = users;
        this.loggedUsersByConnections = loggedUsersByConnections;
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    public void setLoggedUsersByConnections(ConcurrentHashMap<Integer, User> loggedUsersByConnections) {
        this.loggedUsersByConnections = loggedUsersByConnections;
    }

    public void setUsers(ConcurrentHashMap<String, User> users) {
        this.users = users;
    }

    public ConcurrentHashMap<Integer, User> getLoggedUsersByConnections() {
        return loggedUsersByConnections;
    }

    /**
     *
     * adds user to users,if possible.
     * @param username
     * @param password
     * @param dataBlock
     * @return true if added successfully, else false
     */
    public void addUser(String username, String password, String dataBlock) {
        User user = new User(username,password);
        users.put(user.getUsername(), user);
    }

    /**
     *
     * @param userName
     * @return true if userName is logged in, else false
     */
    public boolean isUserLoggedIn(String userName) {
        for (Map.Entry<Integer, User> loggedUser : loggedUsersByConnections.entrySet())
            if (loggedUser.getValue().getUsername().equalsIgnoreCase(userName))
                return true;
        return false;
    }

    /**
     *
     * @param username
     * @param password
     * @return true if username and password match, else false
     */
    public boolean areUsernamePasswordMatch (String username,String password){
        if(users.get(username)==null)
            return false;
        else {
            String requestedPassword = users.get(username).getPassword();
            if (requestedPassword.equalsIgnoreCase(password))
                return true;
            else
                return false;
        }
    }

    /**
     * parsing splittedMsg, and trying to register a user with the given parameters from splittedMsg
     * @param splittedMsg
     * @param connectionId
     * @return response to be sent to the client
     */
    public String register(String[] splittedMsg, int connectionId) {
        synchronized (registerLocker) {
            if (splittedMsg.length < 3) {
                return "ERROR registration failed";
            }
            String userName = splittedMsg[1];
            String password = splittedMsg[2];
            User user = new User(userName, password);
            if (loggedUsersByConnections.get(connectionId) != null ||//The client performing the register call is already logged in
                    getUsers().get(userName) != null || // username already registered
                    !isValidDataBlock(splittedMsg)) //Data block is not Valid
                return "ERROR registration failed";
            else {
                String dataBlock = "";
                int i = 3;
                while (i < splittedMsg.length) {
                    dataBlock = dataBlock + splittedMsg[i] + " ";
                    i++;
                }
                if (dataBlock != "")
                    dataBlock = dataBlock.substring(0, dataBlock.length() - 1);
                addUser(userName, password, dataBlock);
                return "ACK registration succeeded";
            }
        }
    }


    /**
     * parsing splittedMsg, and trying to login to a user with the given parameters from splittedMsg
     * @param splittedMsg
     * @param connectionId
     * @return response to be sent to the client
     */
    public String login(String[] splittedMsg,int connectionId){
        synchronized (loginLocker) {
            if (splittedMsg.length != 3) {
                return "ERROR login failed";
            }
            String userName = splittedMsg[1];
            String password = splittedMsg[2];

            if (getLoggedUsersByConnections().get(connectionId) != null || //Client already logged in
                    isUserLoggedIn(userName) ||//user already logged in
                    (!(areUsernamePasswordMatch(userName, password))))// username and password arent matching   )
                return "ERROR login failed";
            else { //Adds the user & connection to the loggedUsers list
                User user = users.get(userName);
                loggedUsersByConnections.put(connectionId, user);//our client is now logged to user
                return "ACK login succeeded";
            }
        }
    }

    /**
     * trying to sign out the logged user from connectionId
     * @param connectionId
     * @return response to be sent to the client
     */
    public String signOut(int connectionId){
        if(loggedUsersByConnections.get(connectionId)==null) // if client wasnt logged in
            return "ERROR signout failed"; // return error
        else{
            loggedUsersByConnections.remove(connectionId); // 'logut'
            return "ACK signout succeeded";
        }
    }


    public boolean isValidDataBlock(String[] splittedMsg) {
        return true;
    }

    public void writeToJson(){

    }

    public void readFromJson(){

    }



}
