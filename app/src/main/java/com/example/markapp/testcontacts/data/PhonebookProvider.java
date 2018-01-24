package com.example.markapp.testcontacts.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.markapp.testcontacts.data.PhonebookContract.PhonebookEntry;

public class PhonebookProvider extends ContentProvider {

    private PhonebookDbHelper mPhonebookDbHelper;

    private static final int CONTACTS = 100;
    private static final int CONTACT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PhonebookContract.CONTENT_AUTHORITY, PhonebookContract.PATH_CONTACTS, CONTACTS);
        sUriMatcher.addURI(PhonebookContract.CONTENT_AUTHORITY, PhonebookContract.PATH_CONTACTS + "/#", CONTACT_ID);
    }

    @Override
    public boolean onCreate() {
        mPhonebookDbHelper = new PhonebookDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projections, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mPhonebookDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                cursor = database.query(PhonebookEntry.TABLE_NAME, projections, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CONTACT_ID:
                selection = PhonebookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PhonebookEntry.TABLE_NAME, projections, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Invalid uri for query: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = mPhonebookDbHelper.getWritableDatabase();
        long id;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                id = database.insert(PhonebookEntry.TABLE_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Invalid uri for insert: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mPhonebookDbHelper.getWritableDatabase();
        int row;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                row = database.update(PhonebookEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case CONTACT_ID:
                selection = PhonebookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                row = database.update(PhonebookEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid uri for update: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return row;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mPhonebookDbHelper.getWritableDatabase();
        int rows;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                rows = database.delete(PhonebookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CONTACT_ID:
                selection = PhonebookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rows = database.delete(PhonebookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid uri for delete: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return PhonebookEntry.CONTENT_LIST_TYPE;
            case CONTACT_ID:
                return PhonebookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Invalid uri: " + uri + " with match: " + match);
        }
    }
}
