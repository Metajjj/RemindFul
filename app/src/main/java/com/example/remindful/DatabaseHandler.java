package com.example.remindful;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    //ID | Month | Year | Title | Note | TimeModified || Time to remind..
    //PK | INT   | INT  | TEXT  | TEXT | TEXT         || TEXT
    // ID | Year + Month + Day + Hour + Min + Sec | Title | Note | Time to remind...
    // YMDHMNS : 20230201155432
    protected final String DBname = "NoteList", ID = "ID", YMDHMS ="YMDHMS", TITLE = "Title", NOTE = "Note", R_TIME = "R_Time";//, Seperator="||||||", NewLine=UniqueNL();

    //Dont need R bool, just check R_Time if null
    private final String[] ColHeads = {ID, YMDHMS,TITLE,NOTE, R_TIME};

    //Changes NL everytime DH is called/creates - newline is used
    private String UniqueNL(){

        String o = "";

        String[] s = {"£", "$", "€", "%", "^", "&", "*", "¬", "¦"};

        for (int i = 0; i < s.length + 100; i++) {
                o += s[((int) (Math.random() * s.length))];
            }

        //System.out.println("NewLine: "+o);

        return o;
    }

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
        //If table exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS `" + DBname + "`");
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS `" + DBname + "`");
        onCreate(sqLiteDatabase);
    }

    protected String InsertBuilder(String TableName, String[] Columns, String[][]Values){
        String cols="",vals="";
        for(int i=0;i<Columns.length;i++){ cols += "`"+Columns[i]+"`"; if(i == Columns.length-1){ continue; } cols += ","; }
        for(String[] S : Values){
            vals +="(";
            for(int i=0;i<Columns.length;i++){
                if(i >= S.length || S[i].equals("") ){ vals+="NULL";} else { vals+= S[i]; }
                if(i == Columns.length-1){ continue; } vals += ",";
            }
            vals += ")"; if(S == Values[Values.length-1]){ continue; } vals += ",";
        }
        //Ex: InsertHandler("TABLE NAME", new String[]{"C1","C2","C3"}, new String[][]{{"A","B","C","Y"},{"D","E","F"}});
        return "INSERT INTO `"+TableName+"` ("+cols+") VALUES "+vals+";";
    }

    //execSQL = no data return
    //rawQuery = Cursor data return
    protected void Writequery(String query) {
        //FIX WRITE QUERY
        SQLiteDatabase db = this.getWritableDatabase();
        try {

            db.execSQL(query);
        } catch (Exception e) {
            System.out.println("WriteDBErr: " + e);
        }
        db.close();
    }

    @SuppressLint("Range")
    protected String[] Readquery(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c;
        String[] output= new String[]{};
        try {
            c = db.rawQuery(query, null); //selectionArgs to replace wildcard `?` in query | error if lacking ?

            output = CursorSorter(c);

            c.close();
        } catch (Exception e) {
            System.out.println("SRS ERR DH : "+e);
        }
        db.close();
        return output;
    }

    @SuppressLint("Range")
    private String[] CursorSorter(Cursor c){
        String output="", Seperator=UniqueNL(), NewLine=UniqueNL();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) { //for each row
            //OUTPUT = headername:value|headername2:value2|
            int i=1; //Skips first troub
            for (String s : ColHeads){

                if(c.getColumnIndex(s)>=0) {
                    output += s + ":" + c.getString(c.getColumnIndex(s)) + Seperator;
                }
                if (
                        i % ColHeads.length == 0 //s.equals(ColHeads[ColHeads.length-1])
                ) { output += NewLine; }

                i++;
            }

            //output += c.getString(c.getColumnIndex(ID)) + "|" + c.getString(c.getColumnIndex(MONTH)) + "|" + c.getString(c.getColumnIndex(YEAR)) + "|" + c.getString(c.getColumnIndex(TITLE)) + "\n";
        }
        //System.out.println("DH_OUT: "+output);

        //RETURN string[] {OUTPUT , NEWLINE , SEPEPERATOR} - loses need to recall or any changing
        return new String[]{output,Seperator,NewLine};
    }
}

