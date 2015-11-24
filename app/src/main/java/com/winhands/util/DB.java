package com.winhands.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.winhands.bean.Milliseconds;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "settime.db";
	public static final String CREATE_TABLE = "CREATE table IF NOT EXISTS ";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
	private AtomicInteger mUsedCounter = new AtomicInteger();

	public DB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public interface TimeTable {
		String TIMEID = "time_id";
		String LOCALTIME = "time_localtime";
		String NETTIME = "time_nettime";
		String TABLE_NAME = "timeTable";
		

		String DROP_TABLE_SQL = DROP_TABLE + TABLE_NAME;
		String CREATE_TABLE_SQL = CREATE_TABLE + TABLE_NAME + "(" + TIMEID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + LOCALTIME
				+ " INTEGER," + NETTIME + " INTEGER )";
	}
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(TimeTable.CREATE_TABLE_SQL);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL(TimeTable.DROP_TABLE_SQL);
		onCreate(db);
	}

	private void closeDB(SQLiteDatabase sqlitedb) {
		if (mUsedCounter.decrementAndGet() == 0) { // 如没有其他地方使用，关闭数据库
			sqlitedb.close();
		}
		System.out.println("使用数据库的方法个数（closeDB）：" + mUsedCounter.get());
	}

	public void insertTime(long localtime,long nettime) {
		SQLiteDatabase sqlitedb = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(TimeTable.LOCALTIME, localtime);
			values.put(TimeTable.NETTIME, nettime);
			sqlitedb.insert(TimeTable.TABLE_NAME, null, values);
		closeDB(sqlitedb);
	}
	public List<Milliseconds> getTime(){
		
		List<Milliseconds> list = new ArrayList<Milliseconds>();
		
		SQLiteDatabase sqlitedb = getReadableDatabase();
		Cursor c = sqlitedb.query(TimeTable.TABLE_NAME, null, null, null, null, null, TimeTable.TIMEID+" desc","0,10");
		while (c.moveToNext()) {
			long localtime = c.getLong(c.getColumnIndex(TimeTable.LOCALTIME));
			long nettime = c.getLong(c.getColumnIndex(TimeTable.NETTIME));
			Milliseconds item = new Milliseconds();
			item.setLocaltime(localtime);
			item.setNettime(nettime);
			list.add(item);
		}
		if (c != null) {
			c.close();
		}
		closeDB(sqlitedb);
		return list;
	}

}
