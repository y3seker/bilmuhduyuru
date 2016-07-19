package com.y3seker.bilmuhduyuru.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.y3seker.bilmuhduyuru.models.Annc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yunus Emre Şeker on 05.06.2015.
 * -
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "Database";

    // DB Fields
    public static final String KEY_ROWID = "_id";

    public static final String KEY_DUYURU = "duyuru";
    public static final String KEY_TARIH = "tarih";
    public static final String KEY_INDEX = "d_index";
    public static final String KEY_URL = "url";
    public static final String KEY_ICERIK = "icerik";


    public static final String[] ALL_KEYS = new String[]{KEY_ROWID, KEY_DUYURU, KEY_TARIH, KEY_URL, KEY_INDEX, KEY_ICERIK};

    public static final String DATABASE_NAME = "DUYURULAR";
    public static final String DATABASE_TABLE = "mainTable";
    public static final int DATABASE_VERSION = 4;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "

                    + KEY_DUYURU + " text not null, "
                    + KEY_TARIH + " text not null, "
                    + KEY_URL + " text not null, "
                    + KEY_INDEX + " INTEGER not null, "
                    + KEY_ICERIK + " text not null"

                    + ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading application's database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data!");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }


    public long insertRow(Annc annc) throws NullPointerException, SQLiteException {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DUYURU, annc.getTitle());
        initialValues.put(KEY_INDEX, annc.getIndex());
        initialValues.put(KEY_TARIH, annc.getDbDate());
        initialValues.put(KEY_URL, annc.getUrl());
        initialValues.put(KEY_ICERIK, annc.getContent());

        // Insert it into the database.
        return getWritableDatabase().insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return getWritableDatabase().delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = KEY_TARIH + " DESC";
        Cursor c = getReadableDatabase().query(true, DATABASE_TABLE, ALL_KEYS,
                null, null, null, null, where, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getAllRows(String where) {
        Cursor c = getReadableDatabase().query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getAllOrdered(String orderBy) {
        Cursor c = getReadableDatabase().query(true, DATABASE_TABLE, ALL_KEYS,
                null, null, null, null, orderBy, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /**
     * Get a specific row (by rowId)
     *
     * @param rowId for finding the row
     * @return Cursor
     */
    public Cursor getRow(long rowId) {

        String where = KEY_ROWID + "=" + rowId;
        Cursor c = getReadableDatabase().query(true, DATABASE_TABLE, ALL_KEYS, where,
                null, null, null, null, null);

        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    public boolean updateRow(Annc annc) throws NullPointerException, SQLiteException {
        String where = KEY_INDEX + "=" + annc.getIndex();

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_DUYURU, annc.getTitle());
        newValues.put(KEY_INDEX, annc.getIndex());
        newValues.put(KEY_TARIH, annc.getDbDate());
        newValues.put(KEY_URL, annc.getUrl());
        newValues.put(KEY_ICERIK, annc.getContent());

        // Insert it into the database.
        return getWritableDatabase().update(DATABASE_TABLE, newValues, where, null) != 0;
    }

    public void write(List<Annc> list) {
        try {
            for (Annc annc : list) {
                insertRow(annc);
            }
        } catch (NullPointerException | SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void update(List<Annc> list) {
        try {
            for (Annc annc : list) {
                updateRow(annc);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public List<Annc> read(int count) {
        List<Annc> result = new ArrayList<>();
        try (Cursor cursor = getAllRows()) {
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    result.add(new Annc(cursor.getString(cursor.getColumnIndex(KEY_DUYURU)),
                            cursor.getString(cursor.getColumnIndex(KEY_TARIH)),
                            cursor.getString(cursor.getColumnIndex(KEY_URL)),
                            cursor.getString(cursor.getColumnIndex(KEY_ICERIK)),
                            cursor.getInt(cursor.getColumnIndex(KEY_INDEX))));
                    i++;
                } while (cursor.moveToNext() && i != count);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }


    public int deleteLast() {
        Cursor c = getAllRows();
        c.moveToFirst();
        c.moveToNext();
        int newLastIndex = c.getInt(c.getColumnIndex(KEY_INDEX));
        c.moveToFirst();
        deleteRow(c.getLong(0));
        c.close();
        return newLastIndex;
    }

    public int getLastIndex() {
        int result = 0;
        Cursor cursor = getReadableDatabase().query(true, DATABASE_TABLE, new String[]{KEY_INDEX}, null,
                null, null, null, KEY_INDEX + " DESC", null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(KEY_INDEX));
        }
        cursor.close();
        return result;
    }

    public Cursor search(String query, boolean isInContent) {
        String where = KEY_DUYURU + " LIKE '%" + query + "%'";
        if (isInContent) where += "or " + KEY_ICERIK + " LIKE '%" + query + "%'";
        Cursor c = getReadableDatabase().query(true, DATABASE_TABLE, ALL_KEYS, where,
                null, null, null, KEY_TARIH + " DESC", null);

        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }
}
