package com.winhands.widgets;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.winhands.activity.BaseApplication;
import com.winhands.activity.MainActivity;
import com.winhands.bean.SntpClient;

import com.winhands.util.L;
import com.winhands.util.NtpTrustedTime;
import com.winhands.util.SharePreferenceUtil;
import com.winhands.util.SharePreferenceUtils;

import java.util.Calendar;
import java.util.Date;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service implements Runnable {

    public static final String TAG="TSA";
    private static String ACTION_UPDATE_ALL="com.untas.UPDATE_ALL";

    private static String ACTION_SERVICE_STOP="com.untas.ACTION_SERVICE_STOP";
    private SharedPreferences sp;

    private SharePreferenceUtil mSpUtil;
    private static final int UPDATE_TIME = 1000;
    // 周期性更新 widget 的线程
    private Thread mUpdateThread;


    private Context mContext;/*
    Date netDate;
    Calendar netDAteCal;*/
    private TimerAppWidgetProvider appWidgetProvider = TimerAppWidgetProvider.getInstance();

    private String currentNTP= MainActivity.DEFAULT_TNP;
    NtpTrustedTime trustedTime ;

    @Override
    public void onCreate() {
        //SystemClock.setCurrentTimeMillis(1000);

       // Log.d(TAG, "Service Createed==");
        L.d("TimerService create");
        sp = new SharePreferenceUtils(this).getSP();
        mSpUtil = BaseApplication.getInstance().getSharePreferenceUtil();
        mContext = this.getApplicationContext();
      //  netDAteCal=Calendar.getInstance();

        if(!("".equals(mSpUtil.getNtpService()))){
            currentNTP = mSpUtil.getNtpService();
        }

        initThread();

        // 创建并开启线程UpdateThread
        mUpdateThread = new Thread(this);
        mUpdateThread.start();

        trustedTime = NtpTrustedTime.getInstance(this);
        if(!trustedTime.hasCache()){
            trustedTime.setServer(currentNTP);
            trustedTime.setTimeout(5000);

        }
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        // 中断线程，即结束线程。
        if (mUpdateThread != null) {
            mUpdateThread.interrupt();
        }

        mContext.sendBroadcast(new Intent(ACTION_SERVICE_STOP));
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /*
     * 服务开始时，即调用startService()时，onStartCommand()被执行。
     * onStartCommand() 这里的主要作用：
     * (01) 将 appWidgetIds 添加到队列sAppWidgetIds中
     * (02) 启动线程
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /**
         Notification notification = new Notification(R.drawable.ic_app_lg,
         getString(R.string.app_name), System.currentTimeMillis());

         PendingIntent pendingintent = PendingIntent.getActivity(this, 0,
         new Intent(), 0);
         notification.setLatestEventInfo(this, "时间服务", "时间服务",
         pendingintent);
         startForeground(0x111, notification);**/
        return START_STICKY;

    }

    private void initThread(){
        Log.d(TAG,"Pre Thread:"+currentNTP);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
              //  Log.d(TAG, "==getDate");
                getNetDate(currentNTP);
            }
        }, 0,60, TimeUnit.SECONDS);

//       new Thread(new Runnable() {
//            @Override
//            public void run() {
//                getNetDate("1.cn.pool.ntp.org");
//            }
//        }).start();
    }


    private void getNetDate(String ip) {
        if(!trustedTime.hasCache()){
            trustedTime.setServer(currentNTP);
        }
        trustedTime.forceRefresh();
    }



    @Override
    public void run() {

        Date now = new Date();

        try {
            while (true) {

                now.setTime(trustedTime.currentTimeMillis());
                appWidgetProvider.setTime(mContext,now);
                mBinder.setTime(trustedTime.currentTimeMillis());
                Thread.sleep(UPDATE_TIME);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class ServiceStub extends  ITimerService.Stub{
        TimerService mService;

        long mTime;

        ServiceStub(TimerService timerService){
            mService = timerService;
        }

      @Override
        public long getTime() throws RemoteException {
            return  mTime;
        }

        public void setTime(long time){
            mTime=time;
        }

    }
    private  final ServiceStub mBinder= new ServiceStub(this);

}
