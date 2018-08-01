package bgu.spl181.net.DataAccess;


/**
 * Class represents a movie (for user) from Database
 */
public class BaseMovie {
    protected long id;
    protected String name;

    public BaseMovie(long id, String name) {
        this.id = id;
        this.name = name;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



}
