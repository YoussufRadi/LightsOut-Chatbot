package com.malproject.youssufradi.lightsout_chatbot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MessageAdapter adapter;
    private ListView lv;
    private LoremIpsum ipsum;
    private EditText editText1;
    private static Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        random = new Random();
        ipsum = new LoremIpsum();

        lv = (ListView) findViewById(R.id.listView1);

        adapter = new MessageAdapter(this, R.layout.list_discuss);

        lv.setAdapter(adapter);

        editText1 = (EditText) findViewById(R.id.editText1);
        editText1.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    adapter.add(new OneComment(false, editText1.getText().toString()));
                    editText1.setText("");
                    return true;
                }
                return false;
            }
        });

        addItems();
    }
}
