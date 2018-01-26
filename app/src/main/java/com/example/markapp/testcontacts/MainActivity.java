package com.example.markapp.testcontacts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markapp.testcontacts.data.PhonebookContract.PhonebookEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private static final int PHONEBOOK_CONTACT_LOADER = 0;

    private Uri contactUri;

    EditText mEditTextName;
    EditText mEditTextNumber;
    TextView mTextViewBirthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextName = findViewById(R.id.edittext_name);
        mEditTextNumber = findViewById(R.id.edittext_number);
        mTextViewBirthday = findViewById(R.id.textview_birthday);

        contactUri = getIntent().getData();
        if (contactUri != null) {
            getLoaderManager().initLoader(PHONEBOOK_CONTACT_LOADER, null, this);
        }
    }

    // Cursor Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                contactUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            mEditTextName.setText(cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_NAME)));
            mEditTextNumber.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(PhonebookEntry.COLUMN_PHONENUMBER))));
            mTextViewBirthday.setText(cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_BIRTHDAY)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEditTextName.setText("");
        mEditTextNumber.setText("");
        mTextViewBirthday.setText("");
    }

    // Save
    public void save(View view) {
        String name = mEditTextName.getText().toString();
        String numberString = mEditTextNumber.getText().toString();
        String birthday = mTextViewBirthday.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(numberString) || TextUtils.isEmpty(birthday)) {
            Toast.makeText(this, "Must fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int number = Integer.parseInt(numberString);

        ContentValues values = new ContentValues();
        values.put(PhonebookEntry.COLUMN_NAME, name);
        values.put(PhonebookEntry.COLUMN_PHONENUMBER, number);
        values.put(PhonebookEntry.COLUMN_BIRTHDAY, birthday);

        if (contactUri != null) {
            getContentResolver().update(contactUri, values, null, null);
            long id = ContentUris.parseId(contactUri);
            values.put(PhonebookEntry._ID, id);
            new SendPostRequest(values, this, "receiveUpdate").execute();
        } else {
            Uri uri = getContentResolver().insert(PhonebookEntry.CONTENT_URI, values);
            long id = ContentUris.parseId(uri);
            values.put(PhonebookEntry._ID, id);
            new SendPostRequest(values, this, "receiveInsert").execute();
        }

        Toast.makeText(MainActivity.this, "Contact saved!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Post Insert or Update to Web
    public static class SendPostRequest extends AsyncTask<Void, Void, String> {

        @SuppressLint("StaticFieldLeak")
        private Activity activity;
        private ContentValues contentValues;
        private String receiveAction;

        SendPostRequest(ContentValues contentValues, Activity activity, String receiveAction) {
            this.contentValues = contentValues;
            this.activity = activity;
            this.receiveAction = receiveAction;
        }

        protected String doInBackground(Void... voids) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate(PhonebookEntry._ID, contentValues.getAsInteger(PhonebookEntry._ID))
                        .accumulate(PhonebookEntry.COLUMN_NAME, contentValues.getAsString(PhonebookEntry.COLUMN_NAME))
                        .accumulate(PhonebookEntry.COLUMN_PHONENUMBER, contentValues.getAsInteger(PhonebookEntry.COLUMN_PHONENUMBER))
                        .accumulate(PhonebookEntry.COLUMN_BIRTHDAY, contentValues.getAsString(PhonebookEntry.COLUMN_BIRTHDAY));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Utils.LOCALHOST_PATH + receiveAction)
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString()))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                Log.d(TAG, "doInBackground() called with: voids = [" + responseString + "]");
                if (this.receiveAction.equals("receiveInsert")) {
                    String[] res = responseString.split("Web ID = ");
                    // Return Web ID
                    String webIdString = res[1].trim();
                    Log.d(TAG, "doInBackground() called with: voids = [" + webIdString + "]");
                    return webIdString;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String webidString) {
            if (webidString != null) {
                Log.d(TAG, "onPostExecute() called with: webidString = [" + "Contact inserted." + "]");
                int webid = Integer.parseInt(webidString);
                ContentValues webIdValue = new ContentValues();
                webIdValue.put(PhonebookEntry.COLUMN_WEB_ID, webid);
                int id = contentValues.getAsInteger(PhonebookEntry._ID);
                Uri uri = ContentUris.withAppendedId(PhonebookEntry.CONTENT_URI, id);
                activity.getContentResolver().update(uri, webIdValue, null, null);
            } else {
                Log.d(TAG, "onPostExecute() called with: webidString = [" + "Contact updated." + "]");
            }
        }
    }

    // Date Picker
    public static class DatePickerFragment extends android.support.v4.app.DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            int month1 = month + 1;
            String formattedMonth = String.valueOf(month1);
            String formattedDay = String.valueOf(day);

            if (month1 < 10) {
                formattedMonth = "0" + month1;
            }
            if (day < 10) {
                formattedDay = "0" + day;
            }

            TextView birthdayTextView = getActivity().findViewById(R.id.textview_birthday);
            birthdayTextView.setText(year + "-" + formattedMonth + "-" + formattedDay);
        }
    }

    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}