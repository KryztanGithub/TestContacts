package com.example.markapp.testcontacts.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.markapp.testcontacts.data.PhonebookContract.PhonebookEntry;

public class PhonebookDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "phonebook.db";

    public PhonebookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " +
                PhonebookEntry.TABLE_NAME + " (" +
                PhonebookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PhonebookEntry.COLUMN_WEB_ID + " INTEGER, " +
                PhonebookEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                PhonebookEntry.COLUMN_PHONENUMBER + " INTEGER NOT NULL, " +
                PhonebookEntry.COLUMN_BIRTHDAY + " TEXT NOT NULL, " +
                PhonebookEntry.COLUMN_DELETED + " TEXT NOT NULL DEFAULT 'n');";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
