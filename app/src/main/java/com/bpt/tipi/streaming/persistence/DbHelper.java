package com.bpt.tipi.streaming.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private final static String TAG = DbHelper.class.getSimpleName();
    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LABEL);

        db.execSQL("CREATE TABLE cycleCount (date String PRIMARY KEY, cycleCount float )");
        db.execSQL("CREATE TABLE cycleCountWeekDay (weekday integer PRIMARY KEY, lastDate String, weekdayCycleCount float, numOfWeekdays INTEGER )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + TableLabel.TABLE_NAME);

        Log.i(TAG, "--onUpgrade");
        Log.w(TAG, "--Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        switch (oldVersion) {
            case 1:
        }
    }

    /**
     * Estructura de la creación de la tabla {@link TableLabel}.
     */
    private static final String CREATE_TABLE_LABEL = "CREATE TABLE IF NOT EXISTS " +
            TableLabel.TABLE_NAME + "(" +
            TableLabel.COLUMN_ID + " INTEGER," +
            TableLabel.COLUMN_DESCRIPTION + " TEXT);";

    static abstract class TableLabel {
        static final String TABLE_NAME = "tbl_label";
        static final String COLUMN_ID = "id";
        static final String COLUMN_DESCRIPTION = "description";
    }
}
