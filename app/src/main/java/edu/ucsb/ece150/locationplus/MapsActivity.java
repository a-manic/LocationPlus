package edu.ucsb.ece150.locationplus;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private static final int RC_HANDLE_INTERNET_PERMISSION = 2;
    private static final int REQUEST_COARSE_LOCATION = 2;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int REQUEST_BACKGROUND_LOCATION = 2;
    private static final String TAG = "MapsActivity";
    private Geofence mGeofence;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mPendingIntent = null;

    private GnssStatus.Callback mGnssStatusCallback;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    private Toolbar mToolbar;
    private Marker mLocationMarker ;
    private Marker mDestinationMarker ;
    private boolean mAutoCentering;
    private float zoomLevel = 16.0f;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAutoCentering = true;

        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up Geofencing Client
        mGeofencingClient = LocationServices.getGeofencingClient(MapsActivity.this);

        // Set up Satellite List
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mGnssStatusCallback = new GnssStatus.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                // [TODO] Implement behavior when the satellite status is updated
                //make obj of class satellite

                int satelliteCount = status.getSatelliteCount();
                float azimuth;
                float elevation;
                float carrierNoiseDensity;
                float carrierFrequencyHz;
                int constellation;
                int svid;


                int usedSatellites = 0;
                float totalSnr = 0;
                for (int i = 0; i < satelliteCount; i++){
                    if (status.usedInFix(i)) {
                        usedSatellites++;


                        azimuth = status.getAzimuthDegrees(i);
                        elevation = status.getElevationDegrees(i);
                        carrierNoiseDensity = status.getCn0DbHz(i);
                        carrierFrequencyHz = status.getCarrierFrequencyHz(i);
                        constellation = status.getConstellationType(i);
                        svid = status.getSvid(i);

                    }
                }


                Log.d(TAG, "Number of satellites : " + satelliteCount );
                Log.d(TAG, "Number of satellites used in Fix: " + usedSatellites );

                for (int i = 0; i < satelliteCount; i++){

                    azimuth = status.getAzimuthDegrees(i);
                    elevation = status.getElevationDegrees(i);
                    carrierNoiseDensity = status.getCn0DbHz(i);
                    carrierFrequencyHz = status.getCarrierFrequencyHz(i);
                    constellation = status.getConstellationType(i);
                    svid = status.getSvid(i);

                    Log.d(TAG, "Azimuth of satellite " + i +":" + azimuth );
                    Log.d(TAG, "Elevation of satellite " + i +":" + elevation );
                    Log.d(TAG, "Carrier Noise Density of satellite " + i +":" + carrierNoiseDensity );
                    Log.d(TAG, "Carrier frequency of satellite " + i +":" + carrierFrequencyHz );
                    Log.d(TAG, "Constellation of satellite " + i +":" + constellation );
                    Log.d(TAG, "SVID of satellite " + i +":" + svid );
                }
            }
        };

        // [TODO] Additional setup for viewing satellite information (lists, adapters, etc.)

        // Set up Toolbar
        mToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        mToolbar.inflateMenu(R.xml.actions);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.viewSatellite:
                        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
                        frameLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.toggleCenter:
                        mAutoCentering = !mAutoCentering;
                        if(mAutoCentering == true){
                            Toast.makeText(getApplicationContext(), "Auto Centering enabled", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Auto Centering disabled", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

                return false;
            }
        });
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // [TODO] Implement behavior when Google Maps is ready
        //mMap.setMyLocationEnabled(true);

        // [TODO] In addition, add a listener for long clicks (which is the starting point for
        // creating a Geofence for the destination and listening for transitions that indicate
        // arrival)

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                final LatLng destinationPosition = latLng;
                final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);

                AlertDialog.Builder adb=new AlertDialog.Builder(MapsActivity.this);
                adb.setTitle("Add Destination");
                adb.setMessage("Do you want to add this as your Destination" );
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {
                        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                                .position(destinationPosition)
                                .title("Destination")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        final Circle circle = mMap.addCircle(new CircleOptions()
                                .center(destinationPosition)
                                .radius(100)
                                .strokeColor(0xffff3333)
                                .fillColor(0x66ff3333)
                        );

                        fab.show();

                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                AlertDialog.Builder adb=new AlertDialog.Builder(MapsActivity.this);
                                adb.setTitle("Select card");
                                adb.setMessage("Are you sure you want to delete this destination" );
                                adb.setNegativeButton("Cancel", null);
                                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                                    @Override

                                    public void onClick(DialogInterface dialog, int which) {
                                        mDestinationMarker.remove();
                                        circle.remove();
                                        fab.hide();
                                    }});
                                adb.show();


                            }
                        });


                    }});
                adb.show();

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        // [TODO] Implement behavior when a location update is received

        LatLng newLocation = new LatLng(location.getLatitude(),location.getLongitude());

        if(mLocationMarker  != null){
            mLocationMarker.setPosition(newLocation);
        }
        else {
            mLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(newLocation)
                    .title("Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        if(mAutoCentering == true){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, zoomLevel));
        }

    }

    /*
     * The following three methods onProviderDisabled(), onProviderEnabled(), and onStatusChanged()
     * do not need to be implemented -- they must be here because this Activity implements
     * LocationListener.
     *
     * You may use them if you need to.
     */
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private GeofencingRequest getGeofenceRequest() {
        // [TODO] Set the initial trigger (i.e. what should be triggered if the user is already
        // inside the Geofence when it is created)

        return new GeofencingRequest.Builder()
                //.setInitialTrigger()  <--  Add triggers here
                .addGeofence(mGeofence)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if(mPendingIntent != null)
            return mPendingIntent;

        Intent intent = new Intent(MapsActivity.this, GeofenceBroadcastReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mPendingIntent;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() throws SecurityException {
        super.onStart();

        // [TODO] Ensure that necessary permissions are granted (look in AndroidManifest.xml to
        // see what permissions are needed for this app)

        /*int internetPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if(internetPermissionGranted != PackageManager.PERMISSION_GRANTED) {
            final String[] permission = new String[] {Manifest.permission.INTERNET};
            ActivityCompat.requestPermissions(this, permission, RC_HANDLE_INTERNET_PERMISSION);
        }*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION);
        }
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        REQUEST_BACKGROUND_LOCATION);
            }
        }*/


        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // [TODO] Data recovery
    }

    @Override
    protected void onPause() {
        super.onPause();

        // [TODO] Data saving
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();

        mLocationManager.removeUpdates(this);
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
    }
}
