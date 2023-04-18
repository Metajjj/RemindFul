package com.example.remindful;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHandler extends SQLiteOpenHelper {
    //ID | Month | Year | Title | Note | TimeModified || Time to remind..
    //PK | INT   | INT  | TEXT  | TEXT | TEXT         || TEXT
    // ID | Year + Month + Day + Hour + Min + Sec | Title | Note | Time to remind...
    // YMDHMNS : 20230201155432
    protected final String DBname = "NoteList", ID = "ID", YMDHMS ="YMDHMS", TITLE = "Title", NOTE = "Note", R_TIME = "R_Time";

    private final String[] ColHeads = {ID, YMDHMS,TITLE,NOTE, R_TIME};

    public DatabaseHandler(Context c) {
        super(c, "NoteList", null, 1);

        //System.out.println("NL: "+NewLine);
    }

    protected void ResetTable() {
        DatabaseHandler.this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS `" + DBname + "`");
        DatabaseHandler.this.getWritableDatabase().execSQL("CREATE TABLE `" + DBname + "` (`" + ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" + YMDHMS + "` INT, `" + TITLE + "` TEXT, `"+NOTE+"` TEXT, `"+R_TIME+"` TEXT)");
        DatabaseHandler.this.getWritableDatabase().close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //create db
        sqLiteDatabase.execSQL("CREATE TABLE `" + DBname + "` (`" + ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" + YMDHMS + "` INT, `" + TITLE + "` TEXT, `"+NOTE+"` TEXT, `"+R_TIME+"` TEXT)");

        //ResetTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS `" + DBname + "`");
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS `" + DBname + "`");
        onCreate(sqLiteDatabase);
    }

    @SuppressLint("Range")
    protected ArrayList<HashMap<String,String>> CursorSorter(Cursor c){

        ArrayList<HashMap<String,String>> Res = new ArrayList<>();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            //for each row
            HashMap<String,String> Data = new HashMap<>();

            for (String s : ColHeads){
                if(c.getColumnIndex(s)>=0) {
                    Data.put(s,c.getString(c.getColumnIndex(s)));
                }
            }
            Res.add(Data);
            //System.out.println(Data);
            //for( Map.Entry<String,String> Entry : Data.entrySet()) { System.out.println("Key: "+Entry.getKey()+" | Val: "+Entry.getValue()); }
        }

        return Res;
    }
}

