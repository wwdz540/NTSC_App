package com.winhands.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.winhands.settime.R;
import com.winhands.util.L;

import java.util.HashMap;
import java.util.Map;

public class WidgetConfigActivity extends Activity implements View.OnClickListener {
    public static final String WIDGET_ADD_ACTION =" com.winhands.add_widgetAction";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static  final String PREF_PREFIX_KEY ="widget_";
    protected static final String PREFS_NAME = "com.winhands.widget";
    private Button saveBtn ;

    private RadioGroup radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mAppWidgetId =bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        saveBtn = (Button)findViewById(R.id.button);
        saveBtn.setOnClickListener(this);
        radius = (RadioGroup) findViewById(R.id.radius);

    }

    private void setResultAndFinish(){
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,mAppWidgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private void saveWidgetConfig(){
        int id = radius.getCheckedRadioButtonId();
        SharedPreferences.Editor prefs = this.getSharedPreferences(PREFS_NAME, 0 ).edit();
        int layout=R.layout.timer_widget_1;
        switch (id){
            case R.id.have_back:
                layout=0;
                break;
            case R.id.no_back:
                layout=1;
                break;
        }
        prefs.putInt("" + mAppWidgetId, layout);
        prefs.commit();

        Intent intent = new Intent(WIDGET_ADD_ACTION);
        intent.putExtra("id",mAppWidgetId);
        intent.putExtra("layout",layout);
        sendBroadcast(intent);
    }



    @Override
    public void onClick(View v) {
        saveWidgetConfig();
        setResultAndFinish();
    }

    public static int loadLayout(Context context,int appWidgetId){
        SharedPreferences prefs = context.getSharedPreferences( PREFS_NAME, 0 );
        int value = prefs.getInt( ""+appWidgetId, 0 );
        return value;
    }

    public static Map<Integer,Integer> getLayoutMap(Context context){
        SharedPreferences prefs = context.getSharedPreferences( PREFS_NAME, 0 );
        Map<String,?> aa = prefs.getAll();
        Map<Integer,Integer> map = new HashMap<Integer, Integer>();
        for(Map.Entry<String,?> entry:aa.entrySet()){
            try {
                map.put(Integer.parseInt(entry.getKey()), (Integer) entry.getValue());
            }catch (Exception ex){
                L.d("getMap出错");
                ex.printStackTrace();
            }
        }
        return map;

    }


}
