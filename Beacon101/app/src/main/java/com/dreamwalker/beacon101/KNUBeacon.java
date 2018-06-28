package com.dreamwalker.beacon101;

public class KNUBeacon {

    String name;
    String address;
    String uuid;
    String major;
    String minor;
    String distance;

    public KNUBeacon(String name, String address, String uuid, String major, String minor) {
        this.name = name;
        this.address = address;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public KNUBeacon(String name, String address, String uuid, String major, String minor, String distance) {
        this.name = name;
        this.address = address;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.distance = distance;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }
}
