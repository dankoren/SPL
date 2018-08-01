package bgu.spl181.net.DataAccess;

import bgu.spl181.net.Json.JsonMovies;
import bgu.spl181.net.Json.JsonUsers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class SharedMoviesData extends SharedData {

    protected ConcurrentHashMap<Long,Movie> movies;

    protected Object moviesJsonLocker;
    protected Object usersJsonLocker;

    public SharedMoviesData(){
        super();
        this.movies = new ConcurrentHashMap<>();
        this.moviesJsonLocker = new Object();
        this.usersJsonLocker = new Object();
    }

    public SharedMoviesData(ConcurrentHashMap<String,User> users,ConcurrentHashMap<Integer,User> loggedUsersByConnections, ConcurrentHashMap<Long, Movie> movies) {
        super(users,loggedUsersByConnections);
        this.movies = movies;
    }

    public void setMovies(ConcurrentHashMap<Long, Movie> movies) {
        this.movies = movies;
    }

    public ConcurrentHashMap<Long, Movie> getMovies() {
        return movies;
    }


    /*****************************************Json Methods*******************************************/
    @Override
    public void writeToJson(){
        writeMoviesToJson();
        writeUsersToJson();
    }

    protected void writeMoviesToJson(){
        synchronized (moviesJsonLocker){
            try (FileWriter writer = new FileWriter("Database/Movies.json")) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                Gson gson = gsonBuilder.disableHtmlEscaping().setPrettyPrinting().create();
                ArrayList<Movie> moviesJson = new ArrayList<>();
                for (Map.Entry<Long, Movie> movie : movies.entrySet()){
                    moviesJson.add(movie.getValue());
                }
                JsonObject tempJsonObj = new JsonObject();
                tempJsonObj.add("movies",gson.toJsonTree(moviesJson));
                gson.toJson(tempJsonObj, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    protected void writeUsersToJson(){
        synchronized (usersJsonLocker) {
            try (FileWriter writer = new FileWriter("Database/Users.json")) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                Gson gson = gsonBuilder.disableHtmlEscaping().setPrettyPrinting().create();
                ArrayList<MovieUser> usersJson = new ArrayList<>();
                for (Map.Entry<String, User> movieUser : users.entrySet()){
                    usersJson.add((MovieUser)movieUser.getValue());
                }
                JsonObject tempJsonObj = new JsonObject();
                tempJsonObj.add("users",gson.toJsonTree(usersJson));
                gson.toJson(tempJsonObj, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void readFromJson(){
        readMovies();
        readMovieUsers();

    }

    public void readMovies(){
        try (FileReader reader = new FileReader("Database/Movies.json")) {
            JsonMovies jsonMovies = new JsonMovies();
            Gson gson = new Gson();
            jsonMovies = gson.fromJson(reader, JsonMovies.class);
            movies = new ConcurrentHashMap<>();
            for (Movie movie: jsonMovies.getMovies()){
                movies.put(movie.getId(),movie);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMovieUsers(){
        try (FileReader reader = new FileReader("Database/Users.json")) {
            JsonUsers jsonUsers = new JsonUsers();
            Gson gson = new Gson();
            jsonUsers = gson.fromJson(reader, JsonUsers.class);
            users = new ConcurrentHashMap<>();
            for (MovieUser user: jsonUsers.getUsers()){
                users.put(user.getUsername(),user);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }




    /*****************************************Requests Methods*******************************************/

    /**
     * send the balance info of the logged user from the connectionId to the client
     * @param connectionId
     * @return response to be sent to the client
     */
    public String requestBalanceInfo(int connectionId){
        long balance = ((MovieUser)loggedUsersByConnections.get(connectionId)).getBalance();
        return "ACK balance " + balance;
    }

    /**
     * parsing splittedMsg, and adds balance to the logged user from the connectionId
     * @param connectionId
     * @param splittedMsg
     * @return response to be sent to the client
     */
    public String requestBalanceAdd(int connectionId,String[] splittedMsg){

        if(splittedMsg.length !=4){
            return "ERROR request balance failed";
        }
        try {
            long amount = Integer.parseInt(splittedMsg[3]);
            long balance = ((MovieUser)loggedUsersByConnections.get(connectionId)).getBalance();
            ((MovieUser)loggedUsersByConnections.get(connectionId)).setBalance(balance + amount);
            return "ACK balance " + (balance+amount) +" added "+ amount;
        }
        catch(NumberFormatException ex){
            return "ERROR request balance failed";
        }
    }

    /**
     * parsing splittedMsg, and send the movie info (from the splittedMsg) to the client
     * @param connectionId
     * @param splittedMsg
     * @return response to be sent to the client
     */
    public String requestMovieInfo(int connectionId,String[] splittedMsg)
    {
        if(splittedMsg.length < 3){
            return "ERROR request info failed";
        }
        String movieName=movieNameFromSplittedMsg(splittedMsg);
        Movie myMovie=findMovieByName(movieName);
        if(myMovie==null)
            return "ERROR request info failed";
        String myName = "\""+myMovie.getName()+"\"";
        long numberOfCopiesLeft = myMovie.getAvailableAmount();
        long price = myMovie.getPrice();
        ArrayList<String> bannedCountries = myMovie.getBannedCountries();
        String bannedCountriesString="";
        for(int j=0;j<bannedCountries.size();j++){
            bannedCountriesString=bannedCountriesString + "\""+bannedCountries.get(j)+"\""+" ";
        }
        if(bannedCountriesString!="")
            bannedCountriesString=bannedCountriesString.substring(0,bannedCountriesString.length()-1);
        return "ACK info "+myName+" "+numberOfCopiesLeft+" "+price+" " + bannedCountriesString;
    }

    /**
     * send all the movies info to the client
     * @return response to be sent to the client
     */
    public String requestMoviesInfo(){
        String moviesStr = "";
        for (Map.Entry<Long, Movie> movie : movies.entrySet()){
            moviesStr = moviesStr + "\"" + movie.getValue().getName() + "\"" + " ";
        }
        if(moviesStr!="")
            moviesStr = moviesStr.substring(0,moviesStr.length() - 1);
        return "ACK info " + moviesStr;

    }

    /**
     * parsing splittedMsg, and trying to rent a movie (from the splittedMsg) to the logged user from connectionId
     * @param connectionId
     * @param splittedMsg
     * @return response to be sent to the client
     */
    public String requestMovieRent(int connectionId,String[] splittedMsg) throws NullPointerException {

        MovieUser user = ((MovieUser) loggedUsersByConnections.get(connectionId));
        String movieName = movieNameFromSplittedMsg(splittedMsg);
        Movie movie = findMovieByName(movieName);
        try {
            synchronized (movie) {
                if (movie == null || // Movie doesn't exist
                        movie.getAvailableAmount() <= 0 || // no available copy to rent
                        movieRentedByUser(user, movie) || //already rented by current user
                        movieBannedInUserCountry(user, movie) ||
                        user.getBalance() < movie.getPrice() // User doesn't have enough money to rent
                        )
                    return "ERROR request rent failed";
                else {
                    user.getMovies().add(movie);
                    user.setBalance(user.getBalance() - movie.getPrice());
                    movie.setAvailableAmount(movie.getAvailableAmount() - 1);
                    return "ACK rent \"" + movieName + "\" success" + "#IMHERETOSPLIT#" + "BROADCAST movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + movie.getPrice();
                }
            }
        }
        catch(NullPointerException ex){
            if(movie==null){ //In case synchronized null (happens when other concurrent thread removes the movie)
                return "ERROR request rent failed";
            }
            else {
                throw ex;
            }
        }

    }

    /**
     * parsing splittedMsg, and trying to return a movie (from the splittedMsg) from the logged user from connectionId
     * @param connectionId
     * @param splittedMsg
     * @return response to be sent to the client
     */
    public String requestMovieReturn(int connectionId, String[] splittedMsg) throws NullPointerException{
        MovieUser user = ((MovieUser)loggedUsersByConnections.get(connectionId));
        String movieName = movieNameFromSplittedMsg(splittedMsg);
        Movie movie = findMovieByName(movieName);
        try {
            synchronized (movie) {
                if (movie == null || // Movie doesn't exist
                        !movieRentedByUser(user, movie))//movie isnt rented by our user
                    return "ERROR request return failed";
                else {
                    user.getMovies().remove(movie);
                    movie.setAvailableAmount(movie.getAvailableAmount() + 1);
                    return "ACK return \"" + movieName + "\" success" + "#IMHERETOSPLIT#" + "BROADCAST movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + movie.getPrice();

                }
            }
        }
        catch(NullPointerException ex){
            if(movie==null){ //In case synchronized null (happens when other concurrent thread removes the movie)
                return "ERROR request rent failed";
            }
            else {
                throw ex;
            }
        }
    }

    /****************************************Admin Requests**********************************************/

    /**
     * parsing splittedMsg, and trying to add a movie (from the splittedMsg) to the movies list
     * @param splittedMsg
     * @return response to be sent to the client
     */
    public String requestAddMovie(String[] splittedMsg){

        String message = mergeArray(splittedMsg);
        String[] newSplittedmessage=message.split("\"");
        String movieName = newSplittedmessage[1];
        int i=3;
        ArrayList<String> bannedCountries=new ArrayList<>();
        while(i<newSplittedmessage.length){
            bannedCountries.add(newSplittedmessage[i]);
            i = i+2;
        }
        String[] splittedAmountPrice = newSplittedmessage[2].split(" ");
        int price = Integer.parseInt(splittedAmountPrice[2]);
        int amount = Integer.parseInt(splittedAmountPrice[1]);
        if(     price<=0 ||
                amount<=0 ||
                findMovieByName(movieName)!=null)
            return "ERROR request addmovie failed";
        Movie newMovie = new Movie(getMaxMovieId(),movieName,price,bannedCountries,amount,amount);
        movies.put(getMaxMovieId(),newMovie);
        return "ACK addmovie \"" + movieName + "\" success" + "#IMHERETOSPLIT#" + "BROADCAST movie \"" + movieName + "\" " + amount + " " + price;
    }


    /**
     * parsing splittedMsg, and trying to removie a movie (from the splittedMsg) from the movies list
     * @param splittedMsg
     * @return response to be sent to the client
     */
    public String requestRemoveMovie(String[] splittedMsg){
        String movieName = movieNameFromSplittedMsg(splittedMsg);
        Movie movie = findMovieByName(movieName);
        try {
            synchronized (movie) {
                if (movie == null ||
                        movieRented(movie)) {
                    return "ERROR request remmovie failed";
                }
                movies.remove(movie.getId());
                return "ACK remmovie \"" + movieName + "\" success" + "#IMHERETOSPLIT#" + "BROADCAST movie \"" + movieName + "\"" + " removed";
            }
        }
        catch(NullPointerException ex){
            if(movie==null){ //In case synchronized null (happens when other concurrent thread removes the movie)
                return "ERROR request rent failed";
            }
            else {
                throw ex;
            }
        }
    }

    /**
     * parsing splittedMsg, and trying to change a price to a movie (from the splittedMsg) from the movies list
     * @param splittedMsg
     * @return response to be sent to the client
     */
    public String requestChangePrice(String[] splittedMsg){
        String message = String.join(" ", splittedMsg);
        String[] newSplittedmessage=message.split("\"");
        String movieName = newSplittedmessage[1];
        int newPrice = Integer.parseInt(newSplittedmessage[2].substring(1));
        Movie movie = findMovieByName(movieName);
        try {
            synchronized (movie) {
                if (movie == null ||
                        newPrice <= 0)
                    return "ERROR request changeprice failed";
                else {
                    movie.setPrice(newPrice);
                    long amount = movie.getAvailableAmount();
                    return "ACK changeprice \"" + movieName + "\" success" + "#IMHERETOSPLIT#" + "BROADCAST movie \"" + movieName + "\" " + amount + " " + newPrice;
                }
            }
        }
        catch(NullPointerException ex){
            if(movie==null){ //In case synchronized null (happens when other concurrent thread removes the movie)
                return "ERROR request rent failed";
            }
            else {
                throw ex;
            }
        }
    }


    /*****************************************Misc Methods*******************************************/


    @Override
    public void addUser(String username, String password, String dataBlock) {
        String country = dataBlock.split("\"")[1];
        MovieUser movieUser = new MovieUser(username,password,country, new CopyOnWriteArrayList<BaseMovie>(),0);
        users.put(movieUser.getUsername(), movieUser);
    }

    @Override
    public boolean isValidDataBlock(String[] splittedMsg) {
        return splittedMsg.length>=4;
    }

    /**
     * merging splittedMsg to a string using the " " delimeter
     * @param splittedMsg
     * @return merged splittedMsg using the " " delimeter
     */
    protected String mergeArray(String[] splittedMsg){
        String message = "";
        for(int i = 0 ; i< splittedMsg.length;i++){
            message+= splittedMsg[i] + " ";
        }
        if(message!= "")
            message = message.substring(0,message.length() - 1);
        return message;
    }

    protected Movie findMovieByName(String name){
        for (Map.Entry<Long, Movie> movie : movies.entrySet())
            if (movie.getValue().getName().equalsIgnoreCase(name))
                return movie.getValue();
        return null;
    }

    /**
     *
     * @param movie
     * @return true if movie is rented by a user in users, else false
     */
    protected boolean movieRented(Movie movie){
        String movieName = movie.getName();
        for (Map.Entry<String, User> user : this.users.entrySet())
            if(movieRentedByUser((MovieUser)user.getValue(),movie))
                return true;
        return false;
    }

    /**
     * @param user
     * @param movie
     * @return true if movie is rented by user, else false
     */
    protected boolean movieRentedByUser(MovieUser user, Movie movie){
        String movieName = movie.getName();
        for(BaseMovie movieEntry: user.getMovies()){
            if(movieEntry.getName().equalsIgnoreCase(movieName))
                return true;
        }
        return false;
    }

    /**
     *
     * @param user
     * @param movie
     * @return true if movie is banned in user's country, else false
     */
    protected boolean movieBannedInUserCountry(User user, Movie movie){
        ArrayList<String> bannedCountries = movie.getBannedCountries();
        String userCountry = ((MovieUser)user).getCountry();
        return bannedCountries.contains(userCountry);

    }

    protected String movieNameFromSplittedMsg(String[] splittedMsg){
        int i =2;
        String movieName="";
        while (i<splittedMsg.length){ // Building the string
            movieName= movieName+splittedMsg[i]+" ";
            i++;
        }
        if(!(movieName.equals("")) && movieName.length()>=2) {
            movieName = movieName.substring(0, movieName.length() - 1); //remove from space end
            if (movieName.charAt(0) == '"' && movieName.charAt(movieName.length() - 1) == '"') { //remove " from start & end
                movieName = movieName.substring(1, movieName.length() - 1);
            }
        }
        return movieName;
    }

    protected Long getMaxMovieId(){
        long maxId = 0;
        for (Map.Entry<Long, Movie> movie : movies.entrySet()){
            if(movie.getKey() > maxId)
                maxId = movie.getKey();
        }
        return maxId;
    }


}

