package com.robi027.gorun;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = SplashScreenActivity.class.getName();
    private ProgressBar pb;
    private Button btRefresh;
    private TextView tvMessage;
    private FusedLocationProviderClient mFusedClient;
    private LocationCallback locationCallback;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private int reqGPS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        pb = findViewById(R.id.pb);
        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText(R.string.loading);
        btRefresh = findViewById(R.id.btRefresh);
        btRefresh.setOnClickListener(onClickListener);

        mFusedClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                getLocation(locationResult.getLastLocation());
            }
        };

        if (Network.networkInfo(this) !=null){
            setUpGPS();
        }else {
            tvMessage.setText(R.string.noConnection);
            btRefresh.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btRefresh:
                    setUpGPS();
                    break;
            }
        }
    };

    private void setUpGPS(){
        if (Network.networkInfo(this) != null){
            if (Network.gpsEnabled(this)){
                reqLocation();
            }else {
                tvMessage.setText(R.string.GpsOff);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.GpsOff);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, reqGPS);
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

    private void getLocation(Location location){
        mFusedClient.removeLocationUpdates(locationCallback);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("lat", location.getLatitude());
        intent.putExtra("lng", location.getLongitude());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == reqGPS){
            if (Network.gpsEnabled(this)){
                tvMessage.setText(R.string.getLocation);
                reqLocation();
            }
        }
    }

    private void reqLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }else {
            mFusedClient.requestLocationUpdates(LocationReq.getLocationRequest(), locationCallback, null);
            tvMessage.setText(R.string.getLocation);
        }
    }
}
