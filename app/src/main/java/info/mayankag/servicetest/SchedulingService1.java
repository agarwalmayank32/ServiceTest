package info.mayankag.servicetest;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class SchedulingService1 extends IntentService {

    public SchedulingService1() {
        super("SchedulingService");
    }

    public static final int NOTIFICATION_ID = 1;
    public String masg;

    @Override
    protected void onHandleIntent(Intent intent) {

        masg = "Hello Friends";
        sendNotification(masg);

        AlarmReciever1.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, TestActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.regular)
                        .setContentTitle("From Appiva Application")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
