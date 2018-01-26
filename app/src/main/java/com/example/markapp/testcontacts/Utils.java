package com.example.markapp.testcontacts;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markapp.testcontacts.data.PhonebookContract.PhonebookEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class Utils {

    private static final String TAG = "Utils";

    public static final String LOCALHOST_PATH = "http://192.168.1.56/cakephp/contacts/";

    private Utils() {
    }

    public static void insertJson(Activity activity, String jsonResult) {
        try {
            JSONArray contactArray = new JSONArray(jsonResult);

            for (int i = 0; i < contactArray.length(); i++) {
                JSONObject currentObject = contactArray.getJSONObject(i);
                JSONObject currentContact = currentObject.getJSONObject("Contact");
                int webid = currentContact.getInt("id");
                String name = currentContact.getString("name");
                int phonenumber = currentContact.getInt("phonenumber");
                String birthday = currentContact.getString("birthday");
//                String image = currentContact.getString("image");
//                String imageurl = LOCALHOST_PATH + image;
//                String imagepath = Environment.getExternalStorageDirectory().toString() + "/" + image;
                String deleted = currentContact.getString("deleted");

//                new DownloadImageAndSave().execute(new String[]{imageurl, imagepath});

                ContentValues contentValues = new ContentValues();
                contentValues.put(PhonebookEntry.COLUMN_WEB_ID, webid);
                contentValues.put(PhonebookEntry.COLUMN_NAME, name);
                contentValues.put(PhonebookEntry.COLUMN_PHONENUMBER, phonenumber);
                contentValues.put(PhonebookEntry.COLUMN_BIRTHDAY, birthday);
//                contentValues.put(PhonebookEntry.COLUMN_IMAGE_URL, imageurl);
//                contentValues.put(PhonebookEntry.COLUMN_IMAGE_PATH, imagepath);
                contentValues.put(PhonebookEntry.COLUMN_DELETED, deleted);

                Cursor cursor = activity.getContentResolver().query(PhonebookEntry.CONTENT_URI,
                        new String[]{PhonebookEntry._ID},
                        PhonebookEntry.COLUMN_WEB_ID + "=?",
                        new String[]{String.valueOf(webid)},
                        null);

                if (cursor.getCount() == 0) {
                    Uri newUri = activity.getContentResolver().insert(PhonebookEntry.CONTENT_URI, contentValues);
                    long newId = ContentUris.parseId(newUri);
                    contentValues.put(PhonebookEntry._ID, newId);
                    new SendPostRequestForAndroidId(contentValues).execute();
                } else {
                    if (cursor.moveToFirst()) {
                        int id = cursor.getInt(cursor.getColumnIndex(PhonebookEntry._ID));
                        Uri uri = ContentUris.withAppendedId(PhonebookEntry.CONTENT_URI, id);
                        int rowUpdated = activity.getContentResolver().update(uri, contentValues, null, null);
                    }
                }
                cursor.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(activity, "Synced", Toast.LENGTH_SHORT).show();
    }

    public static class SendPostRequestForAndroidId extends AsyncTask<Void, Void, Void> {
        private ContentValues contentValues;

        SendPostRequestForAndroidId(ContentValues contentValues) {
            this.contentValues = contentValues;
        }

        protected Void doInBackground(Void... voids) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate(PhonebookEntry._ID, contentValues.getAsInteger(PhonebookEntry._ID))
                        .accumulate(PhonebookEntry.COLUMN_WEB_ID, contentValues.getAsString(PhonebookEntry.COLUMN_WEB_ID));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Utils.LOCALHOST_PATH + "receiveAndroidId")
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString()))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                Log.d(TAG, "doInBackground() called with: voids = [" + responseString + "]");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static void displayDatabase(Activity activity) {
        Cursor cursor = activity.getContentResolver().query(PhonebookEntry.CONTENT_URI, null,
                null, null, null);

        if (cursor.getCount() == 0) {
            Toast.makeText(activity, "Database empty", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Database queried", Toast.LENGTH_SHORT).show();
        }

        TextView textView = activity.findViewById(R.id.textview);
        textView.setText("");
        while (cursor.moveToNext()) {
            textView.append(" " + String.valueOf(cursor.getInt(cursor.getColumnIndex(PhonebookEntry._ID))));
            textView.append(" " + String.valueOf(cursor.getInt(cursor.getColumnIndex(PhonebookEntry.COLUMN_WEB_ID))));
            textView.append(" " + cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_NAME)));
            textView.append(" " + String.valueOf(cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_PHONENUMBER))));
            textView.append(" " + cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_BIRTHDAY)));
            textView.append(" " + cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_DELETED)));
//            textView.append(" " + cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_IMAGE_URL)));
//            textView.append(" " + cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_IMAGE_PATH)));
            textView.append("\n");
        }

        cursor.close();

    }

    public static class DownloadImageAndSave extends AsyncTask<String[], Void, Void> {

        @Override
        protected Void doInBackground(String[]... strings) {
            int count;
            try {
                URL url = new URL(strings[0][0]);

                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();
                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                // Output stream to write file
                OutputStream output = new FileOutputStream(strings[0][1]);
                byte data[] = new byte[1024];

                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;

                    // writing data to file
                    output.write(data, 0, count);
                }
                // flushing output
                output.flush();
                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

    }

}