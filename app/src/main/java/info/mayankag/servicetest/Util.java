package info.mayankag.servicetest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

class Util {
    static String NOTIFICATION_ID = "notification_id";

    static boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    static void sendNotification(String msg, Context context) {
        int notification_id = Integer.parseInt( PreferenceManager.getDefaultSharedPreferences(context)
                .getString(NOTIFICATION_ID, null));

        notification_id++;

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notification_id, new Intent(context, TestActivity.class), 0);

        //noinspection deprecation
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.regular)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.regular))
                .setContentTitle("Event found : Set Event ?")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        if (mNotificationManager != null) {
            mNotificationManager.notify(notification_id, mBuilder.build());
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(NOTIFICATION_ID, String.valueOf(notification_id));
        editor.apply();
    }
}