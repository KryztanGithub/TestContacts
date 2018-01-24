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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markapp.testcontacts.data.PhonebookContract.PhonebookEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private static final String jsonUrl = Utils.LOCALHOST_PATH + "json";

    private static final int RESULT_LOAD_IMG = 0;
    private static final int PHONEBOOK_CONTACT_LOADER = 0;

    private Uri contactUri;

    EditText mEditTextName;
    EditText mEditTextNumber;
    TextView mTextViewBirthday;
    ImageView mImageViewPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextName = findViewById(R.id.edittext_name);
        mEditTextNumber = findViewById(R.id.edittext_number);
        mTextViewBirthday = findViewById(R.id.textview_birthday);
//        mImageViewPicture = findViewById(R.id.imageview_picture);

        contactUri = getIntent().getData();
        if (contactUri != null) {
            getLoaderManager().initLoader(PHONEBOOK_CONTACT_LOADER, null, this);
        }
    }

    public void list(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public void sync(View view) {
        new GetJsonOnBackground(this).execute();
    }

    public void query(View view) {
        Utils.displayDatabase(this);
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
//            Bitmap image = BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(PhonebookEntry.COLUMN_IMAGE_PATH)));
//            mImageViewPicture.setImageBitmap(image);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEditTextName.setText("");
        mEditTextNumber.setText("");
        mTextViewBirthday.setText("");
//        mImageViewPicture.setImageResource(0);
    }

    // Save
    public void save(View view) {
        String name = mEditTextName.getText().toString();
        String numberString = mEditTextNumber.getText().toString();
        String birthday = mTextViewBirthday.getText().toString();
//        Drawable picDrawable = mImageViewPicture.getDrawable();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(numberString) || TextUtils.isEmpty(birthday)/* || picDrawable == null */) {
            Toast.makeText(this, "Must fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

//        Bitmap bitmap = ((BitmapDrawable) picDrawable).getBitmap();
//        String image = saveImage(bitmap);
        // ImageURL in Web
//        String webImageUrl = "images/" + image;
//        String imageUrl = Utils.LOCALHOST_PATH + webImageUrl;
//        String imagePath = Environment.getExternalStorageDirectory().toString() + "/" + webImageUrl;

        int number = Integer.parseInt(numberString);

        ContentValues values = new ContentValues();
        values.put(PhonebookEntry.COLUMN_NAME, name);
        values.put(PhonebookEntry.COLUMN_PHONENUMBER, number);
        values.put(PhonebookEntry.COLUMN_BIRTHDAY, birthday);
//        values.put(PhonebookEntry.COLUMN_IMAGE_URL, imageUrl);
//        values.put(PhonebookEntry.COLUMN_IMAGE_PATH, imagePath);

        if (contactUri != null) {
            getContentResolver().update(contactUri, values, null, null);
            long id = ContentUris.parseId(contactUri);
            values.put(PhonebookEntry._ID, id);
//            values.put(PhonebookEntry.COLUMN_IMAGE_URL, webImageUrl);
            new SendPostRequest(values, this, "receiveUpdate").execute();
        } else {
            Uri uri = getContentResolver().insert(PhonebookEntry.CONTENT_URI, values);
            long id = ContentUris.parseId(uri);
            values.put(PhonebookEntry._ID, id);
//            values.put(PhonebookEntry.COLUMN_IMAGE_URL, webImageUrl);
            new SendPostRequest(values, this, "receiveInsert").execute();
        }

        Toast.makeText(MainActivity.this, "Contact saved!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
            TextView jsonResultTextview = findViewById(R.id.textview);
            jsonResultTextview.setText(jsonResult);
            Utils.insertJson(activity, jsonResult);
        }
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

    // Get Image
    public void getImageFromGallery(View view) {
//        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/*");
//        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mImageViewPicture.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "You haven't picked an image.", Toast.LENGTH_LONG).show();
        }
    }

    public String saveImage(Bitmap bitmap) {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/images");
        dir.mkdirs();

        String fileName = System.currentTimeMillis() + ".jpg";
        File outFile = new File(dir, fileName);

        try {
            FileOutputStream outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("MainActivity", "onSaveImage - wrote to " + outFile.getAbsolutePath());
        return fileName;
    }
}
