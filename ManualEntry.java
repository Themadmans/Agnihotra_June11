package app.gahomatherapy.agnihotramitra;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class ManualEntry extends AppCompatActivity    {
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);
        Button button2 = (Button) findViewById(R.id.buttonmanual);
        Button button3 = (Button) findViewById(R.id.buttonseeonmap);

        final EditText tv1 = (EditText) findViewById(R.id.editTextLat); // Latitude
        final EditText tv2 = (EditText) findViewById(R.id.EditTextLong);  // Longitude
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        button3.setOnClickListener(new View.OnClickListener() {  // SEe on Map
            @Override
            public void onClick(View v) {
                double lats,longs;
                Intent intent = new Intent(ManualEntry.this,MapsActivity.class);
                final String tvtext1= tv1.getText().toString();
                final String tvtext2= tv2.getText().toString();
                try {
                    lats = Double.parseDouble(tvtext1);
                    longs = Double.parseDouble(tvtext2);
                    if(lats>-91 && lats<91 && longs > -181 && longs < 181 ) {
                        intent.putExtra("Latitude", lats);
                        intent.putExtra("Longitude", longs);
                        intent.putExtra("Source",6);
                        startActivity(intent);
                        //    Log.d(" Raju ", lats + " " + longs);
                        finish();
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Invalid Latitude or Longitude ", Toast.LENGTH_SHORT).show();
                }
                catch (NumberFormatException e)
                {
                    Toast.makeText(getApplicationContext(),"Invalid Latitude or Longitude ", Toast.LENGTH_SHORT).show();
                    // Log.d( " Raju ", " FORMAT ERROR ");
                }

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lats,longs;
                Intent intent = new Intent();
                final String tvtext1= tv1.getText().toString();
                final String tvtext2= tv2.getText().toString();
                try {
                    lats = Double.parseDouble(tvtext1);
                    longs = Double.parseDouble(tvtext2);
                    if(lats>-91 && lats<91 && longs > -181 && longs < 181 ) {
                        intent.putExtra("Latitude", lats);
                        intent.putExtra("Longitude", longs);
                        setResult(210, intent); // for direct manual generation...
                    //    Log.d(" Raju ", lats + " " + longs);
                        finish();
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Invalid Latitude or Longitude ", Toast.LENGTH_SHORT).show();
                }
                catch (NumberFormatException e)
                {
                    Toast.makeText(getApplicationContext(),"Invalid Latitude or Longitude ", Toast.LENGTH_SHORT).show();
                   // Log.d( " Raju ", " FORMAT ERROR ");
                }
            }
        });

        ImageButton btnloc = (ImageButton) findViewById(R.id.buttonfindlocation);
        btnloc.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                                          if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                                              Toast.makeText(ManualEntry.this, "Please turn on GPS ! ", Toast.LENGTH_SHORT).show();
                                          }
                                          else {
                                              int permissionCheck = ContextCompat.checkSelfPermission(ManualEntry.this,
                                                  Manifest.permission.ACCESS_FINE_LOCATION);
                                          if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                              mFusedLocationClient.getLastLocation()
                                                      .addOnSuccessListener(ManualEntry.this, new OnSuccessListener<Location>() {
                                                          @Override
                                                          public void onSuccess(Location location) {
                                                              // Got last known location. In some rare situations this can be null.
                                                              if (location != null) {
                                                                updatelocation(location);
                                                              }
                                                              else {
                                                               Toast.makeText(ManualEntry.this," Unable to get location. Please try again. ! ",Toast.LENGTH_SHORT).show();
                                                              }

                                                          }
                                                      });
                                          }}
                                      }
                                  });
    }

     private void updatelocation(Location location) {
        if (location != null) {  // First Try GPS ...
            double longitude, latitude;
            longitude = location.getLongitude();
            latitude = location.getLatitude();


            final EditText tv1 = (EditText) findViewById(R.id.editTextLat); // Latitude
            final EditText tv2 = (EditText) findViewById(R.id.EditTextLong);  // Longitude

            tv1.setText(String.valueOf(latitude));
            tv2.setText(String.valueOf(longitude));
        }
        else Toast.makeText(this,"Unable to get location ! ", Toast.LENGTH_SHORT).show();
    }
    }

