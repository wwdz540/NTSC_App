package com.winhands.service;

import java.lang.ref.SoftReference;

import com.winhands.activity.BaseApplication;
import com.winhands.util.SharePreferenceUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;

public class SyncService extends Service {
	public static onButtonChanged obc;
	private Intent serviceIntent;
	private AlarmManager mAlarmManager = null;
	private PendingIntent mPendingIntent = null;
	private SharedPreferences sp;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public interface onButtonChanged {
		public void onPressButton();
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sp = new SharePreferenceUtils(this).getSP();
		BaseApplication.isOpen = true;
		serviceIntent = new Intent(getApplicationContext(), SyncService.class);
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mPendingIntent = PendingIntent.getService(this, 0, serviceIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		System.out.println(sp.getInt("timefrequent", 0));
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), sp.getInt("timefrequent", 1*60)*1000, mPendingIntent);
		
		
		
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		obc.onPressButton();
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		mAlarmManager.cancel(mPendingIntent);
		stopSelf();
		BaseApplication.isOpen = false;
		super.onDestroy();
	}
}
