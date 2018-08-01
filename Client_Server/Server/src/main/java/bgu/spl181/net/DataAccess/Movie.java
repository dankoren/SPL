package bgu.spl181.net.DataAccess;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


/**
 * Class represents a movie from Database
 */
public class Movie extends BaseMovie {

    protected long price;
    protected ArrayList<String> bannedCountries;
    protected long availableAmount;
    protected long totalAmount;

    public Movie(long id, String name,int price, ArrayList<String> bannedCountries, int availableAmount, int totalAmount) {
        super(id,name);
        this.availableAmount = availableAmount;
        this.price = price;
        this.bannedCountries = bannedCountries;
        this.totalAmount = totalAmount;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public ArrayList<String> getBannedCountries() {
        return bannedCountries;
    }

    public long getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(long availableAmount) {
        this.availableAmount = availableAmount;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }
}
