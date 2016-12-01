package com.malproject.youssufradi.lightsout_chatbot;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private MessageAdapter adapter;
    private ListView lv;
    private EditText edit;
    String uuid;

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
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    adapter.add(new Message(false, edit.getText().toString()));
                    edit.setText("");
                    return true;
                }
                return false;
            }
        });
        new FetchAuthFromApi().execute();
//        addItems();
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

            // This will only happen if there was an error getting or parsing the forecast.
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
            }catch (Exception e){
                Log.e(LOG_TAG, "onPostExecute: "+ e.toString() );
            }

        }
    }

}
