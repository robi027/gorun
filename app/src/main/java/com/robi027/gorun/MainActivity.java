package com.robi027.gorun;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.robi027.gorun.model.BaseResponse;
import com.robi027.gorun.model.Run;
import com.robi027.gorun.service.RunService;
import com.robi027.gorun.util.LocationReq;
import com.robi027.gorun.util.Network;
import com.robi027.gorun.util.PrefUtil;

import java.security.Timestamp;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private TextView tvTgl, tvJarak, tvDurasi, tvKalori;
    private long timeStart, timeEnd;
    private ProgressBar pbDetail;

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
        tvTgl = findViewById(R.id.tvTgl);
        tvJarak = findViewById(R.id.tvJarak);
        tvDurasi = findViewById(R.id.tvDurasi);
        tvKalori = findViewById(R.id.tvKalori);
        pbDetail = findViewById(R.id.pbDetail);

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
            switch (newState){
                case BottomSheetBehavior.STATE_SETTLING:
                    break;
            }
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
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
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
                    timeStart = System.currentTimeMillis();
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
            timeEnd = System.currentTimeMillis();
            if (points.size() > 0){
                addMarker(points.get(0));
                addMarker(points.get(points.size() - 1));
                post();
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

    public String durasi(long t0, long t1){

        double diff = t1 - t0;
        double diffMinutes = diff / (1000 * 60) ;
        String minutes = String.format("%.2f", diffMinutes);

        return minutes;
    }

    public void post(){

        String device = PrefUtil.getString(this, "id");
        Log.d(TAG, "post: " + device);
        String durasi = durasi(timeStart, timeEnd);
        String lat_start = String.valueOf(points.get(0).latitude);
        String lon_start = String.valueOf(points.get(0).longitude);
        String lat_end = String.valueOf(points.get(points.size() - 1).latitude);
        String lon_end = String.valueOf(points.get(points.size() - 1).longitude);

        RunService runService = new RunService(this);
        runService.create(device, durasi, lat_start, lon_start, lat_end, lon_end, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                pbDetail.setVisibility(View.GONE);
                Run run = (Run) response.body();
                if (run != null){
                    if (!run.isError()){
                        Log.d(TAG, "onResponse: " + run.getRunData());
                        tvDurasi.setText(run.getRunData().getTimer());
                        tvJarak.setText(run.getRunData().getJarak());
                        tvKalori.setText(run.getRunData().getKalori());
                        tvTgl.setText(run.getRunData().getTgl());
                    }else {
                        Toast.makeText(MainActivity.this, R.string.failureMessage, Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, R.string.failureMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(MainActivity.this, R.string.failureMessage, Toast.LENGTH_LONG).show();
            }
        });
    }


}
