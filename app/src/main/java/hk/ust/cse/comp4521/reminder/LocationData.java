package hk.ust.cse.comp4521.reminder;

/**
 * Created by Krauser on 17/5/2016.
 */
public class LocationData {
    String name;
    double latitude;
    double longitude;

    public LocationData(){}

    public LocationData(String name, double latitude, double longitude){
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }
}
