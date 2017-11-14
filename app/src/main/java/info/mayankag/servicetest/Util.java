package info.mayankag.servicetest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import info.mayankag.servicetest.DB.NotificationDBContentProvider;
import info.mayankag.servicetest.DB.NotificationDBContract;

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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
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

    static String getResult(String object, Context context)
    {
        try
        {
            JSONObject res = new JSONObject(object);

            if(!res.has("error"))
            {
                String message="";

                JSONObject entities = new JSONObject(res.getString("entities"));

                String dateTime = getDateTime(entities);

                if(dateTime!=null)
                {
                    String date = dateTime.substring(0,10);
                    String time = dateTime.substring(11,16);

                    String location = getLocation(entities);
                    String duration = getDuration(entities);

                    String event = getEvent(entities);

                    if(event!=null)
                    {
                        message+= event;
                    }

                    message+= "\n"+"Date: "+date;
                    message+= "\n"+"Time: "+time;

                    if(duration!=null)
                    {
                        message+= "\n"+duration;
                    }

                    if(location!=null)
                    {
                        message+= "\n"+location;
                    }

                    String[] columns ={"TITLE", "DATETIME"};
                    Cursor cursor = context.getContentResolver().query(NotificationDBContentProvider.CONTENT_URI, columns, "TITLE=? and DATETIME=?", new String[] { event,  dateTime}, null, null);

                    assert cursor != null;
                    if (!cursor.moveToFirst()) {
                        cursor.close();

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(NotificationDBContract.NOTIFICATION_ENTRY.COLUMN_TITLE,event);
                        contentValues.put(NotificationDBContract.NOTIFICATION_ENTRY.COLUMN_DATETIME,dateTime);
                        context.getContentResolver().insert(NotificationDBContentProvider.CONTENT_URI,contentValues);

                        return message;
                    }
                    else
                    {
                        Log.d("check","Data Already Present in SQLITE");
                    }

                    cursor.close();

                    return null;
                }
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getDateTime(JSONObject entities) throws JSONException {
        if(entities.has("datetime"))
        {
            JSONArray datetime = entities.getJSONArray("datetime");
            JSONObject result = datetime.getJSONObject(0);
            if(result.has("from"))
            {
                JSONObject from = result.getJSONObject("from");
                return from.getString("value");
            }
            else
            {
                return result.getString("value");
            }
        }
        return null;
    }

    private static String getLocation(JSONObject entities) throws JSONException {
        if(entities.has("location"))
        {
            JSONArray location = entities.getJSONArray("location");
            return "Location: "+location.getJSONObject(0).getString("value");
        }
        return null;
    }

    private static String getDuration(JSONObject entities) throws JSONException {
        if(entities.has("duration"))
        {
            JSONArray duration = entities.getJSONArray("duration");
            String value = duration.getJSONObject(0).getString("value");
            String unit = duration.getJSONObject(0).getString("unit");
            return "Duration: "+value+" "+unit;
        }
        return null;
    }

    private static String getEvent(JSONObject entities) throws JSONException
    {
        if(entities.has("message_body") && entities.has("agenda_entry"))
        {
            JSONArray agenda_entry = entities.getJSONArray("agenda_entry");
            String ae = agenda_entry.getJSONObject(0).getString("value");

            JSONArray message_body = entities.getJSONArray("message_body");
            String mb = message_body.getJSONObject(0).getString("value");

            String t = String.valueOf(mb.charAt(0)).toUpperCase();

            return "Title: "+ae+" \nDescription: "+t+mb.substring(1);

        }
        else if(entities.has("message_body"))
        {
            JSONArray message_body = entities.getJSONArray("message_body");
            return "Description: "+message_body.getJSONObject(0).getString("value");
        }
        else if(entities.has("agenda_entry"))
        {
            JSONArray agenda_entry = entities.getJSONArray("agenda_entry");
            return "Title: "+agenda_entry.getJSONObject(0).getString("value");
        }
        return null;
    }
}