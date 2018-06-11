package app.gahomatherapy.agnihotramitra;


import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private final PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
    private int currentlocation;
    private boolean SHOWING_FUTURE_DATE = false;
    private boolean COUNTDOWN_ENABLED = false, TIME_ENABLED = true;



    private String tz = null;
    private String tzname = null;

    private ProgressDialog pd;
    private final boolean ENABLE_MENU = true;   // Disabling menu for now ...will in clude in next version
    //private boolean countdownstart = true;   // Making Countdown default

    private final Handler handler = new Handler();
    private Runnable runnable;
    Integer runfromMapsshow=0;
    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss"); // made it global to reduce load on runnable
    private TextView timetv;
    private CountDownTimer timecountdown = null;
  //  private CountDownTimer timecountdown2=null;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // REMOVE BELOW FOR 4.1 above...check duality use

        pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.message_please_wait));
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        setContentView(R.layout.activity_main);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Intent fromShowonMap = getIntent(); // to capture data coming from Manual to Maps to here...OnActivityResult was not working
        if(fromShowonMap!=null && fromShowonMap.getIntExtra("resultCode",0)==420 && sharedPreferences.getBoolean("MAPRUN",false))
        {
            latLng = new LatLng(fromShowonMap.getDoubleExtra("Latitude", 23), fromShowonMap.getDoubleExtra("Longitude", 78));
            adrs=getAddress(latLng.latitude,latLng.longitude);
            if (isNetworkAvailable()) {
                PrepareDB(latLng.latitude, latLng.longitude, adrs, 0);
            } else
                Toast.makeText(this,getResources().getString(R.string.message_no_internet), Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean("MAPRUN",false);
            editor.commit();
        }

        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.title_done), Toast.LENGTH_SHORT).show();
                loadlocation(sharedPreferences.getInt("Location", 0), null);
            }
        });
        TextView tvd = (TextView) findViewById(R.id.textViewdate);
        tvd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getInt("Location", 0) != 0 && !(sharedPreferences.getBoolean("Empty" + currentlocation, true)))
                    showdate();
            }
        });

        currentlocation = sharedPreferences.getInt("homelocation", 0);

        final SharedPreferences.Editor editor = sharedPreferences.edit();


        timetv = (TextView) findViewById(R.id.textViewTime);  // Atomic Clock Display.

        final int i1 = sharedPreferences.getInt("Location", 0);
        if (i1 == 0) {
            TextView tv1 = (TextView) findViewById(R.id.textViewLocation);
            TextView tv4 = (TextView) findViewById(R.id.textViewTZ);
            TextView tv2 = (TextView) findViewById(R.id.textViewSunrise);
            TextView tv3 = (TextView) findViewById(R.id.textViewSunset);
            tv1.setText("Location Not set, Click on Icon !");
            tv2.setVisibility(View.INVISIBLE);
            tv3.setVisibility(View.INVISIBLE);
            tv4.setVisibility(View.INVISIBLE);
        }


        ImageView imageView = (ImageView) findViewById(R.id.locationimg);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {


            @Override
            public boolean onLongClick(View v) {
                if (sharedPreferences.getInt("Location", 0) != 0) {
                    renamelocation(0);
                } else {
                    return true;
                }
                return false;
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // Location Button Listener

                int i = sharedPreferences.getInt("Location", 0);
                if (i == 0) {
                    // TextView tv1 = (TextView) findViewById(R.id.textViewLocation);
                    // TextView tv2 = (TextView) findViewById(R.id.textViewSunrise);
                    //  TextView tv3 = (TextView) findViewById(R.id.textViewSunset);
                    //  tv1.setText("Location Not set, Click on Icon !");
                    //   tv2.setVisibility(View.INVISIBLE);
                    //  tv3.setVisibility(View.INVISIBLE);
                    runforlocation(0);
                } else {
                    Showlocations();
                    //  locationfill();
                }
            }
        });

        if (sharedPreferences.getInt("Run", 0) == 0) {  // First Run Preparation
            editor.putInt("Run", 1);

            //    runforlocation(1);
            editor.commit();
            // Toast.makeText(getApplicationContext(), " Initializing app for first run ... " , Toast.LENGTH_LONG).show();
        } else if (sharedPreferences.getInt("Location", 0) != 0) {

            int homel = sharedPreferences.getInt("homelocation", 1);
            if (!sharedPreferences.getBoolean("Empty" + homel, true)) {
                loadlocation(homel, null);
                checkforupdate(homel);
            }// Checking if data updatation needed for first location...the default one
            else Showlocations();
        }



        timetv = (TextView) findViewById(R.id.textViewTime);



              //  if (sharedPreferences.getBoolean("countdown",true) && sharedPreferences.getInt("Location", 0) != 0)
                //    countdown(1);



    }

    private void countdown(int i) {



        final TextView tvrise = (TextView) findViewById(R.id.textViewSunrise);
        final TextView tvset = (TextView) findViewById(R.id.textViewSunset);

        final String risetime = tvrise.getText().toString();
        final String settime = tvset.getText().toString();
        long timeinmillis = Calendar.getInstance().getTimeInMillis();
        Calendar c1 = Calendar.getInstance();
      //  Log.d("RAHAM", " 1 ");
        SimpleDateFormat simpleDateFormat;

        if (sharedPreferences.getBoolean("hours24", false))
            simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        else simpleDateFormat = new SimpleDateFormat("hh:mm:ss a");


        Date sunrised = null,
                sunsetd = null;

        try {
            sunrised = simpleDateFormat.parse(risetime);
            sunsetd = simpleDateFormat.parse(settime);
//            Log.d("RAHAM", " 3 ");

        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "Unable to start countdown!", Toast.LENGTH_SHORT).show();
        }
        Calendar c2 = Calendar.getInstance();
   if(sunrised!=null && sunsetd!= null) {
       c1.setTime(sunrised);
       c2.setTime(sunsetd);
       c2.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
       c2.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
       c2.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE));

       c1.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
       c1.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
       c1.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE));

       // Set date to today;

       long diffrise = c1.getTimeInMillis();
       long diffset = c2.getTimeInMillis();

       if (i == 1)  // Code for Countdown timer ...
       {
//           countdownstart = true;
          // handler.removeCallbacks(runnable);

           if (timeinmillis < diffrise)  // if current time is past sunrise time
           {
               countdowntimerstart(timeinmillis, c1.getTimeInMillis());

           } else if (timeinmillis < diffset) {  // Show for sunset..
               countdowntimerstart(timeinmillis, c2.getTimeInMillis());
           } else { // Next day...current time past both sunrise and set
               c1.add(Calendar.DATE, 1);
               countdowntimerstart(timeinmillis, c1.getTimeInMillis());
           }
       }
       }
    }


    private void countdowntimerstart(long starttime, long endtime)
    {
        // CountDownTimer timecountdown;
        final      NumberFormat f = new DecimalFormat("00");
        long timer = endtime-starttime;
        timecountdown=        new CountDownTimer(timer, 1000) {

            public void onTick(long millisUntilFinished) {


                long hour = (millisUntilFinished / 3600000) % 24;
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;

                timetv.setText(f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
                //  timetv.setText(TimeUnit.MILLISECONDS.toHours(millisUntilFinished) + ":" + TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)+":"+ TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished));
            }

            public void onFinish() {

                if(sharedPreferences.getBoolean("meditation",false)) {
                final MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this,R.raw.bell);
                mediaPlayer.start(); }

                new CountDownTimer(10000,1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timetv.setText("Swaha !");
                    }

                    @Override
                    public void onFinish() {
                       if(sharedPreferences.getBoolean("countdown",true))
                           countdown(1);
                       else
                        handler.postDelayed(runnable,1000);
                    }
                }.start();

              //  Log.d("RAHAM", "10");
            }
        }.start();


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(ENABLE_MENU) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menusettings, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        if(timecountdown!=null) {
            timecountdown.cancel(); }

    }


    @Override
    protected void onResume() {
        super.onResume();


        if(sharedPreferences.getBoolean("countdown",true) &&  sharedPreferences.getInt("Location", 0) != 0) {
            handler.removeCallbacks(runnable);
            if(!SHOWING_FUTURE_DATE)
            countdown(1); // Start countdown on resume if activated
          //  Log.d("ttta", " COuntdown started...clock stopped ! ");
        }
        else  {
           // Log.d("ttta", " COuntdown started...clock stopped - ULTA ! ");
            if(timecountdown!=null)
            {
             //    Log.d("ttta", " COuntdown  stopped ! ");
                timecountdown.cancel(); }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable = this;
                  timetv.setText(simpleDateFormat1.format(Calendar.getInstance().getTime()));
                    handler.postDelayed(this, 1000);
                }
            }, 1000);

        }
          //  if(sharedPreferences.getBoolean("meditation",false))
          //  countdown(2); // Start countdown to meditation bell
      //  else {
        //    if(timecountdown2!=null)
          //      timecountdown2.cancel();
       // }

        if(sharedPreferences.getInt("Location",0)!=0) {
            TextView tvrise = (TextView) findViewById(R.id.textViewSunrise);
            TextView tvset = (TextView) findViewById(R.id.textViewSunset);


            String risetime = tvrise.getText().toString();
            String settime = tvset.getText().toString();


            timetv = (TextView) findViewById(R.id.textViewTime);
            if (!sharedPreferences.getBoolean("hours24", false)) {

                if(settime.equals(""))
                    simpleDateFormat1 = new SimpleDateFormat("hh:mm:ss a");
                else {
                    simpleDateFormat1 = new SimpleDateFormat("hh:mm:ss a");
                    int hour = Integer.parseInt(settime.substring(0, 2));
                    if (hour > 12) {
                        hour = hour - 12;
                        if (hour < 10)
                            tvset.setText("0" + hour + settime.substring(2) + " PM");
                        else
                            tvset.setText(hour + settime.substring(2) + " PM");

                    }
                    tvrise.setText(risetime.substring(0, 8) + " AM");
                }
            } else {
                if(settime.equals(""))
                    simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss");
                else {
                    simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss");
                    int hour = Integer.parseInt(settime.substring(0, 2));
                    if (hour < 12) {
                        hour = hour + 12;
                        tvset.setText(hour + settime.substring(2, 8));
                    }
                    tvrise.setText(risetime.substring(0, 8));
                } } }


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");

        if (sharedPreferences.getBoolean("Wakelock", false))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Date todaydate;
        todaydate = Calendar.getInstance().getTime();

        final String datetoday = simpleDateFormat.format(todaydate);

        TextView datetv = (TextView) findViewById(R.id.textViewdate);
        if(!SHOWING_FUTURE_DATE)
        datetv.setText(datetoday);


        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        decorView.setSystemUiVisibility(uiOptions);

    }



        private LatLng latLng;
        private String adrs;  // Making them global due to hanlding of 900 request code activity...need to run preparedb twice...

        @Override
        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);
