package com.utar.habit;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SQLiteAdapter {
    public static final String DATABASE_NAME = "Discipline";
    public static final String DATABASE_TABLE = "Habit";
    public static final int DATABASE_VERSION = 2;
    public static final String USER_ID = "userID";
    public static final String AMOUNT = "amount";
    public static final String CREATE_TIME = "CRT_TMS";
    public static final String ID = "id";
    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;
    private final Context context;
    private static final String SCRIPT_CREATE_DATABASE = "create table " + DATABASE_TABLE +
            " (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            USER_ID + " text not null, " +
            AMOUNT + " text not null," + CREATE_TIME + " text not null);";

    public SQLiteAdapter(Context c) {
        context = c;
    }

    public SQLiteAdapter openToRead() throws
            android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public SQLiteAdapter openToWrite() throws
            android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        sqLiteHelper.close();
    }

    public long insert(String userID, String amount) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_ID, userID);
        contentValues.put(AMOUNT, amount);
        contentValues.put(CREATE_TIME, new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
        return sqLiteDatabase.insert(DATABASE_TABLE, null,
                contentValues);
    }

    public long update(String amount, String id,String selectedTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AMOUNT, amount);
        contentValues.put(ID, id);
        contentValues.put(CREATE_TIME, selectedTime);
        String[] whereArgs = new String[]{id};
        String whereClause = ID + " = ?";
        return sqLiteDatabase.update(DATABASE_TABLE, contentValues, whereClause, whereArgs);
    }

    public int deleteAll() {
        return sqLiteDatabase.delete(DATABASE_TABLE, null, null);
    }

    public int deleteByID(String id) {
        String[] whereArgs = new String[]{id};
        String whereClause = ID + " = ? ";
        return sqLiteDatabase.delete(DATABASE_TABLE, whereClause, whereArgs);
    }
    public String queueAll() {
        String[] columns = new String[] { USER_ID, AMOUNT, CREATE_TIME };
        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns,
                null, null, null, null, null);
        String result = "";
        int index_CONTENT1 = cursor.getColumnIndex(USER_ID);
        int index_CONTENT2 = cursor.getColumnIndex(AMOUNT);
        int index_CONTENT3 = cursor.getColumnIndex(CREATE_TIME);
        for (cursor.moveToFirst(); !(cursor.isAfterLast());
             cursor.moveToNext()) {
            result = result + cursor.getString(index_CONTENT1) + "," +
                    cursor.getString(index_CONTENT2) + "," +
                    cursor.getString(index_CONTENT3) + "\n";
        }
        return result;
    }
    public List<String> queueByIDANDDate(String userID,String DATE) {
        String[] columns = new String[]{ID,AMOUNT, CREATE_TIME};
        String[] whereArgs = new String[]{userID,DATE + "%"};
        String whereClause = USER_ID + " = ?" + " AND " + CREATE_TIME + " LIKE ?";
        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, whereClause, whereArgs, null, null, null);
        List<String> result = new ArrayList<String>();
        int index_CONTENT2 = cursor.getColumnIndex(AMOUNT);
        int index_CONTENT3 = cursor.getColumnIndex(CREATE_TIME);
        int index_CONTENT1 = cursor.getColumnIndex(ID);
        for (cursor.moveToFirst(); !(cursor.isAfterLast());
             cursor.moveToNext()) {
            result.add(cursor.getString(index_CONTENT2) + "," + cursor.getString(index_CONTENT3) + "," + cursor.getString(index_CONTENT1));
        }
        return result;
    }
    public String queueByID(String id) {
        String[] columns = new String[]{CREATE_TIME};
        String[] whereArgs = new String[]{id};
        String whereClause = ID + " = ? ";
        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, whereClause, whereArgs, null, null, null);
        String result = "";
        int index_CONTENT3 = cursor.getColumnIndex(CREATE_TIME);
        for (cursor.moveToFirst(); !(cursor.isAfterLast());
             cursor.moveToNext()) {
            result = cursor.getString(index_CONTENT3);
        }
        return result;
    }

    public class SQLiteHelper extends SQLiteOpenHelper {
        public SQLiteHelper(Context context, String name,
                            CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SCRIPT_CREATE_DATABASE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int
                newVersion) {
            db.execSQL(SCRIPT_CREATE_DATABASE);
        }
    }
}
