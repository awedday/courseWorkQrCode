package com.example.qrcodecoursework;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
public class GPSTracker implements LocationListener {

    private final Context context;
    private Location location;

    public GPSTracker(Context context) {
        this.context = context;
        getLocation();
    }

    private void getLocation() {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Проверка разрешения на использование GPS
            if (locationManager != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else {
                Log.e("GPSTracker", "GPS provider is not available or permission is not granted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canGetLocation() {
        return location != null;
    }

    public double getLatitude() {
        if (location != null) {
            return location.getLatitude();
        }
        return 0.0;
    }

    public double getLongitude() {
        if (location != null) {
            return location.getLongitude();
        }
        return 0.0;
    }

    @Override
    public void onLocationChanged(Location loc) {
        // Обработка изменения местоположения (если необходимо)
    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Обработка включения провайдера (если необходимо)
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Обработка изменения статуса (если необходимо)
    }
}
