package com.example.markapp.testcontacts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.markapp.testcontacts.data.PhonebookContract.PhonebookEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ListActivity";

    private static final String jsonUrl = Utils.LOCALHOST_PATH + "json";

    private static final int PHONEBOOK_LOADER = 0;

    private PhonebookCursorAdapter mPhonebookCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ListView listView = findViewById(R.id.listview);

        TextView emptyView = findViewById(R.id.emptyview);
        listView.setEmptyView(emptyView);

        mPhonebookCursorAdapter = new PhonebookCursorAdapter(this, null);
        listView.setAdapter(mPhonebookCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alertEditOrDelete(id);
            }
        });

        getLoaderManager().initLoader(PHONEBOOK_LOADER, null, this);
    }

    private void alertEditOrDelete(final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(PhonebookEntry.COLUMN_DELETED, "y");
                Uri contactUri = ContentUris.withAppendedId(PhonebookEntry.CONTENT_URI, id);
                getContentResolver().update(contactUri, contentValues, null, null);
                // Send POST Request For Delete
                contentValues.put(PhonebookEntry._ID, id);
                new SendPostRequestForDelete(contentValues).execute();
            }
        });
        builder.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri contactUri = ContentUris.withAppendedId(PhonebookEntry.CONTENT_URI, id);
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                intent.setData(contactUri);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static class SendPostRequestForDelete extends AsyncTask<Void, Void, Void> {

        private ContentValues contentValues;

        SendPostRequestForDelete(ContentValues contentValues) {
            this.contentValues = contentValues;
        }

        protected Void doInBackground(Void... voids) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate(PhonebookEntry._ID, contentValues.getAsInteger(PhonebookEntry._ID))
                        .accumulate(PhonebookEntry.COLUMN_DELETED, contentValues.getAsString(PhonebookEntry.COLUMN_DELETED));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Utils.LOCALHOST_PATH + "receiveDeleted")
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ListActivity.this, MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                PhonebookEntry.CONTENT_URI,
                null,
                PhonebookEntry.COLUMN_DELETED + "=?",
                new String[]{"n"},
                PhonebookEntry.COLUMN_WEB_ID + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPhonebookCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPhonebookCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_sync:
                new GetJsonOnBackground(this).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Sync
    @SuppressLint("StaticFieldLeak")
    public class GetJsonOnBackground extends AsyncTask<Void, Void, String> {

        private Activity activity;

        GetJsonOnBackground(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(jsonUrl)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResult = response.body().string();
                Log.d(TAG, "doInBackground() called with: voids = [" + jsonResult + "]");
                return jsonResult;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String jsonResult) {
            Utils.insertJson(activity, jsonResult);
        }
    }
}
