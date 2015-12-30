package com.winhands.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.winhands.activity.MainActivity;
import com.winhands.settime.R;

import java.util.Date;

/**
 * Created by cheshire_cat on 15/7/17.
 */
public class TimerAppWidgetProvider  extends AppWidgetProvider {
    private static  final  String LOGTAG="TSA";
    private static  final String CLICK_ACTION = "com.untsa.TIMER_APP_WEIDGET_CLICK";

    private static TimerAppWidgetProvider instance;

    public static  TimerAppWidgetProvider getInstance(){
       if(instance==null){
           instance = new TimerAppWidgetProvider();
       }
        return  instance;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOGTAG, "on Update");

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    // 第一个widget被创建时调用
    @Override
    public void onEnabled(Context context) {
        Log.d(LOGTAG, "onEnabled");
        // 在第一个 widget 被创建时，开启服务
        startService(context);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(LOGTAG, "stopService");
        stopService(context);
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG, "onReceive=" + intent.getAction());
        if(intent.getAction().equals(CLICK_ACTION)){
            Intent activityAction = new Intent(context.getApplicationContext(),MainActivity.class);
            activityAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityAction);
            return;
        }else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            Log.d(LOGTAG," screen on service Start");
            Intent serviceIntent = new Intent(context.getApplicationContext(),TimerService.class);
            context.startService(serviceIntent);
        }


        super.onReceive(context, intent);
    }

    private void startService(Context context){
      //  context.startService(new Intent(TimerService.class.getName()).setPackage("com.winhands.settime"));
       Intent intent = new Intent(context.getApplicationContext(),TimerService.class);
       context.startService(intent);
    }

    private  void stopService(Context context){
        Intent intent = new Intent(context.getApplicationContext(),TimerService.class);
        context.stopService(intent);
    }


    public void setTime(Context context,Date date){
         AppWidgetManager  am= AppWidgetManager.getInstance(context);
         int[] widgetIds =  am.getAppWidgetIds(new ComponentName(context,this.getClass()));
         if(widgetIds.length!=0)
         updateAllAppWidgets(context,am,widgetIds,date);

     }

     void initClickAction(Context context,RemoteViews remoteViews){
         Intent intentClick = new Intent(CLICK_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, 0);
       //  PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intentClick,0);
         remoteViews.setOnClickPendingIntent(R.id.nts_logo,pendingIntent);
     }


      void updateAllAppWidgets(Context context, AppWidgetManager manager, int[] ids,Date date) {

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.timer_widget_2);
        initClickAction(context,remoteView);

        int d;
        for(Integer tmpAppId:ids){
            int appId=tmpAppId.intValue();

            // remoteView.setTextViewText(R.id.appwidget_text,DF.format(date));
//            int h=date.getHours();
//            remoteView.setImageViewResource(R.id.nb_h0,Utils.NUMBERS[h%10]);
//            remoteView.setImageViewResource(R.id.nb_h1,Utils.NUMBERS[h/10]);
//
//            int m=date.getMinutes();
//            remoteView.setImageViewResource(R.id.nb_m0,Utils.NUMBERS[m%10]);
//            remoteView.setImageViewResource(R.id.nb_m1,Utils.NUMBERS[m/10]);
//
//            int s = date.getSeconds();
//            remoteView.setImageViewResource(R.id.nb_s0,Utils.NUMBERS[s%10]);
//            remoteView.setImageViewResource(R.id.nb_s1,Utils.NUMBERS[s/10]);

            d=date.getHours();
            remoteView.setTextViewText(R.id.tv_hour,d/10+""+d%10);
            d=date.getMinutes();
            remoteView.setTextViewText(R.id.tv_min,d/10+""+d%10);
            d=date.getSeconds();
            remoteView.setTextViewText(R.id.tv_sec,d/10+""+d%10);

            manager.updateAppWidget(appId,remoteView);
        }
    }

}
