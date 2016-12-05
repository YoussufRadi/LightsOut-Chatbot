package com.malproject.youssufradi.lightsout_chatbot;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private MessageAdapter adapter;
    private ListView lv;
    private EditText edit;
    private Button send;
    String uuid;
    boolean recieved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.listView);

        adapter = new MessageAdapter(this, R.layout.list_discuss);

        lv.setAdapter(adapter);

        edit = (EditText) findViewById(R.id.editText);
        edit.setFocusable(false);
        edit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(recieved)
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        postData();
                        return true;
                    }
                return false;
            }
        });
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recieved)
                    if(edit.getText() != null || !edit.getText().equals("")) {
                        postData();
                    }
            }
        });
        new FetchAuthFromApi().execute();
//        addItems();
    }

    public void postData(){
        new PostDataToApi().execute(edit.getText().toString());
        adapter.add(new Message(false, edit.getText().toString()));
        edit.setText("");
        recieved = false;
        lv.setSelection(adapter.getCount() - 1);
    }

    class FetchAuthFromApi extends AsyncTask<Void,Void,JSONObject> {

        private final String LOG_TAG = FetchAuthFromApi.class.getSimpleName();

        @Override
        protected JSONObject doInBackground(Void... voids) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String authsonStr = null;
            try {
                final String AUTH_URL = "http://lights-out-chatbot.herokuapp.com/welcome";

                Uri builtUri = Uri.parse(AUTH_URL).buildUpon().build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                authsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return new JSONObject(authsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONObject result) {
            if(result == null) {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                uuid = result.getString("uuid");
                adapter.add(new Message(true,result.getString("message")));
                edit.setFocusableInTouchMode(true);
                recieved = true;
            }catch (Exception e){
                Log.e(LOG_TAG, "onPostExecute: "+ e.toString() );
            }

        }
    }

    class PostDataToApi extends AsyncTask<String,Void,String> {

        private final String LOG_TAG = PostDataToApi.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            URL url;
            String response = "";
            try {
                url = new URL("https://lights-out-chatbot.herokuapp.com/chat");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization",uuid);
                conn.setRequestProperty("Content-Type", "application/json");

                HashMap<String,String> postDataParams = new HashMap<String, String>();
                postDataParams.put("message",params[0]);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write("{\"message\" : \""+ params[0] +"\"}");

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();
                Log.e(LOG_TAG, "doInBackground: "+ responseCode );
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                    JSONObject resJSON = new JSONObject(response);
                    response = resJSON.getString("message");
                }
                else {
                    response="";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(LOG_TAG, "doInBackground: " + response );

            return response;
        }

        protected void onPostExecute(String result) {
            if(result == null) {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.add(new Message(true,result));
            recieved = true;
            lv.setSelection(adapter.getCount() - 1);
        }
    }

}
