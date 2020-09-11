package com.emirhan.mytravelbook;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.emirhan.mytravelbook", MODE_PRIVATE);
                boolean firstTimeCheck = sharedPreferences.getBoolean("isFirstTime",false);
                if(!firstTimeCheck){
                    LatLng userLoc = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc,14));

                    sharedPreferences.edit().putBoolean("isFirstTime",true).apply();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
      if(Build.VERSION.SDK_INT >= 23){
          if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
              requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
          } else {
              locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,50,locationListener);
              mMap.clear();
              Location lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
              if(lastLoc != null){
                  LatLng lastLatLang = new LatLng(lastLoc.getLatitude(),lastLoc.getLongitude());
                  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLang,14));
                  mMap.addMarker(new MarkerOptions().position(lastLatLang).title("You are here"));
              }
          }
      }else{
          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,50,locationListener);
          mMap.clear();
          Location lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
          if(lastLoc != null){
              LatLng lastLatLang = new LatLng(lastLoc.getLatitude(),lastLoc.getLongitude());
              mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLang,14));
              mMap.addMarker(new MarkerOptions().position(lastLatLang).title("You are here"));
          }
      }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0){
            if(requestCode ==1){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,50,locationListener);
                    Location lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(lastLoc != null){
                        LatLng lastLatLang = new LatLng(lastLoc.getLatitude(),lastLoc.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLang,14));
                        mMap.addMarker(new MarkerOptions().position(lastLatLang).title("You are here"));
                    }
                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //Dont forget to add under onMapReady
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList.get(0).getThoroughfare() != null && addressList.size()>0){
                address += addressList.get(0).getThoroughfare();
                if(addressList.get(0).getSubThoroughfare() != null){
                    address += " " + addressList.get(0).getSubThoroughfare();
                }

            }else {
                address = "New Place";
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        Toast.makeText(getApplicationContext(), "New Location is added", Toast.LENGTH_SHORT).show();
        try {
            Double la1 = (latLng.latitude);
            Double la2 = (latLng.longitude);
            String l1 = la1.toString();
            String l2 = la2.toString();

            database = this.openOrCreateDatabase("places", Context.MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, lat VARCHAR, long VARCHAR)");
            String toCompile = "INSERT INTO places (name, lat, long) VALUES (?,?,?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
            sqLiteStatement.bindString(1, address);
            sqLiteStatement.bindString(2, l1);
            sqLiteStatement.bindString(3, l2);
            sqLiteStatement.execute();
            System.out.println(address);
            System.out.println(l1);
            System.out.println(l2);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}