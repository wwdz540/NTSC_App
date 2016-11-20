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
import com.winhands.util.L;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 *
 */
public class TimerAppWidgetProvider  extends AppWidgetProvider {
    private static  final String LOGTAG="TSA";
    private static  final String CLICK_ACTION = "com.untsa.TIMER_APP_WEIDGET_CLICK";

    private static final String[] WEEKS_STRS = new String[]{"","星期天","星期一","星期二","星期三","星期四","星期五","星期六"};

    private static TimerAppWidgetProvider instance;

    private Map<Integer,Integer> layoutMap;

    public void setLayoutMap(Map<Integer, Integer> layoutMap) {
        this.layoutMap = layoutMap;
    }

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


    public void setTime(Context context,Calendar cal){
         AppWidgetManager  am= AppWidgetManager.getInstance(context);
         int[] widgetIds =  am.getAppWidgetIds(new ComponentName(context,this.getClass()));
         if(widgetIds.length!=0)
         updateAllAppWidgets(context,am,widgetIds,cal);

     }

     void initClickAction(Context context,RemoteViews remoteViews){
         Intent intentClick = new Intent(CLICK_ACTION);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, 0);
         remoteViews.setOnClickPendingIntent(R.id.nts_logo,pendingIntent);
     }
        DateFormat dateDf = new SimpleDateFormat("yyyy.MM.dd");

      void updateAllAppWidgets(Context context, AppWidgetManager manager, int[] ids,Calendar cal) {
        Date date = cal.getTime();

       // RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.timer_widget_1);
          RemoteViews[] remoteViewses = new RemoteViews[]{
                  new RemoteViews(context.getPackageName(), R.layout.timer_widget_1),
                  new RemoteViews(context.getPackageName(), R.layout.timer_widget_2)
          };

        for(RemoteViews remoteView:remoteViewses) {
            initClickAction(context, remoteView);
        }

        int d;
         RemoteViews remoteView = remoteViewses[0];
          L.d("map ="+layoutMap);
        for(Integer tmpAppId:ids){
            int appId=tmpAppId.intValue();
            Integer layout = 0;

            if(layoutMap.containsKey(appId))
                layout = layoutMap.get(appId);


            if(layout<2)
                remoteView = remoteViewses[layout];

            d=date.getHours();
            remoteView.setTextViewText(R.id.tv_hour,d/10+""+d%10);
            d=date.getMinutes();
            remoteView.setTextViewText(R.id.tv_min,d/10+""+d%10);

            d=cal.get(Calendar.SECOND);
            remoteView.setTextViewText(R.id.tv_sec,d/10+""+d%10);
            remoteView.setTextViewText(R.id.wg_date,dateDf.format(date));

            remoteView.setTextViewText(R.id.tv_week, WEEKS_STRS[cal.get(Calendar.DAY_OF_WEEK)]);

            manager.updateAppWidget(appId,remoteView);
        }
    }

}
