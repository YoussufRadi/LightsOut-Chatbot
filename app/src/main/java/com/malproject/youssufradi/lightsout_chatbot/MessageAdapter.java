package com.malproject.youssufradi.lightsout_chatbot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by y_sam on 12/1/2016.
 */

public class MessageAdapter extends ArrayAdapter<Message> {

    public MessageAdapter(Context context, int resource) {
        super(context, resource);
    }
    private TextView messageView;
    private List<Message> messages = new ArrayList<Message>();
    private LinearLayout wrapper;

    @Override
    public void add(Message object) {
        messages.add(object);
        super.add(object);
    }

    public int getCount() {
        return this.messages.size();
    }

    public Message getItem(int index) {
        return this.messages.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_discuss, parent, false);
        }

        wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

        Message message = getItem(position);

        messageView = (TextView) row.findViewById(R.id.message);

        messageView.setText(message.content);

        messageView.setBackgroundResource(message.left ? R.drawable.bubble_yellow : R.drawable.bubble_green);
        wrapper.setGravity(message.left ? Gravity.LEFT : Gravity.RIGHT);

        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
