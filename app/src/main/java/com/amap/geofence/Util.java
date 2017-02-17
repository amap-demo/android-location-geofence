package com.amap.geofence;

import com.amap.api.location.DPoint;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ligen on 16/12/15.
 */
public class Util {

    public static List<DPoint> toAMapGeoFenceList(String points) {
        List<DPoint> list = new ArrayList<DPoint>();
        if (points == null || points.equals("")) {
            return list;
        }
        String[] pointArray = points.split(";");
        for (int i = 0; i < pointArray.length; i++) {
            if (pointArray[i] == null || pointArray[i].equals("")) {
                continue;
            }
            String[] point = pointArray[i].split(",");
            try {
                if (point.length != 2) {
                    continue;
                }
                double lat = Double.parseDouble(point[1]);
                double lng = Double.parseDouble(point[0]);
                DPoint dPoint = new DPoint(lat, lng);
                list.add(dPoint);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static List<LatLng> toAMapList(String points) {
        List<LatLng> list = new ArrayList<LatLng>();
        if (points == null || points.equals("")) {
            return list;
        }
        String[] pointArray = points.split(";");

        for (int i = 0; i < pointArray.length; i++) {
            if (pointArray[i] == null || pointArray[i].equals("")) {
                continue;
            }
            String[] point = pointArray[i].split(",");
            try {
                if (point.length != 2) {
                    continue;
                }
                double lat = Double.parseDouble(point[1]);
                double lng = Double.parseDouble(point[0]);
                LatLng latLng = new LatLng(lat, lng);
                list.add(latLng);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
