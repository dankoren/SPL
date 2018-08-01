package bgu.spl181.net.Json;

import bgu.spl181.net.DataAccess.MovieUser;

import java.util.concurrent.CopyOnWriteArrayList;

public class JsonUsers {
    protected CopyOnWriteArrayList<MovieUser> users;

    public CopyOnWriteArrayList<MovieUser> getUsers() {
        return users;
    }

    public void setUsers(CopyOnWriteArrayList<MovieUser> users) {
        this.users = users;
    }

}
