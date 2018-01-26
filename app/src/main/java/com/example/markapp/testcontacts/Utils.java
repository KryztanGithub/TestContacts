package com.example.markapp.testcontacts;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.markapp.testcontacts.data.PhonebookContract.PhonebookEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
                String deleted = currentContact.getString("deleted");

                ContentValues contentValues = new ContentValues();
                contentValues.put(PhonebookEntry.COLUMN_WEB_ID, webid);
                contentValues.put(PhonebookEntry.COLUMN_NAME, name);
                contentValues.put(PhonebookEntry.COLUMN_PHONENUMBER, phonenumber);
                contentValues.put(PhonebookEntry.COLUMN_BIRTHDAY, birthday);
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
}