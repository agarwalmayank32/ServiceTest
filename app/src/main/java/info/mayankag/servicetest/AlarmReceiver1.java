package info.mayankag.servicetest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver1 extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, SchedulingService1.class);
        Log.d("Service","Scheduling Done");
        startWakefulService(context, service);
    }

    public void setAlarm(Context context) {

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver1.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Toast.makeText(context,"Email Alarm Started",Toast.LENGTH_SHORT).show();

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5*60*1000, 5*60*1000, alarmIntent);

        ComponentName receiver = new ComponentName(context, BootReciever.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        Toast.makeText(context,"Email Alarm Stopped",Toast.LENGTH_SHORT).show();

        ComponentName receiver = new ComponentName(context, BootReciever.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}