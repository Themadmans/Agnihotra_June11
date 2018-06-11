package app.gahomatherapy.agnihotramitra;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleMap mMap;
    Double ilng, ilats;
    Boolean intentcall = false;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
        PlaceAutocompleteFragment placeAutoComplete;
    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        checkLocationPermission();
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
            placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    updatemap(place.getLatLng());
                }

                @Override
                public void onError(Status status) {
                    //  Log.d("Maps", "An error occurred: " + status);
                }
            });

        Intent intent=getIntent();
        if(intent!=null) {
            if(intent.getIntExtra("Source",0)==6) {
                ilats = intent.getDoubleExtra("Latitude", 78);
                ilng = intent.getDoubleExtra("Longitude", 23);
                intentcall = true;
            }
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult==null)
                {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
//Log.d("LOCA", "On Location Change Called ");
                    //Place current location marker
                    final Double lati = location.getLatitude();
                    final Double longi = location.getLongitude();
                    LatLng latLng = new LatLng(lati,longi);

                    updatetext(getAddress(lati,longi),longi,lati);
                    final TextView textViewname = (TextView) findViewById(R.id.textViewnamelocation);
                    textViewname.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final EditText inputname = new EditText(MapsActivity.this);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);
                            alertDialog.setTitle("Enter new name for location : ");
                            alertDialog.setView(inputname);
                            inputname.setLayoutParams(lp);
                            alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (inputname.getText().toString().length() > 1) {
                                        newname = inputname.getText().toString();
                                        textViewname.setText(newname);
                                        //     Toast.makeText(getApplicationContext(), "Done ! ", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                        Toast.makeText(MapsActivity.this, "Please enter at least 2 characters.", Toast.LENGTH_SHORT).show();

                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) { }
                            });
                            alertDialog.create().show();
                        }

                    });
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Agnihotra Location");
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location1));
                    markerOptions.draggable(true);
                    mCurrLocationMarker = mMap.addMarker(markerOptions);
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

                    // stop location updates
                    if (mGoogleApiClient != null) {
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                    }


                }
            }
        };


    }


    private void updatemap(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        updatetext(getAddress(latLng.latitude,latLng.longitude),latLng.longitude,latLng.latitude);
        markerOptions.title("Agnihotra Location");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location1));
        markerOptions.draggable(true);
        mCurrLocationMarker = mMap.addMarker(markerOptions);
      //  mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private String getAddress(double lat, double lng) {
        String addr = "Location Name Not Found !";
        if(lat>-90 && lat<91 && lng >-180 && lng <181 ) {
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if(addresses.size()>0) {
                    Address obj = addresses.get(0);
                    String add = obj.getAddressLine(0);
                //    add = add + " " + obj.getCountryName();
                    //          add = add + "\n" + obj.getCountryCode();
                    //  add = add + " " + obj.getAdminArea();
                    //        add = add + "\n" + obj.getPostalCode();
                    //      add = add + "\n" + obj.getSubAdminArea();
                    // add = add + " " + obj.getLocality();
//            add = add + "\n" + obj.getSubThoroughfare();
                    addr = add;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
               // Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return addr;
        }
        else return "Location name not found ! ";
        }

private void updatetext(String loc, Double longs, Double lats) {
    TextView locname = (TextView) findViewById(R.id.textViewnamelocation);
    locname.setText(loc);
    TextView longitude = (TextView) findViewById(R.id.textViewlongi);
    TextView latti = (TextView) findViewById(R.id.textViewLatti);
    longitude.setText(Double.toString(longs));
    latti.setText(Double.toString(lats));
}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        final Button bt = (Button) findViewById(R.id.buttontogglesat);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMap.getMapType()==GoogleMap.MAP_TYPE_HYBRID) {
                    bt.setText("Satellite");
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                else{
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    bt.setText("Map View");
                }

            }
        });

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Button btn = (Button)findViewById(R.id.button2map); //
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                TextView textView1 = (TextView) findViewById(R.id.textViewLatti);
                TextView textView2 = (TextView) findViewById(R.id.textViewlongi);
                TextView textViewname = (TextView) findViewById(R.id.textViewnamelocation);
                Double longi1=230.0, lati1=230.0;

                try {
                longi1 = Double.parseDouble(textView2.getText().toString());
                lati1 = Double.parseDouble(textView1.getText().toString());
                }
                catch(NumberFormatException e)
                {


                    }
                Intent intent = new Intent(MapsActivity.this,MainActivity.class);
                if(longi1>-180 && longi1<180 && lati1>-90 && lati1<90) {
                    intent.putExtra("Longitude", longi1);
                    intent.putExtra("Latitude", lati1);
                    intent.putExtra("resultCode",420);
                    intent.putExtra("LocationName",textViewname.getText().toString());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("MAPRUN",true);
                    editor.commit();
                    startActivity(intent);
                 //   Log.d( "LONTI",longi1 + " " + lati1);
                }
                else
                    Toast.makeText(MapsActivity.this,"Invalid Latitude / Longitude",Toast.LENGTH_SHORT).show();
            }
        });


        if(intentcall) {
            LatLng sydney = new LatLng(ilats,ilng);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(sydney);
            markerOptions.title("Agnihotra Location");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location1));
            markerOptions.draggable(true);
            mCurrLocationMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            updatetext(getAddress(ilats,ilng),ilng,ilats);
        }
     mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {


         @Override
    public void onMapClick(LatLng latLng) {
        final double lng = latLng.longitude;
        final double lat =latLng.latitude;

        updatetext(getAddress(lat,lng),lng,lat);


        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Agnihotra Location");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location1));
        markerOptions.draggable(true);
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

    }
});
mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
    @Override
    public void onMarkerDragStart(Marker marker) {

    }




    @Override
    public void onMarkerDrag(Marker marker) {
        final double lng = marker.getPosition().longitude;
        final double lat =marker.getPosition().latitude;

//        updatetext(getAddress(lat,lng),lng,lat);

        TextView longs = (TextView) findViewById(R.id.textViewlongi);
        TextView latti = (TextView) findViewById(R.id.textViewLatti);

        longs.setText(Double.toString(lng));
        latti.setText(Double.toString(lat));


    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        final double lng = marker.getPosition().longitude;
        final double lat =marker.getPosition().latitude;
        updatetext(getAddress(lat,lng),lng,lat);

    }
});



        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
            else {
        checkLocationPermission();
            }
        } else {
          // Toast.makeText(MapsActivity.this," Please enable location permission. ",Toast.LENGTH_SHORT).show();
            buildGoogleApiClient();
           mMap.setMyLocationEnabled(true);
        }
    }






    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(!intentcall) // Call is not from Intent of Manual entry
        {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this);
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
           //    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            } else {

                Toast.makeText(this, "Please Turn the GPS On !", Toast.LENGTH_SHORT).show();
                checkLocationPermission();
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//checkLocationPermission();

    }

String newname=null;




    @Override
    public void onLocationChanged(Location location) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
              ActivityCompat.requestPermissions(this,
                 new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                      MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                       mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                 //   Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to

        }
    }

}