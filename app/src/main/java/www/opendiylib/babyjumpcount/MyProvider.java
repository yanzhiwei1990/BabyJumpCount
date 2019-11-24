package www.opendiylib.babyjumpcount;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class MyProvider extends ContentProvider {
    private final static String TAG = "MyProvider";
    private static final String AUTHORITY = "www.opendiylib.com";
    public static final String PROVIDER_NAME = "www.opendiylib.com";
    public static final String URL = "content://" + PROVIDER_NAME + "/babys";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String _ID = "_id";
    public static final String COUNT = "_count";
    public static final String ACTION = "_action";
    public static final String TIME = "_time";
    public static final String TOTAL = "_total";
    public static final String PERIOD = "_period";
    public static final String STATUS = "_status";

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    public static final int BABYS = 1;
    public static final int BABYS_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "babys", BABYS);
        uriMatcher.addURI(PROVIDER_NAME, "babys/#", BABYS_ID);
    }

    /**
     * 数据库特定常量声明
     */
    private SQLiteDatabase mDb;
    static final String DATABASE_NAME = "MyProvider";
    static final String BABYS_TABLE_NAME = "babys";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + BABYS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " _count TEXT NOT NULL, " +
                    " _action TEXT NOT NULL, " +
                    " _time TEXT NOT NULL, " +
                    " _total TEXT NOT NULL, " +
                    " _period TEXT NOT NULL, " +
                    " _status TEXT NOT NULL);";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  BABYS_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        mDb = dbHelper.getWritableDatabase();
        return (mDb == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = mDb.insert( BABYS_TABLE_NAME, "", values);
        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(BABYS_TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case BABYS:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;
            case BABYS_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == ""){
            sortOrder = COUNT;
        }
        Cursor c = qb.query(mDb, projection, selection, selectionArgs,null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case BABYS:
                count = mDb.delete(BABYS_TABLE_NAME, selection, selectionArgs);
                break;
            case BABYS_ID:
                String id = uri.getPathSegments().get(1);
                count = mDb.delete( BABYS_TABLE_NAME, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case BABYS:
                count = mDb.update(BABYS_TABLE_NAME, values, selection, selectionArgs);
                break;
            case BABYS_ID:
                count = mDb.update(BABYS_TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case BABYS:
                return "cursor.dir";
            case BABYS_ID:
                return "cursor.item";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
