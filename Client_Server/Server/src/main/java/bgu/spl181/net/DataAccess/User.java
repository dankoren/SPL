package bgu.spl181.net.DataAccess;

/**
 * Class represents a user from Database
 */
public class User {
    protected String username;
    protected String password;
    protected String type;
    protected transient boolean isLogged=false;

    public User (String username,String password){
        this.password=password;
        this.username=username;
        this.type="normal";
    }

    public String getPassword() {
        return password;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }
}
