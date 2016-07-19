package com.y3seker.bilmuhduyuru.models;

import android.database.Cursor;

import com.y3seker.bilmuhduyuru.database.DatabaseHelper;

import java.text.ParseException;

/**
 * Created by Yunus Emre Şeker on 14.11.2015.
 * -
 */
public class AnncProxy {

    private Annc annc;

    public AnncProxy(Cursor cursor) {
        try {
            annc = new Annc(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_DUYURU)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_TARIH)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_URL)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ICERIK)),
                    cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_INDEX)));
        } catch (ParseException e) {
            annc = null;
            e.printStackTrace();
        }
    }

    public Annc getAnnc() {
        return annc;
    }
}
