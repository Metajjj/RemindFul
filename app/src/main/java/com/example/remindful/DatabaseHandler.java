package com.example.remindful;

import android.annotation.SuppressLint;
import android.content.ContentValues;
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

    private int AutoIncrementVal= CursorSorter(
            this.getReadableDatabase().query(DBname,null,null,null,null,null,null) ).size()
            ;

    protected int getAutoIncrement(){ return ++AutoIncrementVal; }

    protected void RecheckAutoIncrement(){

        this.close();
        //Loop thru database = highest ID + 1
        ArrayList<HashMap<String,String>> Notes = CursorSorter( this.getReadableDatabase().query(DBname,new String[]{ID},null,null,null,null,ID+" ASC") );

        for (int i=1;i<=Notes.size();i++ ) {
            String CurrID = Notes.get( i-1 ).get(ID);
            //System.out.println("i: "+ i +" | ID: "+CurrID);
            if(! CurrID.equals(i+"")){
                //Update to fill up empty spots in ID
                //ContentValues CV = new ContentValues(); CV.put(ID,i); this.getWritableDatabase().update(DBname,CV,ID+" = ?",new String[]{CurrID});
                AutoIncrementVal=--i;
                return;
            }
            //Update innate A_I to match my new latest ID
            if(i==Notes.size()){ AutoIncrementVal=i; }
        }

        DatabaseHandler.this.close();
    }

    protected void Insert(String TableName, String NullColHck, ContentValues CV){
        //Put incr if lower better
        RecheckAutoIncrement(); //Should insert automatically into empty slots in DB w o affecting rest of DB existing rows

        CV.remove(ID); CV.put(ID,getAutoIncrement()); //Re-adds if better slot available else should be same
        DatabaseHandler.this.getWritableDatabase().insert(TableName,NullColHck,CV);
    }

    public DatabaseHandler(Context c) {
        super(c, "NoteList", null, 1);
    }

    protected void ResetTable(SQLiteDatabase s) {
        s.execSQL("DROP TABLE IF EXISTS `" + DBname + "`");

        s.execSQL("CREATE TABLE `" + DBname + "` (`" + ID + "` INTEGER PRIMARY KEY NOT NULL, `" + YMDHMS + "` INT, `" + TITLE + "` TEXT, `"+NOTE+"` TEXT, `"+R_TIME+"` TEXT)");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //create db
        //sqLiteDatabase.execSQL("CREATE TABLE `" + DBname + "` (`" + ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" + YMDHMS + "` INT, `" + TITLE + "` TEXT, `"+NOTE+"` TEXT, `"+R_TIME+"` TEXT)");

        ResetTable(sqLiteDatabase);
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

        System.out.println(Res);
        return Res;
    }
}

