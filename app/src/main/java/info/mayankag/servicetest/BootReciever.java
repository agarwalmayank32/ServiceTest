package info.mayankag.servicetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReciever extends BroadcastReceiver {

    private AlarmReceiver1 alarm = new AlarmReceiver1();

    @Override
    public void onReceive(Context context, Intent intent) {
        //noinspection ConstantConditions
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarm.setAlarm(context);
        }
    }
}
