package com.vincevscode.cointracker;

public class Coin {
    private int id;
    private String  country;
    private String denomination;
    private int year;

    public Coin(int id, String country, String denomination, int year){
    this.id = id;
    this.country = country;
    this.denomination = denomination;
    this.year = year;
    }

    public int getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public String getDenomination() {
        return denomination;
    }

    public int getYear() {
        return year;
    }

}
