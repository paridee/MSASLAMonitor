package com.alfaprojects.paride.it.msaslamonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by paride on 28/09/15.
 */
public class AlarmSerializer {
    private Context aContext;
    private String dbString;
    private SQLiteDatabase aDatabase;
    private AlarmSQLiteAssetHelper assetHelper;
    private final String Alarm_Tablename    =   "Alarm";
    private final String Alarm_Id           =   "id";
    private final String Alarm_Json         =   "json";

    public AlarmSerializer(Context aContext,String dbName){
        super();
        this.aContext   =   aContext;
        this.dbString   =   dbName;
    }

    /**
     * open the DB
     */
    public void open(){
        AlarmSQLiteAssetHelper  anHelper            =   new AlarmSQLiteAssetHelper(this.aContext,this.dbString);
        this.assetHelper                            =   anHelper;
        this.aDatabase =   anHelper.getWritableDatabase();
    }

    /**
     * close the DB
     */
    public void close(){
        aDatabase.close();
        assetHelper.close();
    }

    ArrayList<TaskInstance> getAllAlarms(){
        Cursor result   =   this.aDatabase.query(Alarm_Tablename,null,null,null,null,null,null);
        ArrayList<TaskInstance>returnList   =   new ArrayList<>();
        result.moveToFirst();
        for(int i=0;i<result.getCount();i++){
            String     json         =   result.getString(result.getColumnIndex(Alarm_Json));
            TaskInstance loaded     =   TaskInstance.loadFromJson(json);
            //System.out.println("Loaded "+loaded.toString());
            returnList.add(loaded);
            result.moveToNext();
        }
        result.close();
        return returnList;
    }

    public int addToDb(TaskInstance aTask){
        ContentValues values  = new ContentValues();
        values.put(Alarm_Json,aTask.generateJson());
        System.out.println("AlarmSerializer.java adding element to DB "+aTask.generateJson());
        return  (int)this.aDatabase.insert(Alarm_Tablename,null,values);
    }
}
