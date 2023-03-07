package com.example.remindful;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    //ID | Month | Year | Title | Note | To be reminded? || Time to remind..
    //PK | INT   | INT  | TEXT  | TEXT | BOOL/INT        || TEXT
    protected final String DBname = "NoteList", ID = "ID", MONTH = "Month", YEAR = "Year", TITLE = "Title", NOTE = "Note", REMIND = "Remind", R_TIME = "R_Time";

    public DatabaseHandler(Context c) {
        super(c, "ClockList", null, 1);
    }

    protected void ResetTable() {
        DatabaseHandler.this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS `" + DBname + "`");
        DatabaseHandler.this.getWritableDatabase().execSQL("CREATE TABLE `" + DBname + "` (`" + ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" + MONTH + "` INT, `" + YEAR + "` INT, `" + TITLE + "` TEXT, `"+NOTE+"` TEXT, `"+REMIND+"` INT, `"+R_TIME+"` TEXT)");
        DatabaseHandler.this.getWritableDatabase().close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //create db
        sqLiteDatabase.execSQL("CREATE TABLE `" + DBname + "` (`" + ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" + MONTH + "` INT, `" + YEAR + "` INT, `" + TITLE + "` TEXT, `"+NOTE+"` TEXT, `"+REMIND+"` INT, `"+R_TIME+"` TEXT)");

        //ResetTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        //If table exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS `" + DBname + "`");
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS `" + DBname + "`");
        onCreate(sqLiteDatabase);
    }

    //execSQL = no data return
    //rawQuery = Cursor data return
    protected void Writequery(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL(query);
        } catch (Exception e) {
            System.out.println("WriteDBErr: " + e);
        }
        db.close();
    }

    @SuppressLint("Range")
    protected String Readquery(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c;
        String output = "";
        try {
            c = db.rawQuery(query, null); //selectionArgs to replace wildcard `?` in query | error if lacking ?
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                output += c.getString(c.getColumnIndex(ID)) + "|" + c.getString(c.getColumnIndex(MONTH)) + "|" + c.getString(c.getColumnIndex(YEAR)) + "|" + c.getString(c.getColumnIndex(TITLE)) + "\n";
            }
            c.close();
        } catch (Exception e) {
            output = "ReadDBErr: " + e;
        }
        db.close();
        return output;
    }
}

