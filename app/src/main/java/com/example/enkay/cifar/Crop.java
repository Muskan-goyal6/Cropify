package com.example.enkay.cifar;

public class Crop {

    private String cropName;
    private String address;
    private String city;
    private String country;
    private String id;
    private String price;

    public Crop() {
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Crop(String id, String cropName, String address, String city, String country, String price) {
        this.id=id;
        this.cropName = cropName;
        this.address = address;
        this.city = city;
        this.country = country;
        this.price= price;

    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
