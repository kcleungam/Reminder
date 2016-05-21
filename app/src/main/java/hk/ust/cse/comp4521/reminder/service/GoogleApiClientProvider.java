package hk.ust.cse.comp4521.reminder.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by Jeffrey on 21/5/2016.
 */
public class GoogleApiClientProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "GoogleApiClientProvider";

    private static GoogleApiClientProvider providerInstance;

    private Context application;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<ResultCallback<Status>> listeners;
//    private LocationRequest mLocationRequest;

    private final static int UPDATE_INTERVAL_IN_MILLISECONDS = 10000;   //10 second update once
    private final static int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 50000;

    private GoogleApiClientProvider(){

    }

    public static GoogleApiClient getInstance(Context application, ResultCallback<Status> listener){
        if(providerInstance ==null) {
            providerInstance = new GoogleApiClientProvider();
            providerInstance.buildClient(application);
            providerInstance.application = application;
            providerInstance.listeners = new ArrayList<>();
        }
        providerInstance.listeners.add(listener);
        return providerInstance.mGoogleApiClient;
    }

    private void buildClient(Context application){
        mGoogleApiClient = new GoogleApiClient.Builder(application)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        //TODO: What does mLocaitonReuest do?
//        Log.i(TAG, "create location request");
//        mLocationRequest = LocationRequest.create();
//        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "On connected");
        //TODO: this may need to move back to GUI classes
        if (ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //TODO: What does mLastLocaiton do?
//        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        for(ResultCallback listener:listeners){
            listener.onResult(new Status(1));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

}