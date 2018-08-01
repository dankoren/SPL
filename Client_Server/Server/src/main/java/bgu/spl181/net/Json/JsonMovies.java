package bgu.spl181.net.Json;



import bgu.spl181.net.DataAccess.Movie;

import java.util.concurrent.CopyOnWriteArrayList;

public class JsonMovies {
    protected CopyOnWriteArrayList<Movie> movies;

    public CopyOnWriteArrayList<Movie> getMovies() {
        return movies;
    }

    public void setMovies(CopyOnWriteArrayList<Movie> movies) {
        this.movies = movies;
    }
}
