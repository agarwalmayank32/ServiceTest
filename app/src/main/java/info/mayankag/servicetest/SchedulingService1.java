package info.mayankag.servicetest;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SchedulingService1 extends IntentService{

    public SchedulingService1() {
        super("SMSSchedulingService");
    }

    GoogleAccountCredential mCredential;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[ ] SCOPES = { GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY};
    Context context;

    ArrayList<String> excludedWords = new ArrayList<>();

    int flag = 0;

    @Override
    protected void onHandleIntent(Intent intent) {

        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
        context = getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mCredential.setSelectedAccountName(prefs.getString(PREF_ACCOUNT_NAME,null));

        getResultsFromApi();

        AlarmReceiver1.completeWakefulIntent(intent);
    }

    private void getResultsFromApi() {
        if (! Util.isDeviceOnline(context)) {
            Toast.makeText(context,"No network connection available.",Toast.LENGTH_SHORT).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        String url = "https://api.wit.ai/message?v=";

        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart").build();
        }

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
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> output) {
            if (!(output == null || output.size() == 0)) {

                for (int i=0;i<output.size();i++)
                {
                    Toast.makeText(context,output.get(i),Toast.LENGTH_LONG).show();
                    Util.sendNotification(output.get(i),context);
                }
            }
        }


        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                //Toast.makeText(MainActivity.this,"The following error occurred:\n" + mLastError.getMessage(),Toast.LENGTH_SHORT).show();
                Log.d(" errs","The following error occurred:\n" + mLastError.toString());
            } else {
                Toast.makeText(context, "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }

        private List<String> getDataFromApi() throws IOException {
            String user = "me";
            List<String> messageList = new ArrayList<>();

            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            Long tsLong1 = tsLong - 300;
            String ts1 = tsLong1.toString();

            String query = "in:inbox AND after:" + ts1 + " AND before:" + ts;
            ListMessagesResponse messageResponse = mService.users().messages().list(user).setQ(query).execute();

            try {
                JSONObject jsonObject = new JSONObject(messageResponse.toString());
                int totalMessages = Integer.parseInt(jsonObject.getString("resultSizeEstimate"));

                if(totalMessages > 0)
                {
                    List<Message> messages = messageResponse.getMessages();

                    if (messages.size() > 0) {
                        for (Message message : messages) {
                            Message message2 = mService.users().messages().get(user, message.getId()).execute();
                            byte[] bodyBytes = Base64.decodeBase64(message2.getPayload().getParts().get(0).getBody().getData().trim()); // get body
                            String body = new String(bodyBytes, "UTF-8");

                            if(body.contains("/"))
                            {
                                //noinspection ResultOfMethodCallIgnored
                                body.replace("/","\"/");
                            }

                            body = body.replaceAll("\\s{2,}", " ").trim();

                            for (String excludedWord : excludedWords) {
                                if (body.toLowerCase().contains(excludedWord)) {
                                    flag++;
                                    break;
                                }
                            }

                            if(flag == 0) {
                                Request.Builder builder = new Request.Builder();
                                builder.url(url + body).header("Authorization", "Bearer 6PUGR2FU2TVKLF7MGAGITU6KWN4XLT52");
                                Request request = builder.build();

                                try {
                                    Response response = client.newCall(request).execute();
                                    //noinspection ConstantConditions
                                    String object = response.body().string();

                                    Log.d("service", object);

                                    if (object != null) {
                                        String resultMessage = Util.getResult(object, context);
                                        if (resultMessage != null) {
                                            messageList.add(resultMessage);
                                        }
                                    } else {
                                        messageList.add("Network Slow Try Again");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();

                                    Log.d("service", e.toString());
                                }
                                Log.d("bb", body);
                            }
                            else {
                                Log.d("error","Restricted Words");
                            }
                        }
                    }
                    else
                    {
                        Log.d("service","No messages");
                    }
                }
                else
                {
                    Log.d("service","No Total Messages");
                }
            } catch (JSONException e) {
                e.printStackTrace();

                Log.d("service",e.toString());
            }
            return messageList;
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
