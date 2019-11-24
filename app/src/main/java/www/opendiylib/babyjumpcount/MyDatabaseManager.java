package www.opendiylib.babyjumpcount;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseManager {
    private static final String TAG = MyDatabaseManager.class.getSimpleName();

    public static final Uri BABY_ACTION_URI = Uri.parse("content://" + MyProvider.PROVIDER_NAME + "/babys");
    public static final String[] PROJECTION = {
            MyProvider._ID,
            MyProvider.COUNT,
            MyProvider.ACTION,
            MyProvider.TIME,
            MyProvider.TOTAL,
            MyProvider.PERIOD,
            MyProvider.STATUS
    };

    public MyDatabaseManager() {
        Log.d(TAG, "MyDatabaseManager");
    }

    public static JSONObject getData(Context context, Uri uri, String where, String value) {
        JSONObject result = null;
        if (context != null && uri != null && where != null) {
            /*
            (@RequiresPermission.Read @NonNull Uri uri,
            @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder)
            */
            Cursor cursor = null;
            where = where + "=?";
            String[] selectionArgs = {value};
            cursor = context.getContentResolver().query(uri, PROJECTION, where, selectionArgs, null);
            while (cursor != null && cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int count = cursor.getInt(1);
                String action = cursor.getString(2);
                String time = cursor.getString(3);
                String total = cursor.getString(4);
                String period = cursor.getString(5);
                String status = cursor.getString(6);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(MyProvider._ID, id);
                    jsonObject.put(MyProvider.COUNT, count);
                    jsonObject.put(MyProvider.ACTION, action);
                    jsonObject.put(MyProvider.TIME, time);
                    jsonObject.put(MyProvider.TOTAL, total);
                    jsonObject.put(MyProvider.PERIOD, period);
                    jsonObject.put(MyProvider.STATUS, status);
                } catch (Exception e) {
                    Log.e(TAG, "getData Exception = " + e.getMessage());
                }
                result = jsonObject;
                break;
            }
        }
        return result;
    }

    public static List<JSONObject> getDatas(Context context, Uri uri) {
        List<JSONObject> result = new ArrayList<JSONObject>();
        if (context != null && uri != null) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, PROJECTION, null, null, null);
            } catch (Exception e) {
                Log.e(TAG, "getData query Exception = " + e.getMessage());
                e.printStackTrace();
            }
            while (cursor != null && cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int count = cursor.getInt(1);
                String action = cursor.getString(2);
                String time = cursor.getString(3);
                String total = cursor.getString(4);
                String period = cursor.getString(5);
                String status = cursor.getString(6);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(MyProvider._ID, id);
                    jsonObject.put(MyProvider.COUNT, count);
                    jsonObject.put(MyProvider.ACTION, action);
                    jsonObject.put(MyProvider.TIME, time);
                    jsonObject.put(MyProvider.TOTAL, total);
                    jsonObject.put(MyProvider.PERIOD, period);
                    jsonObject.put(MyProvider.STATUS, status);
                } catch (Exception e) {
                    Log.e(TAG, "getData Exception = " + e.getMessage());
                }
                result.add(jsonObject);
            }
        }
        return result;
    }

    public static boolean updateData(Context context, Uri uri, ContentValues values, String where, String value) {
        boolean result = false;
        if (context != null && where != null && values != null) {
            where = where + "=?";
            String[] selectionArgs = {value};
            result = context.getContentResolver().update(uri, values, where, selectionArgs) > -1;
        }
        return result;
    }

    public static boolean insertData(Context context, Uri uri, ContentValues values) {
        boolean result = false;
        if (context != null && values != null) {
            result = context.getContentResolver().insert(uri, values) != null;
        }
        return result;
    }

    public static boolean deleteData(Context context, Uri uri, String where, String value) {
        boolean result = false;
        if (context != null && where != null) {
            /*(@RequiresPermission.Write @NonNull Uri url, @Nullable String where,
                    @Nullable String[] selectionArgs)*/
            where = where + "=?";
            String[] selectionArgs = {value};
            result = context.getContentResolver().delete(uri, where, selectionArgs) > -1;
        }
        return result;
    }

    public static boolean deleteAllData(Context context, Uri uri) {
        boolean result = false;
        if (context != null) {
            result = context.getContentResolver().delete(uri, null, null) > -1;
        }
        return result;
    }
}
