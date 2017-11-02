package info.mayankag.servicetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReciever1 extends BroadcastReceiver {

    AlarmReciever1 alarm = new AlarmReciever1();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}
