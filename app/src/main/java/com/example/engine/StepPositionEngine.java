package com.example.engine;

import com.amap.api.maps.model.LatLng;
import com.example.bean.Location;

public class StepPositionEngine {
    private LatLng nowLatLng;

    private Location nowLocation;

    private Float bearing;

    public StepPositionEngine() {}

    private static final int eRadius = 6371000; //地球半径

    public void setNowLocation(Location location){
        this.nowLocation = location;
    }

    public void setNowLatLng(LatLng nowLatLng) {
        this.nowLatLng = nowLatLng;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public Location computeNextStep(float stepSize) {
        Location newLoc = new Location();
        newLoc.setLatitude(nowLocation.getLatitude());
        newLoc.setLongitude(nowLocation.getLongitude());
        float angDistance = stepSize / eRadius;
//        double oldLat = nowLatLng.latitude;
//        double oldLng = nowLatLng.longitude;
        double oldLat = nowLocation.getLatitude();
        double oldLng = nowLocation.getLongitude();

        double newLat = Math.asin( Math.sin(Math.toRadians(oldLat))*Math.cos(angDistance) +
                Math.cos(Math.toRadians(oldLat))*Math.sin(angDistance)*Math.cos(bearing) );

        double newLon = Math.toRadians(oldLng) +
                Math.atan2(Math.sin(bearing)*Math.sin(angDistance)*Math.cos(Math.toRadians(oldLat)),
                        Math.cos(angDistance) - Math.sin(Math.toRadians(oldLat))*Math.sin(newLat));

        newLoc.setLatitude(Math.toDegrees(newLat));
        newLoc.setLongitude(Math.toDegrees(newLon));


        nowLocation = newLoc;
        return newLoc;
    }
}
