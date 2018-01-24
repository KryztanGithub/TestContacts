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
//        TextView imageUrlTextView = view.findViewById(R.id.list_image_url);
//        TextView imagePathTextView = view.findViewById(R.id.list_image_path);
//        ImageView imageImageView = view.findViewById(R.id.list_image);

        int id = cursor.getInt(cursor.getColumnIndex(PhonebookEntry._ID));
        int webId = cursor.getInt(cursor.getColumnIndex(PhonebookEntry.COLUMN_WEB_ID));
        String name = cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_NAME));
        int phonenumber = cursor.getInt(cursor.getColumnIndex(PhonebookEntry.COLUMN_PHONENUMBER));
        String birthday = cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_BIRTHDAY));
//        String imageUrl = cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_IMAGE_URL));
//        String imagePath = cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_IMAGE_PATH));

        idTextView.setText(String.valueOf(id));
        webIdTextView.setText(String.valueOf(webId));
        nameTextView.setText(name);
        phonenumberTextView.setText(String.valueOf(phonenumber));
        birthdayTextView.setText(birthday);
//        imageUrlTextView.setText(imageUrl);
//        imagePathTextView.setText(imagePath);
//        Bitmap image = BitmapFactory.decodeFile(imagePath);
//        imageImageView.setImageBitmap(image);
    }
}
