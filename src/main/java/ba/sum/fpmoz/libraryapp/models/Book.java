package ba.sum.fpmoz.libraryapp.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Book {
    public String name;
    public String author;
    public String genre;
    public Long year;
    public String characters;
    public String description;
    public String image;
    public Map<String, Float> ratings;

    public Book() {}

    public Book(String name, String author, String genre, Long year, String characters, String description, String image, HashMap<String, Float> ratings) {
        this.name = name;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.characters = characters;
        this.description = description;
        this.image = image;
        this.ratings = ratings;
    }

    public float getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return 0;
        }
        float sum = 0;
        for (Float rating : ratings.values()) {
            sum += rating;
        }
        return sum / ratings.size();
    }

}

