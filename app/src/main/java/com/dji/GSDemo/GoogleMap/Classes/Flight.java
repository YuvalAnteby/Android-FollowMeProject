package com.dji.GSDemo.GoogleMap.Classes;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Object of the flight, each time the user makes a new flight their email will automatically be in the flight object as well as the date the flight started (the date of pressing new flight)
 */
public class Flight implements Serializable {
    private String name;
    private String email;
    private int countPictures;
    private String dateStart;
    private String dateEnd;
    private ArrayList<String> links;

    public Flight() {
    }


    public Flight(String name, String email, int countPictures, String dateStart, String dateEnd, ArrayList<String> links) {
        this.name = name;
        this.email = email;
        this.countPictures = countPictures;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.links = links;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCountPictures() {
        return countPictures;
    }

    public void setCountPictures(int countPictures) {
        this.countPictures = countPictures;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public ArrayList<String> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<String> links) {
        this.links = links;
    }


}
