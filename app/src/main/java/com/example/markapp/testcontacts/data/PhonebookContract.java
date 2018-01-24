package com.example.markapp.testcontacts.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class PhonebookContract {

    public static final String CONTENT_AUTHORITY = "com.example.markapp.testcontacts";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CONTACTS = "contacts";

    private PhonebookContract() {
    }

    public static abstract class PhonebookEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        public static final String TABLE_NAME = "contacts";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_WEB_ID = "webid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONENUMBER = "phonenumber";
        public static final String COLUMN_BIRTHDAY = "birthday";
        public static final String COLUMN_IMAGE_URL = "imageurl";
        public static final String COLUMN_IMAGE_PATH = "imagepath";
        public static final String COLUMN_DELETED = "deleted";
    }

}
