package com.winhands.service;

import android.app.Service;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;

import android.content.SharedPreferences;

import com.winhands.activity.BaseApplication;
import com.winhands.activity.MainActivity;
import com.winhands.bean.SntpClient;
import com.winhands.util.L;
import com.winhands.util.SharePreferenceUtil;
import com.winhands.widgets.TimerAppWidgetProvider;

import java.util.Date;


public class TsaService extends Service {
    private SharedPreferences sp;
    //从snt服务器上获取执行的时间
    private long sntpDelay= 10000*60;
    TimerRun timerRun;

    private Context mContext;

    private  int sntp_idx=0;

    private SharePreferenceUtil mSpUtil;
    private  String tnpUrl="";

    @Override
    public void onCreate() {
        super.onCreate();
        L.d("tsaServer create");
        mSpUtil = BaseApplication.getInstance().getSharePreferenceUtil();
        mContext = this.getApplicationContext();
        timerRun = new TimerRun();
        timerRun.mHandler = mSntpHandler;
        timerRun.mContext= mContext;

        tnpUrl = mSpUtil.getNtpService();
       // mSntpHandler.sendEmptyMessageDelayed(1,300);
        Message msg =mSntpHandler.obtainMessage();
        msg.what=2;
        msg.getData().putLong("net_time", (new Date().getTime()));
      //  mSntpHandler.sendMessageDelayed(msg,100);
        L.d("sendMsgEnd");
    }

    public TsaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean syncSNTP() {

        SntpClient client = new SntpClient();

       /// tnpUrl="202.112.29.82";
        L.d("syncSNTp+"+tnpUrl);
        if (client.requestTime(tnpUrl, 3000)) {

            long now = client.getNtpTime() + SystemClock.elapsedRealtime()
                    - client.getNtpTimeReference();
//            Calendar ca = Calendar.getInstance(TimeZone.getDefault());
//            ca.getTimeInMillis();
//
            if(now - System.currentTimeMillis()<5000) return false;

            long netDate = now
                    - ((8 - sp.getInt("timezone", 8)) * 60 * 60 * 1000);
            mSntpHandler.sendEmptyMessageDelayed(1,sntpDelay);

            Message msg = mSntpHandler.obtainMessage();
            Bundle data =msg.getData();
            msg.what=2;
            data.putLong("net_time",netDate);
            mSntpHandler.sendMessage(msg);
            L.d("=========\n\n成功");

        } else {
            L.d("获取时间失败"+client.getNtpTime());
             tnpUrl= MainActivity.NTP_URLS[sntp_idx];
            sntp_idx = (sntp_idx+1) % MainActivity.NTP_URLS.length;
            mSntpHandler.sendEmptyMessageDelayed(1,10000);
            return false;
        }

        return true;
    }

    private Handler mSntpHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            L.d("reservr message"+msg.what);
            switch (msg.what){
                case 1:
                    syncSNTP();

                    break;
                case 2:
                   // this.removeCallbacks(timerRun);
                    timerRun.time=msg.getData().getLong("net_time");
                    this.removeCallbacks(timerRun);
                    this.post(timerRun);
                    break;

            }

            super.handleMessage(msg);
        }
    };



    public static class   TimerRun implements Runnable{
        long time;
        Handler mHandler;
        Context mContext;
         TimerAppWidgetProvider appWidgetProvider = TimerAppWidgetProvider.getInstance();

        @Override
        public void run() {

                time +=1000;
                L.d("运行时间" + time);
            mHandler.postDelayed(this, 1000);
            appWidgetProvider.setTime(mContext,new Date(time));

        }
    }
//    private void regesiterReceive(){
//        final IntentFilter filter = new IntentFilter();
//        // 屏幕灭屏广播
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        // 屏幕亮屏广播
//        filter.addAction(Intent.ACTION_SCREEN_ON);
//        // 屏幕解锁广播
//        filter.addAction(Intent.ACTION_USER_PRESENT);
//        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
//        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
//        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
//        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        registerReceiver(broadcastReceiver,filter);
//    }
//
//    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            L.d("receive"+intent.getAction());
//        }
//    };

}
