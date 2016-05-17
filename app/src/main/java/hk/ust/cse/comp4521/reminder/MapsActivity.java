package hk.ust.cse.comp4521.reminder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, LocationListener, ResultCallback<Status> ,GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private Marker myMarker;
    private Marker targetMarker;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    private int UPDATE_INTERVAL_IN_MILLISECONDS = 200000;   //20 second update once
    private int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 100000;
    private boolean fastLocateCurrent = false;
    private Geocoder geocode;
    List<android.location.Address> addressesList;

    private EditText editLocation;
    private EditText editLatLng;
    private Button searchButton;
    private LocationData locationData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        fastLocateCurrent = false;

        geocode = new Geocoder(getApplicationContext());

        setContentView(R.layout.activity_maps);
        editLocation = (EditText) findViewById(R.id.editLocation);
        editLatLng = (EditText) findViewById(R.id.editLatLng);
        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationText = "";
                try{
                    locationText = editLocation.getText().toString();
                } catch (Exception e){
                    e.printStackTrace();
                }
                if(locationText.equals("")){
                    // do nothing
                }else{
                    // resolve address and get the location data
                    try {
                        addressesList = geocode.getFromLocationName(locationText, 5);
                        String latString = Double.toString(addressesList.get(0).getLatitude());
                        String longString = Double.toString(addressesList.get(0).getLongitude());
                        editLatLng.setText(latString + "," + longString);

                        LatLng latLng = new LatLng(addressesList.get(0).getLatitude(), addressesList.get(0).getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, (float) 16);   // layer 21 means show details(your home) , 2 means show big area(Earth)
                        mMap.animateCamera(cameraUpdate);

                    } catch (Exception f){
                        f.printStackTrace();
                    }
                }
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //myLocation =new LatLng ( mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude() );

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();

        switch (item_id) {
            case R.id.location_save:

                break;

            case R.id.location_cancel:
                finish();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        try {
            addressesList = geocode.getFromLocation(latLng.latitude, latLng.longitude, 5);

            android.location.Address address = addressesList.get(0);
            if(address.getLocality() == null && address.getFeatureName() == null){
                editLocation.setText("Place with no name");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                if(address.getFeatureName() != null) {
                    stringBuilder.append(address.getFeatureName() );
                    if (address.getLocality() != null) {
                        stringBuilder.append( "," + address.getLocality());
                    }
                }

                editLocation.setText(stringBuilder.toString());
            }
            String latString = Double.toString(addressesList.get(0).getLatitude());
            String longString = Double.toString(addressesList.get(0).getLongitude());
            editLatLng.setText(latString + "," + longString);

            if(targetMarker != null){
                targetMarker.remove();
            }
            MarkerOptions markCur = new MarkerOptions().position(latLng).title("Target").icon(BitmapDescriptorFactory.defaultMarker());
            targetMarker = mMap.addMarker(markCur);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }


     // Callback that fires when the location changes.
    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this,"OnLocation Changed", Toast.LENGTH_LONG).show();
        if(fastLocateCurrent == false){
            fastLocateCurrent = true;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, (float) 15);   // layer 21 means show details(your home) , 2 means show big area(Earth)
            mMap.animateCamera(cameraUpdate);

            if (myMarker != null)
                myMarker.remove();

            MarkerOptions markCur = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pink_stick_man))
                    .title("Current_Location");

            myMarker = mMap.addMarker(markCur);
        } else {
            // do nothing, we don't want the camera of map move when user search for location
        }

    }


    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link com.google.android.gms.common.api.ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    @Override
    public void onResult(Status status) {

    }


    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"On connected", Toast.LENGTH_LONG).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            String message = "Last Location is: " +
                    "  Latitude = " + String.valueOf(mLastLocation.getLatitude()) +
                    "  Longitude = " + String.valueOf(mLastLocation.getLongitude());

//            mLocationOutput = message;
            Log.i("Fuck", message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,"No location detected", Toast.LENGTH_LONG).show();
        }

        // Determine whether a Geocoder is available.
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, "No geocoder available", Toast.LENGTH_LONG).show();
            return;
        }
        // It is possible that the user presses the button to get the address before the
        // GoogleApiClient object successfully connects. In such a case, mAddressRequested
        // is set to true, but no attempt is made to fetch the address (see
        // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
        // user has requested an address, since we now have a connection to GoogleApiClient.
//        if (mAddressRequested) {
//            startIntentService(mLastLocation);
//        }

//        if (!mRequestingLocationUpdates) {
//            mRequestingLocationUpdates = true;
            startLocationUpdates();
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }


    protected void createLocationRequest() {
        Toast.makeText(this,"create location request", Toast.LENGTH_LONG).show();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }


    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        fastLocateCurrent = false;
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onRestart() {
        fastLocateCurrent = false;
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Toast.makeText(this,"onResume", Toast.LENGTH_LONG).show();
        fastLocateCurrent = false;
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

     @Override
     protected void onPause() {

        fastLocateCurrent = false;
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        fastLocateCurrent = false;
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        fastLocateCurrent = false;

        super.onDestroy();
    }
}
