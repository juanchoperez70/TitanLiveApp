package com.bpt.tipi.streaming.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import android.widget.Toast;

import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.model.CycleCountWeekDay;
import com.bpt.tipi.streaming.model.CycleCount;

import java.util.LinkedList;
import java.util.List;

public class ChargeCycleBattery {

    private DbHelper dbHelper;
    private Context context;
    private SQLiteDatabase writableDatabase;

    private static final String[] b = new String[]{"date", "cycleCount"};
    private static final String[] c = new String[]{"lastDate", "weekdayCycleCount", "numOfWeekdays", "weekday"};

    public ChargeCycleBattery(Context mContext) {
        context = mContext;
        dbHelper = new DbHelper(context, Database.DB_NAME, null, Database.DB_VERSION);
    }

    public int addCycleCountWeekDay(CycleCountWeekDay aeVar) {
        Toast.makeText(context, "--addCycleCountWeekDay", Toast.LENGTH_LONG).show();
        writableDatabase = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("lastDate", aeVar.getLastDate());
        contentValues.put("weekdayCycleCount", Float.valueOf(aeVar.getWeekdayCycleCount()));
        contentValues.put("numOfWeekdays", Integer.valueOf(aeVar.getNumOfWeekdays()));
        contentValues.put("weekday", Integer.valueOf(aeVar.getWeekday()));
        int update = writableDatabase.update("cycleCountWeekDay", contentValues, "weekday = ?", new String[]{String.valueOf(aeVar.getWeekday())});
        if (update == 0) {
            writableDatabase.replace("cycleCountWeekDay", null, contentValues);
        }
        writableDatabase.close();
        return update;
    }

    public CycleCount getCycleCount(String str) {
        Cursor query = dbHelper.getReadableDatabase().query("cycleCount", b, " date = ?", new String[]{String.valueOf(str)}, null, null, null, null);
        CycleCount aVar = new CycleCount(str, 0.0f);
        if (query != null && query.getCount() > 0) {
            query.moveToFirst();
            aVar.setDate(query.getString(0));
            aVar.setCycleCount(Float.parseFloat(query.getString(1)));
        }
        return aVar;
    }

    public CycleCount getCycleCount() {
        Cursor query = dbHelper.getReadableDatabase().query("cycleCount", b, null, null, null, null, null, null);
        CycleCount aVar = new CycleCount("", 0.0f);
        if (query != null && query.getCount() > 0) {
            query.moveToFirst();
            aVar.setDate(query.getString(0));
            aVar.setCycleCount(Float.parseFloat(query.getString(1)));
        }
        return aVar;
    }

    public CycleCountWeekDay getCycleCountWeekDay(int weekday) {
        Cursor query = dbHelper.getReadableDatabase().query("cycleCountWeekDay", c, " weekday = ?", new String[]{String.valueOf(weekday)}, null, null, null, null);
        CycleCountWeekDay aeVar = new CycleCountWeekDay();
        if (query != null && query.getCount() > 0) {
            query.moveToFirst();
            aeVar.setLastDate(query.getString(0));
            aeVar.setWeekdayCycleCount(query.getFloat(1));
            aeVar.setNumOfWeekdays(query.getInt(2));
            aeVar.setWeekday(query.getInt(3));
        }
        return aeVar;
    }

    public List listCycleCountWeekDay() {
        List linkedList = new LinkedList();
        Cursor rawQuery = dbHelper.getWritableDatabase().rawQuery("SELECT * FROM cycleCountWeekDay", null);
        if (rawQuery.moveToFirst()) {
            do {
                CycleCountWeekDay aeVar = new CycleCountWeekDay();
                aeVar.setNumOfWeekdays(rawQuery.getInt(2));
                aeVar.setLastDate(rawQuery.getString(0));
                aeVar.setWeekdayCycleCount(rawQuery.getFloat(1));
                aeVar.setWeekday(rawQuery.getInt(3));
                linkedList.add(aeVar);
            } while (rawQuery.moveToNext());
        }
        return linkedList;
    }

    public void addCycleCount(CycleCount aVar) {
        Toast.makeText(context, "--addCycleCount", Toast.LENGTH_LONG).show();
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", aVar.getDate());
        contentValues.put("cycleCount", Float.valueOf(aVar.getCycleCount()));
        writableDatabase.replace("cycleCount", null, contentValues);
        writableDatabase.close();
    }

    /*
    public ad b() {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        Cursor rawQuery = dbHelper.getWritableDatabase().rawQuery("SELECT * FROM cycleCount ORDER BY date DESC LIMIT 7", null);
        String str = "2010/10/10";
        long j = 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        ad adVar = new ad();
        if (rawQuery.moveToLast()) {
            String str2 = str;
            int i = 0;
            while (!rawQuery.isBeforeFirst() && i < 7) {
                long time;
                String string;
                int i2;
                float parseFloat = Float.parseFloat(rawQuery.getString(1));
                try {
                    time = (simpleDateFormat.parse(rawQuery.getString(0)).getTime() - simpleDateFormat.parse(str2).getTime()) / 86400000;
                } catch (ParseException e) {
                    e.printStackTrace();
                    time = j;
                }
                if (str2.equals("2010/10/10")) {
                    string = rawQuery.getString(0);
                    arrayList.add(new BarEntry(parseFloat, i));
                    arrayList2.add(string);
                    rawQuery.moveToPrevious();
                    i2 = i + 1;
                } else if (str2.equals("2010/10/10") || time <= 1) {
                    string = rawQuery.getString(0);
                    arrayList.add(new BarEntry(parseFloat, i));
                    arrayList2.add(string);
                    rawQuery.moveToPrevious();
                    i2 = i + 1;
                } else {
                    Object obj = str2;
                    for (int i3 = 1; ((long) i3) < time; i3++) {
                        arrayList.add(new BarEntry(0.0f, i));
                        try {
                            obj = simpleDateFormat.format(Long.valueOf(simpleDateFormat.parse(obj).getTime() + 86400000));
                        } catch (ParseException e2) {
                            e2.printStackTrace();
                        }
                        arrayList2.add(obj);
                    }
                    string = rawQuery.getString(0);
                    arrayList.add(new BarEntry(parseFloat, i));
                    arrayList2.add(string);
                    rawQuery.moveToPrevious();
                    i2 = i + 1;
                }
                str2 = string;
                i = i2;
                j = time;
            }
        }
        adVar.a(arrayList);
        adVar.b(arrayList2);
        adVar.d();
        return adVar;
    }
    */

    public void close() {
        if (writableDatabase != null) {
            writableDatabase.close();
        }
    }
}
