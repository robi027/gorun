package com.robi027.gorun;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getName();
    private GoogleMap mMap;
    private ArrayList<LatLng> points = new ArrayList<>();
    private Polyline lines;
    private FusedLocationProviderClient mFusedClient;
    private boolean trackingLocation = false;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationCallback locationCallback;
    private Button btTracking, btRunning;
    private ImageButton ibHistory;
    private LatLng point;
    private double lat, lng;
    private BottomSheetBehavior sheetBehavior;
    private Retrofit retrofit = RetrofitClientInstance.getRetrofitInstance();
    private RunAPI api = retrofit.create(RunAPI.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        lat = intent.getDoubleExtra("lat", -7.944551);
        lng = intent.getDoubleExtra("lng", 112.610901);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(onMapReadyCallback);
        btTracking = findViewById(R.id.btTracking);
        btTracking.setText(R.string.trackingOff);
        btTracking.setOnClickListener(onClickListener);
        btRunning = findViewById(R.id.btRunning);
        ibHistory = findViewById(R.id.ibHistory);
        ibHistory.setOnClickListener(onClickListener);

        mFusedClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        View nestedScroll = findViewById(R.id.nsView);
        sheetBehavior = BottomSheetBehavior.from(nestedScroll);
        sheetBehavior.setBottomSheetCallback(bottomSheetCallback);

        Log.d(TAG, "onCreate: " + PrefUtil.getString(this, "id"));
    }

    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Log.d(TAG, "onStateChanged: " + newState);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };



    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btTracking:
                    if (!trackingLocation){
                        startTracking();
                    }else{
                        stopTracking();
                    }
                    break;
                case R.id.ibHistory:
                    break;
            }
        }
    };

    private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)!=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }else {
                mMap = googleMap;
                mMap.setMyLocationEnabled(true);
                LatLng point;

                point = new LatLng(lat, lng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 20));
            }
        }

    };

    private void startTracking(){
        if (Network.networkInfo(this) != null){
            if (Network.gpsEnabled(this)){
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                }else {
                    points.clear();
                    trackingLocation = true;
                    mFusedClient.requestLocationUpdates(LocationReq.getLocationRequest(), locationCallback, null);
                    btTracking.setText(R.string.trackingOn);
                    btRunning.setVisibility(View.VISIBLE);
                }
            }else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.GpsOff);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }else {
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noConnection, Snackbar.LENGTH_LONG).show();
        }
    }

    private void stopTracking(){
        if (trackingLocation){
            trackingLocation = false;
            mFusedClient.removeLocationUpdates(locationCallback);
            if (points.size() > 0){
                addMarker(points.get(0));
                addMarker(points.get(points.size() - 1));

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        detailRun();
                    }
                }, 1000);
            }
            btTracking.setText(R.string.trackingOff);
            btRunning.setVisibility(View.GONE);
        }
    }

    public void onLocationChanged(Location location){
        Toast.makeText(this, location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();

        point = new LatLng(location.getLatitude(), location.getLongitude());
        points.add(point);
        drawing();
    }

    public void drawing(){
        PolylineOptions line = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i<points.size(); i++){
            point = points.get(i);
            line.add(point);
        }

        lines = mMap.addPolyline(line);
    }

    public void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    public void detailRun(){
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    public void postRun(){
        Call call = api.postRun("","","",null,null,null,null);
    }
}
