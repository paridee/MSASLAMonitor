package com.alfaprojects.paride.it.msaslamonitor;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by paride on 28/09/15.
 */
public class AlarmSQLiteAssetHelper extends SQLiteAssetHelper {
    private static final int DATABASE_VERSION = 1;
    public AlarmSQLiteAssetHelper(Context aContext,String dbName){
        super(aContext,dbName,null,DATABASE_VERSION);
    }
}
