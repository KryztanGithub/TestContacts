package com.example.markapp.testcontacts;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.markapp.testcontacts.data.PhonebookContract.PhonebookEntry;

public class PhonebookCursorAdapter extends CursorAdapter {

    public PhonebookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView idTextView = view.findViewById(R.id.list_id);
        TextView webIdTextView = view.findViewById(R.id.list_webid);
        TextView nameTextView = view.findViewById(R.id.list_name);
        TextView phonenumberTextView = view.findViewById(R.id.list_phonenumber);
        TextView birthdayTextView = view.findViewById(R.id.list_birthday);

        int id = cursor.getInt(cursor.getColumnIndex(PhonebookEntry._ID));
        int webId = cursor.getInt(cursor.getColumnIndex(PhonebookEntry.COLUMN_WEB_ID));
        String name = cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_NAME));
        int phonenumber = cursor.getInt(cursor.getColumnIndex(PhonebookEntry.COLUMN_PHONENUMBER));
        String birthday = cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_BIRTHDAY));

        idTextView.setText(String.valueOf(id));
        webIdTextView.setText(String.valueOf(webId));
        nameTextView.setText(name);
        phonenumberTextView.setText(String.valueOf(phonenumber));
        birthdayTextView.setText(birthday);
    }
}
