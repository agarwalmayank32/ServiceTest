package info.mayankag.servicetest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IncomingSms extends BroadcastReceiver {

    Context context;

    ArrayList<String> excludedWords = new ArrayList<>();

    int flag = 0;

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
                        String message = currentMessage.getDisplayMessageBody();
                        Log.d("SmsReceiver", " message: " + message);
                        new MakeRequestTask().execute(message);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MakeRequestTask extends AsyncTask<Object, Object, Object> {

        String url = "https://api.wit.ai/message?v=";
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        @Override
        protected void onPreExecute() {
            Calendar c = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            String date = df.format(c.getTime());
            url+= date+"&q=";
            parseJSON();
        }

        @Override
        protected String doInBackground(Object[] params) {

            String body = params[0].toString();

            if(body.contains("/"))
            {
                //noinspection ResultOfMethodCallIgnored
                body.replace("/","\"/");
            }

            for (String excludedWord : excludedWords) {
                if (body.toLowerCase().contains(excludedWord)) {
                    flag++;
                    break;
                }
            }

            if(flag == 0)
            {
                Request.Builder builder = new Request.Builder();
                builder.url(url+body).header("Authorization" , "Bearer 6PUGR2FU2TVKLF7MGAGITU6KWN4XLT52");
                Request request = builder.build();
                try {
                    Response response = client.newCall(request).execute();
                    //noinspection ConstantConditions
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.d("error","Restricted Words");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (o != null)
            {
                String resultMessage = Util.getResult(o.toString(), context);
                if(resultMessage!=null)
                {
                    Util.sendNotification(resultMessage, context);
                }
            }
        }
    }

    private String getJSONString(Context context)
    {
        StringBuilder str = new StringBuilder();
        try
        {
            AssetManager assetManager = context.getAssets();
            InputStream in = assetManager.open("json.txt");
            InputStreamReader isr = new InputStreamReader(in);
            char [] inputBuffer = new char[1000];

            int charRead;
            while((charRead = isr.read(inputBuffer))>0)
            {
                String readString = String.copyValueOf(inputBuffer,0,charRead);
                str.append(readString);
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        return str.toString();
    }

    public void parseJSON()
    {
        try {
            JSONArray json = new JSONArray(getJSONString(context));

            for(int i=0;i<json.length();i++)
            {
                JSONObject jsonObject = json.getJSONObject(i);
                excludedWords.add(jsonObject.getString("value"));
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