//Log.d("Raju",  "Req code " + requestCode  + " Result code "+ resultCode);
// Same activityesult handles call back for places, for manual ....
            if ((requestCode == 420 && requestCode != 500 && requestCode != 900) && data != null) { //&& resultCode!=0) {
                latLng = new LatLng(data.getDoubleExtra("Latitude", 23), data.getDoubleExtra("Longitude", 78));
                adrs = getAddress(latLng.latitude, latLng.longitude);
                if (isNetworkAvailable()) {
                    PrepareDB(latLng.latitude, latLng.longitude, adrs, 0);
                } else
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            } else if (requestCode == 900 && data != null && resultCode != 0) {  // from Dialog Time Zone
                tz = data.getStringExtra("tz");
                tzname = data.getStringExtra("tzname");
                //  Log.d("Raju", tz);
                if (latLng != null) {
                    PrepareDB(latLng.latitude, latLng.longitude, adrs, 1); //1 for 900 activity run
                }
            } else if (requestCode == 500 && data != null && resultCode != 0) {  // update
                final double lats = data.getDoubleExtra("Latitude", 23);
                final double longs = data.getDoubleExtra("Longitude", 78);
                PrepareDB(lats, longs, adrs, 3); //3 for 500 activity / OLD CELL
            } else if (data != null && resultCode == 210) {
                latLng = new LatLng(data.getDoubleExtra("Latitude", 23), data.getDoubleExtra("Longitude", 78));
                adrs = getAddress(latLng.latitude, latLng.longitude);
                if (isNetworkAvailable()) {
                    PrepareDB(latLng.latitude, latLng.longitude, adrs, 0);
                } else
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Error processing request. Please try again !", Toast.LENGTH_SHORT).show();
                // Log.d("raju", "REquest code issue");
            }
        }

        private boolean isNetworkAvailable () {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        //  public void gettimenow() {  // Using this clock due to issues with regular digital clock..
        //    timetv.setText(simpleDateFormat1.format(Calendar.getInstance().getTime()));
        // }


        public void PrepareDB (Double lats, Double longs, String Address,int runcode){
//runcode 0 for call coming from 840 activity, 1 for DialogTimezone activity run

            final SharedPreferences.Editor editor = sharedPreferences.edit();
            final double lat = lats;
            final double longi = longs;
            final String Address1 = Address;


            Date todaydate = Calendar.getInstance().getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            final String date1 = simpleDateFormat.format(todaydate);
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(simpleDateFormat.parse(date1));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final int loc = sharedPreferences.getInt("Location", 0);
            c.add(Calendar.MONTH, 3);
            final String date2 = simpleDateFormat.format(c.getTime()); // first three months

            c.add(Calendar.MONTH, 3);
            final String date3 = simpleDateFormat.format(c.getTime()); // six months

            c.add(Calendar.MONTH, 3);
            final String date4 = simpleDateFormat.format(c.getTime()); // nine months

            c.add(Calendar.MONTH, 3);
            final String date5 = simpleDateFormat.format(c.getTime()); // nine months
            int locatowrite = 0;
            if (loc < 3) {
                if (sharedPreferences.getBoolean("Empty1", true))
                    locatowrite = 1;
                else if (sharedPreferences.getBoolean("Empty2", true))
                    locatowrite = 2;
                else if (sharedPreferences.getBoolean("Empty3", true))
                    locatowrite = 3;
            }

            final int loc1 = locatowrite;
            if (runcode == 0 || runcode == 3) {

                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setTitle("Please confirm timezone for this location");
                ad.setMessage(TimeZone.getDefault().getDisplayName() + "(" + TimeZone.getDefault().getID() + ")");
                ad.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tz = TimeZone.getDefault().getID();
                        tzname = TimeZone.getDefault().getDisplayName();
                        String querystring1 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date1 + "&end_date=" + date2;
                        String querystring2 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date2 + "&end_date=" + date3;
                        String querystring3 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date3 + "&end_date=" + date4;
                        String querystring4 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date4 + "&end_date=" + date5;

                        //      Log.d("RAJU", querystring4);
                        new QuerytoAPI(MainActivity.this, loc1, pd).execute(querystring1);
                        new QuerytoAPI(MainActivity.this, loc1, pd).execute(querystring2);
                        new QuerytoAPI(MainActivity.this, loc1, pd).execute(querystring3);
                        new QuerytoAPI(MainActivity.this, loc1, pd).execute(querystring4);
                        if (Address1 != " ") {

                            if (tz != null) {
                                if (loc1 == 1) {
                                    editor.putInt("Location", sharedPreferences.getInt("Location", 0) + 1);
                                    editor.putString("Location1", Address1);
                                    editor.putString("Timezone1", tzname);
                                    editor.putLong("Latitude1", Double.doubleToRawLongBits(lat));
                                    editor.putLong("Longitude1", Double.doubleToRawLongBits(longi));
                                    editor.putBoolean("Empty1", false);
                                    //   loadlocation(1);  UNable to load as the Backgound download process not completes before coming here..
                                } else if (loc1 == 2) {
                                    editor.putInt("Location", sharedPreferences.getInt("Location", 0) + 1);
                                    editor.putString("Location2", Address1);
                                    editor.putString("Timezone2", tzname);
                                    editor.putLong("Latitude2", Double.doubleToRawLongBits(lat));
                                    editor.putLong("Longitude2", Double.doubleToRawLongBits(longi));
                                    editor.putBoolean("Empty2", false);
                                    // loadlocation(2);
                                } else if (loc1 == 3) {
                                    editor.putInt("Location", sharedPreferences.getInt("Location", 0) + 1);
                                    editor.putString("Location3", Address1);
                                    editor.putString("Timezone3", tzname);
                                    editor.putLong("Latitude3", Double.doubleToRawLongBits(lat));
                                    editor.putLong("Longitude3", Double.doubleToRawLongBits(longi));
                                    editor.putBoolean("Empty3", false);
                                    // loadlocation(3);
                                }

                                editor.commit();
                            }
                        }

                    }
                });
                ad.setNegativeButton("Change timezone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(MainActivity.this, DialogTimeZone.class);
                        startActivityForResult(intent, 900);

                    }
                });
                ad.create();
                ad.show();
            } else if (runcode == 1 && Address1 != "") {  //Call from Timezone Change
                String querystring1 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date1 + "&end_date=" + date2;
                String querystring2 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date2 + "&end_date=" + date3;
                String querystring3 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date3 + "&end_date=" + date4;
                String querystring4 = "lat_deg=" + lat + "&lon_deg=" + longi + "&timeZoneId=" + tz + "&date=" + date4 + "&end_date=" + date5;

                // Log.d("RAJU", querystring4);
                new QuerytoAPI(MainActivity.this, loc1, pd).execute(querystring1);
                new QuerytoAPI(MainActivity.this, loc1, pd).execute(querystring2);
                new QuerytoAPI(MainActivity.this, loc1, pd).execute(querystring3);
                new QuerytoAPI(MainActivity.this, loc1, pd).execute(querystring4);
                if (tz != null) {
                    if (loc1 == 1) {
                        editor.putInt("Location", sharedPreferences.getInt("Location", 0) + 1);
                        editor.putString("Location1", Address1);
                        editor.putString("Timezone1", tzname);
                        editor.putLong("Latitude1", Double.doubleToRawLongBits(lat));
                        editor.putLong("Longitude1", Double.doubleToRawLongBits(longi));
                        editor.putBoolean("Empty1", false);
                        // loadlocation(1);
                    } else if (loc1 == 2) {
                        editor.putInt("Location", sharedPreferences.getInt("Location", 0) + 1);
                        editor.putString("Location2", Address1);
                        editor.putString("Timezone2", tzname);
                        editor.putLong("Latitude2", Double.doubleToRawLongBits(lat));
                        editor.putLong("Longitude2", Double.doubleToRawLongBits(longi));
                        editor.putBoolean("Empty2", false);
                        //  loadlocation(2);
                    } else if (loc1 == 3) {
                        editor.putInt("Location", sharedPreferences.getInt("Location", 0) + 1);
                        editor.putString("Location3", Address1);
                        editor.putString("Timezone3", tzname);
                        editor.putLong("Latitude3", Double.doubleToRawLongBits(lat));
                        editor.putLong("Longitude3", Double.doubleToRawLongBits(longi));
                        editor.putBoolean("Empty3", false);
                        //  loadlocation(3);
                    }

                    editor.commit();
                }

            } else if (runcode == 2)  // Call from Update Databse
            {
                tz = "Timezone" + currentlocation;
                tz = sharedPreferences.getString(tz, "Asia/Kolkata");
                String Lat = "Latitude" + currentlocation;
                String Longs = "Longitude" + currentlocation;
                double latitude = Double.longBitsToDouble(sharedPreferences.getLong(Lat, 23));
                double longitude = Double.longBitsToDouble(sharedPreferences.getLong(Longs, 78));
                String querystring = "lat_deg=" + latitude + "&lon_deg=" + longitude + "&timeZoneId=" + tz + "&date=" + date1 + "&end_date=" + date2;
                //  Log.d("RAJU", querystring);
                new QuerytoAPI(MainActivity.this, currentlocation, pd).execute(querystring);
            }

        }

        private void runforlocation ( int runcode){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            final CharSequence[] items = {
                    "Use Google Maps", "Enter Manually", "Do it later"  // Changing Places to Maps
            };
            alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 1:
                            Intent intent = new Intent(getApplicationContext(), ManualEntry.class);
                            startActivityForResult(intent, 420);
                            break;
                        case 0:
                            if (isNetworkAvailable()) {
                                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                    Toast.makeText(MainActivity.this, "Please turn on GPS ! ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent intent2 = new Intent(getApplicationContext(), MapsActivity.class);
                                    startActivityForResult(intent2, 429);
                                }
                            } else
                                Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            break;
                    }
                }
            });
            alertDialog.create();
            alertDialog.show();

            if (runcode == 1)
                loadlocation(1, null);
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){


            if (ENABLE_MENU) {
                switch (item.getItemId()) {

                    case R.id.raju1:
                        Intent intent1 = new Intent(this, SettingsActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.raju2:
                        Intent intent2 = new Intent(this, HTCenteres.class);
                        startActivity(intent2);
                        break;
                    case R.id.raju3:
                        AlertDialog.Builder adb = new AlertDialog.Builder(this);
                        adb.setView(R.layout.dialogaboutus);
                        adb.create().show();
                }
            }
            return super.onOptionsItemSelected(item);
        }

        private void loadlocation ( int loc, String datetoload){

            if (!sharedPreferences.getBoolean("Empty" + loc, true)) {

                currentlocation = loc;

                TextView tv1 = (TextView) findViewById(R.id.textViewLocation);
                TextView tv2 = (TextView) findViewById(R.id.textViewSunrise);
                TextView tv5 = (TextView) findViewById(R.id.textViewTZ);
                tv2.setVisibility(View.VISIBLE);
                TextView tv3 = (TextView) findViewById(R.id.textViewSunset);
                TextView tv4 = (TextView) findViewById(R.id.textViewdate);
                tv3.setVisibility(View.VISIBLE);
                tv5.setVisibility(View.VISIBLE);
                Double lo, lt;
                switch (loc) {
                    case 1:
                        tv1.setText(sharedPreferences.getString("Location1", " Location Not Set "));
                        lo = Double.longBitsToDouble(sharedPreferences.getLong("Longitude1", 0));
                        lt = Double.longBitsToDouble(sharedPreferences.getLong("Latitude1", 0));
                        tv5.setText("(" + String.format("%.4f", lo) + "," + String.format("%.4f", lt) + ") " + sharedPreferences.getString("Timezone1", " TimeZone Not Set "));
                        checkforupdate(1);
                        break;
                    case 2:
                        tv1.setText(sharedPreferences.getString("Location2", " Location Not Set "));
                        lo = Double.longBitsToDouble(sharedPreferences.getLong("Longitude2", 0));
                        lt = Double.longBitsToDouble(sharedPreferences.getLong("Latitude2", 0));
                        tv5.setText("(" + String.format("%.4f", lo) + "," + String.format("%.4f", lt) + ") " + sharedPreferences.getString("Timezone2", " TimeZone Not Set "));
                        checkforupdate(2);
                        break;
                    case 3:
                        tv1.setText(sharedPreferences.getString("Location3", " Location Not Set "));
                        lo = Double.longBitsToDouble(sharedPreferences.getLong("Longitude3", 0));
                        lt = Double.longBitsToDouble(sharedPreferences.getLong("Latitude3", 0));
                        tv5.setText("(" + String.format("%.4f", lo) + "," + String.format("%.4f", lt) + ") " + sharedPreferences.getString("Timezone3", " TimeZone Not Set "));
                        checkforupdate(3);
                        break;
                    default:
                        tv1.setText(sharedPreferences.getString("Location1", " Location Not Set, Click on Icon "));
                        tv5.setText(sharedPreferences.getString("Timezone1", " TimeZone Not Set "));
                        break;
                }

                DBhelper dBhelper = new DBhelper(this, loc);
                Date todaydate = Calendar.getInstance().getTime();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMMM yyyy");

                String date1 = simpleDateFormat.format(todaydate);
                if (datetoload == null) {
                    datetoload = date1;
                    tv4.setText(simpleDateFormat2.format(todaydate));
                }


                Entrydate entrydate = dBhelper.getDate(datetoload);


                if (entrydate != null) {
                    tv2.setVisibility(View.VISIBLE);
                    tv3.setVisibility(View.VISIBLE);
                    tv2.setText(entrydate.getSunrise());
                    tv3.setText(entrydate.getSunset());
                    String risetime = tv2.getText().toString();
                    String settime = tv3.getText().toString();

                    if (!sharedPreferences.getBoolean("hours24", false)) {
                        simpleDateFormat1 = new SimpleDateFormat("hh:mm:ss a");
                        int hour = Integer.parseInt(settime.substring(0, 2));
                        if (hour > 12) {
                            hour = hour - 12;
                            if (hour < 10)
                                tv3.setText("0" + hour + settime.substring(2) + " PM");
                            else
                                tv3.setText(hour + settime.substring(2) + " PM");

                        }
                        tv2.setText(risetime.substring(0, 8) + " AM");

                    } else {
                        simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss");
                        int hour = Integer.parseInt(settime.substring(0, 2));
                        if (hour < 12) {
                            hour = hour + 12;
                            tv3.setText(hour + settime.substring(2, 8));
                        }
                        tv2.setText(risetime.substring(0, 8));
                    }

                } else {
                    tv2.setText("");
                    tv3.setText("");
                }
                //  if(countdownstart)
                //    countdown(2);  // Start countdown for meditation on location change
            }
            if (timecountdown != null) {
                timecountdown.cancel();
                countdown(1);
            }

        }

        private void checkforupdate ( int loc){
            Date todaydate = Calendar.getInstance().getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            final String date1 = simpleDateFormat.format(todaydate);
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(simpleDateFormat.parse(date1));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.add(Calendar.DAY_OF_MONTH, 7);  // 7 days from today , to check whether we have data or not...
            final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.updatelayout);
            String d = simpleDateFormat.format(c.getTime());
            final DBhelper dBhelper = new DBhelper(this, loc);
            Entrydate entrydate = dBhelper.getDate(d);
            //  Log.d("Deva", date1 + " " + simpleDateFormat.format(c.getTime()));
            //Log.d("Deva"," " + entrydate + " " + (dBhelper.getDate(date1)) );

            if ((entrydate == null && dBhelper.getDate(date1) != null) && currentlocation != 0) // i.e. today is available 7 days after is not...
            {
                ImageView updatebutton = (ImageView) findViewById(R.id.imageViewUpdate);
                linearLayout.setVisibility(View.VISIBLE);
                updatebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dBhelper.deleteallrecords();  // Cleearing all records first...
                        PrepareDB(0.0, 0.0, "null", 2); // Updating database with existing longi, lats
                        //  Log.d( " ERROR "," do ka  "  + currentlocation);
                        Toast.makeText(getApplicationContext(), "Updating...", Toast.LENGTH_SHORT).show();
                    }
                });

                //  Log.d("Deva", "Here in if");
            } else {
                linearLayout.setVisibility(View.GONE);

                //       Log.d("Deva", "Here in else ");
            }

        }

        private void Showlocations () {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            final List<String> locationsname = new ArrayList<>();

            //   final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            int i = sharedPreferences.getInt("Location", 0);
            if (!sharedPreferences.getBoolean("Empty1", true)) {
                locationsname.add((sharedPreferences.getString("Location1", " Location Not Set ")));
            }
            if (!sharedPreferences.getBoolean("Empty2", true)) {
                locationsname.add((sharedPreferences.getString("Location2", " Location Not Set ")));
            }
            if (!sharedPreferences.getBoolean("Empty3", true)) {
                locationsname.add((sharedPreferences.getString("Location3", " Location Not Set ")));
            }

            int locationbutton = 0, deletebutton = 1;
            if (i == 0) {
                locationsname.add(getResources().getString(R.string.title_add_location));
                locationbutton = 0;
                deletebutton = 1;
            } else if (i == 1) {
                locationsname.add(getResources().getString(R.string.title_add_location));
                locationsname.add(getResources().getString(R.string.title_delete_all_locations));
                locationbutton = 1;
                deletebutton = 2;
            } else if (i == 2) {
                locationsname.add(getResources().getString(R.string.title_add_location));
                locationsname.add(getResources().getString(R.string.title_delete_all_locations));
                locationbutton = 2;
                deletebutton = 3;
            } else if (i == 3) {
                locationsname.add(getResources().getString(R.string.title_delete_all_locations));
                locationbutton = -1; // add location absent
                deletebutton = 3;
            }

            final CharSequence[] items = locationsname.toArray(new CharSequence[locationsname.size()]);
            final int locationbutton1 = locationbutton, deletebutton1 = deletebutton;

//        alertDialog.setTitle(R.string.title_dialog);
            alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //  Log.d("HUMA", which + " deletebutton = " + deletebutton1 + " loca " + locationbutton1);
                    if (which != locationbutton1 && which != deletebutton1) {  // if add location button is not pressed
                        if (locationsname.get(which) == sharedPreferences.getString("Location1", " Location not set ")) {
                            loadlocation(1, null);
                            currentlocation = 1;
                        }
                        if (locationsname.get(which) == sharedPreferences.getString("Location2", " Location not set ")) {
                            loadlocation(2, null);
                            currentlocation = 2;
                        }
                        if (locationsname.get(which) == sharedPreferences.getString("Location3", " Location not set ")) {
                            loadlocation(3, null);
                            currentlocation = 3;
                        }
                    } else if (which == locationbutton1) {    // if add location button is pressed
                        runforlocation(0);
                    } else if (which == deletebutton1) {
                        AlertDialog.Builder alertd = new AlertDialog.Builder(MainActivity.this);
                        alertd.setMessage(getResources().getString(R.string.message_delete_confirm));
                        alertd.setPositiveButton(getResources().getString(R.string.title_proceed), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deletealllocation();
                            }
                        });
                        alertd.setNegativeButton(getResources().getString(R.string.title_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertd.create().show();

                    }

                }
            });
            alertDialog.create();
            alertDialog.show();
        }


        private void deletealllocation () {

            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("Location", 0);
            editor.putBoolean("Empty1", true);  // To check if location db is empty
            editor.putBoolean("Empty2", true);
            editor.putBoolean("Empty3", true);
            editor.putString("Location1", " Location Not Set ");
            editor.putString("Location2", " Location Not Set ");
            editor.putString("Location3", " Location Not Set ");
            editor.putString("Timezone1", "TimeZone Not Set ");
            editor.putString("Timezone2", "TimeZone Not Set ");
            editor.putString("Timezone3", "TimeZone Not Set ");
            editor.commit();

            DBhelper dBhelper = new DBhelper(this, -1);
            dBhelper.deleteall();
            TextView tv1 = (TextView) findViewById(R.id.textViewLocation);
            TextView tv5 = (TextView) findViewById(R.id.textViewTZ);
            TextView tv2 = (TextView) findViewById(R.id.textViewSunrise);
            TextView tv3 = (TextView) findViewById(R.id.textViewSunset);
            tv1.setText("Location Not set, Click on Icon !");
            tv2.setVisibility(View.INVISIBLE);
            tv3.setVisibility(View.INVISIBLE);
            tv5.setVisibility(View.INVISIBLE);
            Toast.makeText(this, getResources().getString(R.string.title_done), Toast.LENGTH_SHORT).show();
        }

        String newname = null;

        private String renamelocation ( int mode){ // Rename and Delete individual location
// mode = 0 for rename and location ....and 1 for asking new name
            //  final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            final int mode1 = mode;


            if (currentlocation != 0) {

                String bt1, bt2;

                final String location = "Location" + currentlocation;

                final EditText inputname = new EditText(MainActivity.this);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Enter new name for location : ");
                alertDialog.setView(inputname);
                inputname.setLayoutParams(lp);
                if (mode == 0) {
                    bt1 = "Rename";
                    bt2 = "Delete";
                } else {
                    bt1 = "Save";
                    bt2 = "Cancel";
                }
                alertDialog.setPositiveButton(bt1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (inputname.getText().toString().length() > 1) {
                            if (mode1 == 0) {
                                editor.putString(location, inputname.getText().toString());
                                editor.commit();
                                TextView tv = (TextView) findViewById(R.id.textViewLocation);
                                tv.setText(inputname.getText().toString());
                            } else newname = inputname.getText().toString();
                            Toast.makeText(getApplicationContext(), "Done ! ", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Please enter at least 2 characters.", Toast.LENGTH_SHORT).show();

                    }
                });
                alertDialog.setNegativeButton(bt2, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mode1 == 0) {
                            AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                            adb.setMessage("Confirm Delete ?");
                            adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DBhelper dBhelper = new DBhelper(MainActivity.this, currentlocation);
                                    dBhelper.deleteallrecords(); // delete record of this location...
                                    editor.putString(location, " Location not set ");
                                    editor.putInt("Location", sharedPreferences.getInt("Location", 0) - 1);
                                    editor.putString("Timezone" + currentlocation, "Timezone not set");
                                    editor.putBoolean("Empty" + currentlocation, true);
                                    editor.commit();
                                    TextView e1 = (TextView) findViewById(R.id.textViewSunrise);
                                    TextView e2 = (TextView) findViewById(R.id.textViewSunset);
                                    TextView e3 = (TextView) findViewById(R.id.textViewLocation);
                                    TextView e4 = (TextView) findViewById(R.id.textViewTZ);

                                    e1.setText("");
                                    e2.setText("");
                                    e4.setText("");
                                    e3.setText("Location not Set ! Please click on Icon ! ");
                                    dBhelper.close();
                                    Toast.makeText(MainActivity.this, "Location Deleted ! ", Toast.LENGTH_SHORT).show();
                                    Showlocations();
                                }
                            });
                            adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            adb.create().show();

                        }
                    }
                });
                alertDialog.create();
                alertDialog.show();
            }
            if (newname != null)
                return newname;
            else return "Home";
        }



    private void showdate() {

        final Calendar myCalendar = Calendar.getInstance();

        final SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
        final String date2 = new DBhelper(this, currentlocation).getlastdate();
        final SimpleDateFormat sd2 = new SimpleDateFormat("dd MMMM yyyy");
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                loadlocation(currentlocation, sd.format(myCalendar.getTime()));
                TextView tv = (TextView) findViewById(R.id.textViewdate);
                tv.setText(sd2.format(myCalendar.getTime()));

                if(monthOfYear>Calendar.getInstance().get(Calendar.MONTH)) // REmoving bug with future date showing old timings...
                {
                    SHOWING_FUTURE_DATE=true;
                    if(timecountdown!=null) {
                        timecountdown.cancel();
                        timetv.setText("");

                    } // Stopping timer for future date
                }
                else if ( dayOfMonth>Calendar.getInstance().get(Calendar.DAY_OF_MONTH) && monthOfYear==Calendar.getInstance().get(Calendar.MONTH))
                {
                    SHOWING_FUTURE_DATE=true;
                    if(timecountdown!=null) {
                        timecountdown.cancel();
                        timetv.setText("");

                    } // Stopping timer for future date

                }
                else if (dayOfMonth==Calendar.getInstance().get(Calendar.DAY_OF_MONTH) && monthOfYear==Calendar.getInstance().get(Calendar.MONTH))
        {                    SHOWING_FUTURE_DATE=false;
        if(timecountdown!=null) {
            timecountdown.cancel();
                    countdown(1);
                }
        }
              }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        Date date3 = null;
        try {
            date3 = sd.parse(date2);
        } catch (Exception e) {
            // Log.d("AGNI", " Date Parsing Error");
        }
        //   Log.d("ÄGNI", "" + date3);
        if (date3 != null) {
            if (date3.getTime() > Calendar.getInstance().getTimeInMillis()) {
                // Log.d("Daty ", "Min " + Calendar.getInstance().getTimeInMillis() + " Max " + date3.getTime() );
                datePickerDialog.getDatePicker().setMaxDate(date3.getTime());
                datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - 1000);
                datePickerDialog.show();
            }
        }
    }

    private String getAddress(double lat, double lng) {
        String addr = "Location Name Not Found !";
        if(lat>-90 && lat<91 && lng >-180 && lng <181 ) {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
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




}

