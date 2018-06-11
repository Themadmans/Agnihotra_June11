package app.gahomatherapy.agnihotramitra;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Bootreciever extends BroadcastReceiver {
SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            setalarmagain(context);
            Log.d("AGNIHOTRA", " BOOT RECIEVED");
        }
    }

    private void setalarmagain(Context context)
    {
        int homel = sharedPreferences.getInt("homelocation", 1);
        int alarmtime = sharedPreferences.getInt("alarm",0);
        if (!sharedPreferences.getBoolean("Empty" + homel, true)) {

            DBhelper dBhelper = new DBhelper(context.getApplicationContext(), homel);
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
                    if (currenttime < drise-alarmtime*60000) {
                        currenttime = drise;
                    } else if (currenttime > drise-alarmtime*60000 && currenttime < dset-alarmtime*60000) {
                        currenttime = dset;
                    } else {
                        c1.add(Calendar.DATE, 1);
                        currenttime = c1.getTimeInMillis();
                    }


                    AlarmManager alarmManager =
                            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                    Intent myIntent = new Intent(context, AlarmReciever.class);
                //    myIntent.setAction("ga.homatherapy.alarm");

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    if (alarmtime > 0) {
                        currenttime = currenttime - alarmtime * 60000;

                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                        String dateString = formatter.format(new Date(currenttime));

                        Log.d(" Alarm time", " Alarm time is set to " + dateString);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("alarmtime",dateString);
                        editor.commit();

                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                                    currenttime,
                                    pendingIntent);
                        else
                            alarmManager.set(AlarmManager.RTC_WAKEUP,
                                    currenttime,
                                    pendingIntent);

                    } else
                        alarmManager.cancel(pendingIntent);

                }
            }
        }
    }



}
