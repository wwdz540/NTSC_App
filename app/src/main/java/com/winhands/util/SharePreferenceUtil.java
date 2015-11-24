package com.winhands.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtil {
	public static final String CITY_SHAREPRE_FILE = "city";
	private static final String CASH_CITY = "_city";
	private static final String SIMPLE_CLIMATE = "simple_climate";
	private static final String SIMPLE_TEMP = "simple_temp";
	private static final String TIMESAMP = "timesamp";
	private static final String TIME = "time";
	private static final String VERSION = "version";
	private static final String PINYIN = "pinyin";
	private static final String TNP_URL="tnp_url";

	private SharedPreferences sp;
	private SharedPreferences.Editor editor;

	public SharePreferenceUtil(Context context, String file) {
		sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		editor = sp.edit();
	}

	// city
	public void setCity(String city) {
		// TODO Auto-generated method stub
		editor.putString(CASH_CITY, city);
		editor.commit();
	}

	public String getCity() {
		return sp.getString(CASH_CITY, "北京");
	}

	// SimpleClimate
	public void setSimpleClimate(String climate) {
		editor.putString(SIMPLE_CLIMATE, climate);
		editor.commit();
	}

	public String getSimpleClimate() {
		return sp.getString(SIMPLE_CLIMATE, "N/A");
	}

	// SimpleTemp
	public void setSimpleTemp(String temp) {
		editor.putString(SIMPLE_TEMP, temp);
		editor.commit();
	}

	public String getSimpleTemp() {
		return sp.getString(SIMPLE_TEMP, "");
	}

	// timesamp
	public void setTimeSamp(long time) {
		editor.putLong(TIMESAMP, time);
		editor.commit();
	}

	public long getTimeSamp() {
		return sp.getLong(TIMESAMP, System.currentTimeMillis());
	}

	// time
	public void setTime(String time) {
		editor.putString(TIME, time);
		editor.commit();
	}

	public String getTime() {
		return sp.getString(TIME, "");
	}

	// database
	public void setVersion(int version) {
		editor.putInt(VERSION, version);
		editor.commit();
	}

	public int getVersion() {
		return sp.getInt(VERSION, -1);
	}

	public void setPinyin(String pinyin) {
		editor.putString(PINYIN, pinyin);
		editor.commit();
	}
	public String getPinyin() {
		return sp.getString(PINYIN, "beijing");
	}


	public void setNtpService(String url){
		editor.putString(TNP_URL,url);
		editor.commit();
	}

	public String getNtpService(){

		return sp.getString(TNP_URL,"");
	}
}
