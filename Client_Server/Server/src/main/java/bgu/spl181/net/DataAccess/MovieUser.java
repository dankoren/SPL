package bgu.spl181.net.DataAccess;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class represents a user from Database
 */
public class MovieUser extends User {
    protected String country;
    protected CopyOnWriteArrayList<BaseMovie> movies;
    protected long balance;

    public MovieUser(String username, String password, String country, CopyOnWriteArrayList<BaseMovie> movies, int balance) {
        super(username, password);
        this.country = country;
        this.movies = movies;
        this.balance = balance;
    }
    public MovieUser(String username, String password, String country, CopyOnWriteArrayList<BaseMovie> movies, int balance,String type) {
        super(username, password);
        this.country = country;
        this.movies = movies;
        this.balance = balance;
        this.type=type;
    }

    public long getBalance() {
        return balance;
    }

    public String getCountry() {
        return country;
    }

    public CopyOnWriteArrayList<BaseMovie> getMovies() {
        return movies;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
