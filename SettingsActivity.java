package app.gahomatherapy.agnihotramitra;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    //implements AdapterView.OnItemSelectedListener {
//TextView trise,tset;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsfile);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();


      /*  Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);  */


        Switch hours24 = (Switch) findViewById(R.id.radioButton2);
        Switch wake = (Switch) findViewById(R.id.radioButton);
        Switch meditation = (Switch) findViewById(R.id.button3min);
        Switch countdown = (Switch) findViewById(R.id.switchcountd);
        Switch alarms = (Switch) findViewById(R.id.switchalarm);

        final TextView textViewal = (TextView) findViewById(R.id.textViewalarm);

        countdown.setChecked(sharedPreferences.getBoolean("countdown", true));
        wake.setChecked(sharedPreferences.getBoolean("Wakelock", false));
        hours24.setChecked(sharedPreferences.getBoolean("hours24", false));
        if (sharedPreferences.getInt("alarm", 0) == 0)
            alarms.setChecked(false);
        else {
            alarms.setChecked(true);
            textViewal.setText("Alarm set for : " + sharedPreferences.getInt("alarm", 0) + " minutes before Agnihotra\n " + "next alarm " + sharedPreferences.getString("alarmtime","not set"));
        }

        alarms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showalarmdialog();
                } else {
                    textViewal.setText("Set alarm to ring before agnihotra");
                    editor.putInt("alarm", 0);
                    alarmsetfor(0);
                    editor.commit();
                }
            }
        });


        meditation.setChecked(sharedPreferences.getBoolean("meditation", false));

        countdown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("countdown", isChecked);
                editor.commit();
            }
        });

        wake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("Wakelock", isChecked);
                editor.commit();
            }
        });

        hours24.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("hours24", isChecked);
                editor.commit();
            }
        });


        meditation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("meditation", isChecked);
                editor.commit();
            }
        });


        final TextView tvhom = (TextView) findViewById(R.id.textViewhomeloc);
        int homeloc = sharedPreferences.getInt("homelocation", 1);
        tvhom.setText(sharedPreferences.getString("Location" + homeloc, "Home location not set"));
        Button homebutton = (Button) findViewById(R.id.buttonsethome);
        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
                final List<String> locationsname = new ArrayList<>();

                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
                final CharSequence[] items = locationsname.toArray(new CharSequence[locationsname.size()]);
                alertDialog.setTitle("Set home location");
                alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (locationsname.get(which) == sharedPreferences.getString("Location1", " Location not set ")) {
                            editor.putInt("homelocation", 1);

                        }
                        if (locationsname.get(which) == sharedPreferences.getString("Location2", " Location not set ")) {
                            editor.putInt("homelocation", 2);
                        }
                        if (locationsname.get(which) == sharedPreferences.getString("Location3", " Location not set ")) {
                            editor.putInt("homelocation", 3);

                        }
                        editor.commit();
                        tvhom.setText(locationsname.get(which));
                    }

                });
                alertDialog.create().show();
            }
        });
    }

    private void showalarmdialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.alertdialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Set",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                TextView textViewal = (TextView) findViewById(R.id.textViewalarm);
                                int alarmtime;
                                try {
                                    alarmtime = Integer.parseInt(userInput.getText().toString());
                                }
                                catch (NumberFormatException e) {
                                    Toast.makeText(getApplicationContext(),"Please enter minutes between 3-120",Toast.LENGTH_SHORT);
                                    alarmtime=0;
                                }

                                if (alarmtime > 2 && alarmtime < 121) {
                                    editor.putInt("alarm", alarmtime);
                                    editor.commit();
                                    textViewal.setText("Alarm set for : " + alarmtime + " minutes before Agnihotra ");
                                    alarmsetfor(alarmtime);
                                } else
                                    Toast.makeText(getApplicationContext(), " Please enter minutes between 3-120", Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton("Cancel",

                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                alarmsetfor(0);
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void alarmsetfor(int alarmtime) {
        int homel = sharedPreferences.getInt("homelocation", 1);
        if (!sharedPreferences.getBoolean("Empty" + homel, true)) {

            DBhelper dBhelper = new DBhelper(this, homel);
            Date todaydate = Calendar.getInstance().getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

            String date1 = simpleDateFormat.format(todaydate);
            Entrydate entrydate = dBhelper.getDate(date1);

            if (entrydate != null) {
                Log.d("SUNSET", entrydate.getSunrise() + "  " + entrydate.getSunset());
                simpleDateFormat = new SimpleDateFormat("HH:mm:ss");


                Date sunrised = null,
                        sunsetd = null;

                try {
                    sunrised = simpleDateFormat.parse(entrydate.getSunrise());
                    sunsetd = simpleDateFormat.parse(entrydate.getSunset());

                } catch (Exception e) {
                }
                Calendar c1 = Calendar.getInstance();
                Calendar c2 = Calendar.getInstance();
                if (sunrised != null && sunsetd != null) {
                    c1.setTime(sunrised);
                    c2.setTime(sunsetd);
                    c2.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                    c2.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
                    c2.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE));

                    c1.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                    c1.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
                    c1.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE));

                    long drise = c1.getTimeInMillis();
                    long dset = c2.getTimeInMillis();
                    long currenttime = Calendar.getInstance().getTimeInMillis();
                    // also holds alarm time ...currentime
                    if (currenttime < drise) {
                        currenttime = drise;
                    } else if (currenttime > drise && currenttime < dset) {
                        currenttime = dset;
                    } else {
                        c1.add(Calendar.DATE, 1);
                        currenttime = c1.getTimeInMillis();
                    }


                    AlarmManager alarmManager =
                            (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                    Intent myIntent = new Intent(getApplicationContext(), AlarmReciever.class);
                 //   myIntent.setAction("ga.homatherapy.alarm");

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    if (alarmtime > 0) {
                        currenttime = currenttime - alarmtime * 60000;

                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                        String dateString = formatter.format(new Date(currenttime));

                        Log.d(" Alarm time", " Alarm time is set to " + dateString);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("alarmtime",dateString);
                        editor.commit();

                        if (android.os.Build.VERSION.SDK_INT >=23)
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                    currenttime,
                                    pendingIntent);
                        else if(Build.VERSION.SDK_INT >=19 )
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                                    currenttime,
                                    pendingIntent);
                        else
                            alarmManager.set(AlarmManager.RTC_WAKEUP,
                                    currenttime,
                                    pendingIntent);
             // enabling boot load
                        ComponentName receiver = new ComponentName(getApplicationContext(), Bootreciever.class);
                        PackageManager pm = getApplicationContext().getPackageManager();

                        pm.setComponentEnabledSetting(receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);


                    } else {
                        alarmManager.cancel(pendingIntent);
                        // enabling boot load
                        ComponentName receiver = new ComponentName(getApplicationContext(), Bootreciever.class);
                        PackageManager pm = getApplicationContext().getPackageManager();

                        pm.setComponentEnabledSetting(receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);

                    }

                }
            }
        }
    }
}
/*    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
SharedPreferences sp = SharedPreferences.
        if(!parent.getItemAtPosition(pos).equals(getResources().getConfiguration().locale)) {
            switch (pos) {
                case 1:
                    setLocale("en");
                    editor.putString("Locale","en");
                    editor.commit();

                    break;
                case 2:
                    setLocale("hi");
                    editor.putString("Locale","hi");
                    editor.commit();
                    break;
                // default: setLocale("en");
                // break;
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
    public void setLocale(String lang) {
        String languageToLoad  = lang;
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Intent intent = new Intent(SettingsActivity.this,SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        }

} */
