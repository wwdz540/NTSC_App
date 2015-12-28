package com.winhands.widgets;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.winhands.activity.BaseApplication;
import com.winhands.activity.VMainActivity;
import com.winhands.settime.R;
import com.winhands.util.NtpTrustedTime;
import com.winhands.util.SharePreferenceUtil;
import com.winhands.util.SharePreferenceUtils;

import java.util.Date;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service {

    public static final String TAG="TSA";
    private static String ACTION_UPDATE_ALL="com.untas.UPDATE_ALL";

    private static String ACTION_SERVICE_STOP="com.untas.ACTION_SERVICE_STOP";
    private SharedPreferences sp;

    private SharePreferenceUtil mSpUtil;
    private static final int UPDATE_TIME = 1000;
    // 周期性更新 widget 的线程
    private Thread mUpdateThread;


    private Context mContext;
    private TimerAppWidgetProvider appWidgetProvider = TimerAppWidgetProvider.getInstance();

    private String currentNTP= VMainActivity.DEFAULT_TNP;
    NtpTrustedTime trustedTime ;

    HandlerThread handlerThread;
    TimerHandler handler;
    @Override
    public void onCreate() {

        Log.d(TAG," service create");
        sp = new SharePreferenceUtils(this).getSP();
        mSpUtil = BaseApplication.getInstance().getSharePreferenceUtil();
        mContext = this.getApplicationContext();
      //  netDAteCal=Calendar.getInstance();

        if(!("".equals(mSpUtil.getNtpService()))){
            currentNTP = mSpUtil.getNtpService();
        }


        trustedTime = NtpTrustedTime.getInstance(this);
        if(!trustedTime.hasCache()){
            trustedTime.setServer(currentNTP);
            trustedTime.setTimeout(5000);

        }


        initThread();

        handlerThread = new HandlerThread("Main");
        handlerThread.start();
        handler = new TimerHandler(handlerThread.getLooper());
        handler.sendEmptyMessageDelayed(1,500);


        regesiterReceive();
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        Log.d(TAG,"service Destroy");
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

         Notification notification = new Notification(R.drawable.ic_launcher,
         getString(R.string.app_name), System.currentTimeMillis());

         PendingIntent pendingintent = PendingIntent.getActivity(this, 0,
                 new Intent(), 0);
         notification.setLatestEventInfo(this, "时间服务", "时间服务",
                 pendingintent);
         startForeground(0x111, notification);
        return START_STICKY;

    }

    private void initThread(){
        Log.d(TAG,"Pre Thread:"+currentNTP);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getNetDate(currentNTP);
            }
        }, 0, 60, TimeUnit.SECONDS);


    }


    private void getNetDate(String ip) {
        if(!trustedTime.hasCache()){
            trustedTime.setServer(currentNTP);
        }
        trustedTime.forceRefresh();
    }



    Date now = new Date();
    public void updateTime(){
       // Log.d(TAG,"update time");
        if(trustedTime.hasCache()) {
            now.setTime(trustedTime.currentTimeMillis());
            appWidgetProvider.setTime(mContext, now);
            mBinder.setTime(trustedTime.currentTimeMillis());
            SystemClock.sleep(UPDATE_TIME);
        }else {
            now.setTime(System.currentTimeMillis());
            appWidgetProvider.setTime(mContext,now);
            SystemClock.sleep(100l);
        }
    }


//    private Runnable run = new Runnable() {
//        @Override
//        public void run() {
//            while(true) {
//                updateTime();
//                SystemClock.sleep(1000l);
//            }
//        }
//    };
    class TimerHandler extends Handler{
        /**
         * Use the provided {@link Looper} instead of the default one.
         *
         * @param looper The looper, must not be null.
         */
        public TimerHandler(Looper looper) {
            super(looper);
        }

        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    TimerService.this.updateTime();
                    this.sendEmptyMessageDelayed(1, 300);
                    break;
                case 2:
                    this.removeMessages(1);
            }


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


    private void regesiterReceive(){
        final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(broadcastReceiver,filter);
    }

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

          if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
              Log.d(TAG,"remove message");
             // handlerThread.interrupt();
              handler.sendEmptyMessage(2);
          }else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
             // handlerThread.notify();
              handler.sendEmptyMessage(1);
          }
        }
    };

}
