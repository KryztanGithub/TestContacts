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
        TextView nameTextView = view.findViewById(R.id.list_name);
        TextView phonenumberTextView = view.findViewById(R.id.list_phonenumber);

        String name = cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_NAME));
        int phonenumber = cursor.getInt(cursor.getColumnIndex(PhonebookEntry.COLUMN_PHONENUMBER));

        nameTextView.setText(name);
        phonenumberTextView.setText(String.valueOf(phonenumber));
    }
}
