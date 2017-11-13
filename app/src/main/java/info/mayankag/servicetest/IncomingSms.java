package info.mayankag.servicetest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IncomingSms extends BroadcastReceiver {

    Context context;

    public void onReceive(Context context, Intent intent) {
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        try {

            if (bundle != null) {

                this.context = context;

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                if (pdusObj != null) {
                    for (Object aPdusObj : pdusObj) {

                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);

                        String senderNum = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();

                        Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                        new MakeRequestTask().execute(message);

                        //Util.sendNotification(message, context);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }

    private class MakeRequestTask extends AsyncTask<Object, Object, Object> {
        String url = "https://api.wit.ai/message?v=11/11/2017&q=";
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Object[] params) {

            Request.Builder builder = new Request.Builder();
            builder.url(url+params[0]).header("Authorization" , "Bearer 6PUGR2FU2TVKLF7MGAGITU6KWN4XLT52");
            Request request = builder.build();
            try {
                Response response = client.newCall(request).execute();
                //noinspection ConstantConditions
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (o != null)
            {
                Util.sendNotification(Util.getResult(o.toString()), context);
            }
        }
    }
}
