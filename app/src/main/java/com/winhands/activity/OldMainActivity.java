package com.winhands.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.michael.easydialog.EasyDialog;
import com.winhands.activity.SelectCtiyActivity.onCityNameChanged;
import com.winhands.activity.WheatherActivity.onSelected;
import com.winhands.bean.Backgound;
import com.winhands.bean.City;
import com.winhands.bean.TimeBean;
import com.winhands.bean.TimeFrequent;
import com.winhands.bean.WeatherInfo;
import com.winhands.http.MyJsonObjectRequest;
import com.winhands.service.FxService;
import com.winhands.service.FxService.onServiceCreate;
import com.winhands.service.SyncService;
import com.winhands.service.SyncService.onButtonChanged;
import com.winhands.settime.R;
import com.winhands.util.GetWeatherTask;
import com.winhands.util.L;
import com.winhands.util.NtpTrustedTime;
import com.winhands.util.PermissionUtils;
import com.winhands.util.SharePreferenceUtil;
import com.winhands.util.SharePreferenceUtils;
import com.winhands.util.T;
import com.winhands.util.TimeUtil;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class OldMainActivity extends StartActivity implements OnTouchListener,
		OnClickListener, onCityNameChanged, onButtonChanged, onServiceCreate,
		onSelected {

	/***
	 * 更新时间的消息值
	 */
	private static final int UPDATE_TIME=50;

	/**
	 * 滚动显示和隐藏menu时，手指滑动需要达到的速度。
	 */
	public static final int SNAP_VELOCITY = 200;

	/**
	 * 屏幕宽度值。
	 */
	private int screenWidth;

	/**
	 * menu最多可以滑动到的左边缘。值由menu布局的宽度来定，marginLeft到达此值之后，不能再减少。
	 */
	private int leftEdge;

	/**
	 * menu最多可以滑动到的右边缘。值恒为0，即marginLeft到达0之后，不能增加。
	 */
	private int rightEdge = 0;

	/**
	 * menu完全显示时，留给content的宽度值。
	 */
	private int menuPadding = 80;

	/**
	 * 主内容的布局。
	 */
	private View content;

	/**
	 * menu的布局。
	 */
	private View menu;

	/**
	 * menu布局的参数，通过此参数来更改leftMargin的值。
	 */
	private LinearLayout.LayoutParams menuParams;

	/**
	 * 记录手指按下时的横坐标。
	 */
	private float xDown;

	/**
	 * 记录手指移动时的横坐标。
	 */
	private float xMove;

	/**
	 * 记录手机抬起时的横坐标。
	 */
	private float xUp;

	/**
	 * menu当前是显示还是隐藏。只有完全显示或隐藏menu时才会更改此值，滑动过程中此值无效。
	 */
	private boolean isMenuVisible;

	/**
	 * 用于计算手指滑动的速度。
	 */
	private VelocityTracker mVelocityTracker;
	private ImageView lv_sync;
	private TextView local_date, local_time, net_date, net_time, cityName,
			settime_frequent_text,openweb;
	private Handler mHandler;

	private ImageView timeSetting, setting, news;
	private ImageView iv_s1, iv_s2;
	private ImageView iv_h1, iv_h2;
	private ImageView iv_m1, iv_m2;
	private LinearLayout citySetting, setTimeZone, setTimeFrequent,
			setBackground, notice, serverSetting,ntpServiceSetting;

	private EasyDialog easyDialog;
	private SharedPreferences sp;
	private List<TimeBean> timeZoneList;
	private List<TimeFrequent> timeFrequentList;
	private List<Backgound> backgroundList;
	private AlertDialog dlg;
	private SharePreferenceUtil mSpUtil;
	private int exitValue;
	private boolean timeoutFlag = false;
	private int timefrequent;

	private Button unCachedButton;

	private NtpTrustedTime ntpTrustedTime;
	/****
	 * 根据时区算得的偏移量
	 */
	private long timeZoneOffset;

	/***
	 * 所有tnp服务的名称
	 */
	public static final String[] NTP_NAMES ={"1.国家授时中心一","1.国家授时中心二","2.网络授时服务一","3.网络授时服务二","4.网络授时服务三","5.网络授时服务四"};
	/***
	 * 所有tnp服务的地址
	 */
	public static final String[] NTP_URLS ={"210.72.145.39",
			"210.72.145.47",
			"1.cn.pool.ntp.org",
			"2.cn.pool.ntp.org",
			"3.cn.pool.ntp.org",
			"0.cn.pool.ntp.org",
			};

	public static  final String DEFAULT_TNP= NTP_URLS[0];


	private String currntTnp=DEFAULT_TNP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sp = new SharePreferenceUtils(this).getSP();
		SelectCtiyActivity.ocnc = this;
		SyncService.obc = this;
		FxService.osc = this;
		WheatherActivity.ost = this;
		mSpUtil = BaseApplication.getInstance().getSharePreferenceUtil();

		initRoot();
		initList();
		initValues();
		initView();

		content.setOnTouchListener(this);
	//	startService(new Intent(this.getApplicationContext(),TsaService.class));
		mHandler = new Handler() {

			final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd");
			final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
			Date now = new Date();
			Date netDate = new Date();
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == UPDATE_TIME){
					//Calendar calendar = Calendar.getInstance();
					now.setTime(System.currentTimeMillis() - timeZoneOffset);
					setLocalAnitmation(now.getHours(), now.getMinutes(), now.getSeconds());
					local_time.setText(sdf2.format(now));
					local_date.setText(sdf1.format(now)
							+ " "
							+ invertWeekday(now.getDay()));

					if(ntpTrustedTime.hasCache()){
						netDate.setTime(ntpTrustedTime.currentTimeMillis() - timeZoneOffset);
					}else{
						L.d("==非网络时间");
						netDate.setTime(System.currentTimeMillis()-timeZoneOffset);
					}
					setNetClockAnimation(netDate.getHours(), netDate.getMinutes(), netDate.getSeconds());
					net_time.setText(sdf2.format(netDate));
					net_date.setText(sdf1.format(netDate)
							+" "
							+invertWeekday(netDate.getDay()));

					mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 200);


				}

				if (msg.what == 3) {
					settime_frequent_text.setText(getFrequentTime());
				}
				if (msg.what == 4) {
					content.setBackgroundResource(sp.getInt("background",
							R.drawable.bg_blue));
				}
				if (msg.what == 5) {
					WeatherInfo allWeather = BaseApplication.getInstance()
							.getAllWeather();
//					cityName.setText(allWeather.getCity());
					String s = "";
					if (!TextUtils.isEmpty(allWeather.getFeelTemp())) {
						s = s + allWeather.getFeelTemp() + "  ";
						mSpUtil.setSimpleTemp(allWeather.getFeelTemp()
								.replace("~", "/").replace("℃", "°"));// 保存一下温度信息，用户小插件
					} else if(!TextUtils.isEmpty(allWeather.getTemp0())) {
						s = s + allWeather.getTemp0() + "  ";
						mSpUtil.setSimpleTemp(allWeather.getTemp0()
								.replace("~", "/").replace("℃", "°"));
					}


					String climate = allWeather.getWeather0();
					if(!TextUtils.isEmpty(climate)) {
						s = s + climate + "  ";
						mSpUtil.setSimpleClimate(climate);// 保存一下天气信息，用户小插件
					}


					String time = allWeather.getIntime();
					mSpUtil.setTimeSamp(TimeUtil.getLongTime(time));// 保存一下更新的时间戳，记录更新时间
					if(!TextUtils.isEmpty(allWeather.getCity())) {
						s = s + allWeather.getCity() + " >";
						cityName.setText(s);
						cityName.setTextSize(17);
					}
				}
/*
				if(msg.what==21){
					ntpError();
				}*/
			}


		};
		new GetWeatherTask(mHandler, new City(null, mSpUtil.getCity(), null,
				mSpUtil.getPinyin(), null)).execute();

		if(!mSpUtil.getNtpService().equals("")){
			currntTnp = mSpUtil.getNtpService();
		}

		setNtpText();
		updateTimeZoneOffset();

		initNtpTime();

		startService(this.getApplicationContext());

	}

	/**
	 * 获取时间偏移量
	 * */
	private void updateTimeZoneOffset(){
		timeZoneOffset =((8 - sp.getInt("timezone", 8)) * 60 * 60 * 1000); //获取区偏移量
	}


	private void initTnp(){

			//dialog参数设置
			AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
			builder.setItems(NTP_NAMES, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					currntTnp = NTP_URLS[which];

					setNtpText();
					mSpUtil.setNtpService(currntTnp);
					scrollToContent();
					refreshTime();
					//mHandler.removeMessages(UPDATE_TIME);
				}
			});

			builder.setTitle("请选择NTP服务器"); //设置标题

			builder.create().show();
	}

	private void setNtpText(){

		TextView mTv= (TextView)findViewById(R.id.settime_service_txt);
		for(int i=0;i< NTP_URLS.length;i++){
			if(NTP_URLS[i].equals(currntTnp)){
				String tnpName= NTP_NAMES[i];
				mTv.setText(tnpName.substring(2,tnpName.length()));
			}
		}
	}





	private  void ntpError(){

				unCachedButton.setBackgroundResource(R.drawable.uncached_ntp_time);
				showNtpErrorTooltip();

	}
	private void ntpSucc(){
		mHandler.removeMessages(UPDATE_TIME);

		long  delay = 1000 - ntpTrustedTime.currentTimeMillis() % 1000;
		mHandler.sendEmptyMessageDelayed(UPDATE_TIME,delay);
		unCachedButton.setBackgroundResource(R.drawable.cached_ntp_time);
	}

	private void initList() {
		initTimeZoneList();
		initTimeFrequntList();
		initBackgroundList();
	}

	private void initTimeZoneList() {
		timeZoneList = new ArrayList<TimeBean>();
		timeZoneList.add(new TimeBean("(UTC-12:00）国际日期变更线西", -12, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		// timeZoneList.add(new TimeBean("西十一区", -11, sp.getInt("timezone",
		// 8)));
		timeZoneList.add(new TimeBean("(UTC-10:00）夏威夷时间 ", -10, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-09:00）阿拉斯加时间 ", -9, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-08:00）太平洋时间", -8, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-07:00）亚利桑那州", -7, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-07:00）山地区时间 - 奇瓦瓦、马萨特兰", -7, sp
				.getString("timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-07:00）山区地时间", -7, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-06:00）中部央标准时间", -6, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-06:00）墨西哥城", -6, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-05:00）东部时间", -5, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-05:00）波哥大", -5, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-05:00）利马", -5, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-04:00）拉巴斯", -4, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-04:00）亚松森", -4, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-04:00）圣地亚哥", -4, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-04:00）大西洋时间", -4, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-04:00）库亚巴", -4, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-03:00）卡宴，福塔雷萨", -3, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-03:00）布宜诺斯艾利斯", -3, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-03:00）萨尔瓦多", -3, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-03:00）蒙得维的亚", -3, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		// timeZoneList.add(new TimeBean("西二区", -2, sp.getString("timecity",
		// "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-01:00）亚述尔群岛", -1, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC-01:00）佛得角", -1, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+00:00）蒙罗维亚，雷克雅末克", 0, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+00:00）伦敦，都柏林", 0, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+01:00）巴黎，布鲁塞尔，马德里", 1, sp
				.getString("timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+01:00）温得和克", 1, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+01:00）华沙", 1, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+01:00）布拉柴维尔", 1, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+01:00）柏林，罗马，阿姆斯特丹", 1, sp
				.getString("timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+02:00）伊斯坦布尔", 2, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+02:00）哈拉雷", 2, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+02:00）大马士革", 2, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+02:00）安曼", 2, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+02:00）开罗", 2, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+02:00）的黎波里", 2, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+02:00）贝鲁特", 2, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+02:00）雅典", 2, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+03:00）莫斯科", 3, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+03:00）巴格达", 3, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+03:00）科威特", 3, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+04:00）巴库", 4, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+04:00）第比利斯", 4, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+05:00）塔什干", 5, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+06:00）达卡", 6, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+07:00）曼谷，河内，雅加达", 7, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+07:00）拉斯诺亚尔斯克", 7, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+08:00）伊尔库次克", 8, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+08:00）北京，香港", 8, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+08:00）台北", 8, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+08:00）新加坡", 8, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+09:00）东京", 9, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+09:00）首尔", 9, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+10:00）关岛", 10, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+10:00）莫尔兹比港", 10, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+10:00）墨尔本、悉尼", 10, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));
		// timeZoneList.add(new TimeBean("东十一区", 11, sp.getString("timecity",
		// "(UTC+08:00）北京，香港")));
		timeZoneList.add(new TimeBean("(UTC+12:00）斐济", 12, sp.getString(
				"timecity", "(UTC+08:00）北京，香港")));

	}

	private void initBackgroundList() {
		backgroundList = new ArrayList<Backgound>();
		backgroundList.add(new Backgound(R.drawable.bg_blue, "蓝色", sp.getInt(
				"background", R.drawable.bg_blue)));
		backgroundList.add(new Backgound(R.drawable.bg_green, "绿色", sp.getInt(
				"background", R.drawable.bg_blue)));
		backgroundList.add(new Backgound(R.drawable.bg_purple, "紫色", sp.getInt(
				"background", R.drawable.bg_blue)));
		backgroundList.add(new Backgound(R.drawable.bg_qing, "青色", sp.getInt(
				"background", R.drawable.bg_blue)));
		backgroundList.add(new Backgound(R.drawable.bg_yellow, "黄色", sp.getInt(
				"background", R.drawable.bg_blue)));
		backgroundList.add(new Backgound(R.drawable.bg_red, "红色", sp.getInt(
				"background", R.drawable.bg_blue)));
	}

	private void initTimeFrequntList() {
		timeFrequentList = new ArrayList<TimeFrequent>();
		timeFrequentList.add(new TimeFrequent("每隔24个小时", 60 * 24, sp.getInt(
				"timefrequent", 1 * 60)));
		timeFrequentList.add(new TimeFrequent("每隔12个小时", 60 * 12, sp.getInt(
				"timefrequent", 1 * 60)));
		timeFrequentList.add(new TimeFrequent("每隔1个小时", 60, sp.getInt(
				"timefrequent", 1 * 60)));
		timeFrequentList.add(new TimeFrequent("每隔30分钟", 30, sp.getInt(
				"timefrequent", 1 * 60)));
		timeFrequentList.add(new TimeFrequent("每隔15分钟", 15, sp.getInt(
				"timefrequent", 1 * 60)));
		timeFrequentList.add(new TimeFrequent("每隔5分钟", 5, sp.getInt(
				"timefrequent", 1 * 60)));
	}

	private void initRoot() {
		Process process = null;
		DataOutputStream os = null;
		exitValue = -1;
		try {
			String cmd = "chmod 777 " + getPackageCodePath();
			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			exitValue = process.waitFor();
			System.out.println(exitValue);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
	}

	private void initNtpTime(){

		ntpTrustedTime = NtpTrustedTime.getInstance(this);
		ntpTrustedTime.setTimeout(5000);
		ntpTrustedTime.setServer(currntTnp);
/*		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				refreshTime();
			}
		}, 500);*/
		refreshTimeUseFrequence();
	}
/*
	private Timer timer= new Timer();
	private TimerTask refreshTimerTask= new TimerTask() {
		@Override
		public void run() {
			//refreshTime();
		}
	};*/

	private void refreshTimeUseFrequence(){
		mHandler.removeCallbacks(refreshTimeRunnabel);
		mHandler.postDelayed(refreshTimeRunnabel,500);
	}

	Runnable refreshTimeRunnabel = new Runnable() {
		@Override
		public void run() {
			refreshTime();
			//int delay = sp.getInt("timefrequent",1)*60*1000;
			int delay = 60000;
			mHandler.postDelayed(this, delay);
		}
	};




	@Override
	protected void onStart() {
		super.onStart();
		//restartTimer();
/*
		if(ntpTrustedTime.hasCache()){
			mHandler.removeMessages(UPDATE_TIME);
			mHandler.sendEmptyMessage(UPDATE_TIME);
		}*/

		mHandler.removeMessages(UPDATE_TIME);
		mHandler.sendEmptyMessage(UPDATE_TIME);
		refreshTime();


	}

	ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			L.d("service ....");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	} ;

	private void startService(Context context){

		//  context.startService(new Intent(TimerService.class.getName()).setPackage("com.winhands.settime"));
		Intent intent = new Intent("aw.untas.com.timerappwidget.TimerService");

		context.bindService(intent,connection,BIND_AUTO_CREATE);
		//context.startService(intent);
	}

	private  void showNtpErrorTooltip(){

		easyDialog = new EasyDialog(this)
				.setLayoutResourceId(R.layout.uncached_ntp_tool_tip)
				.setBackgroundColor(0x7f0b0006)
				.setLocationByAttachedView(unCachedButton)
				.setGravity(EasyDialog.GRAVITY_TOP)
				.setAnimationAlphaShow(600, 0.0f, 1.0f)
				.setAnimationAlphaDismiss(600, 1.0f, 0.0f)
				.setTouchOutsideDismiss(true)
				.setMatchParent(false)
				.setMarginLeftAndRight(24, 24)
				.setOutsideColor(0x0)
				.show();
	}

	/***
	 * 重新从服务器获取时间
	 */
	private void refreshTime(){
		/*new Thread() {
			@Override
			public void run() {
				if(ntpTrustedTime.forceRefresh()) {//因为没有获取时间时，程序也在自动加载，所以引处不用加

				//	unCachedButton.setVisibility(View.INVISIBLE);
				//	mHandler.removeMessages(UPDATE_TIME);
				//	mHandler.sendEmptyMessage(UPDATE_TIME);
				}else{

					ntpError();
				}

			}
		}.start();*/
		RefressTimeTask refreshTimeTask = new RefressTimeTask();
		refreshTimeTask.execute();
	}


	 class RefressTimeTask extends AsyncTask<Void,Void, Boolean>{
		@Override
		protected void onPreExecute() {
			if(easyDialog !=null)
				easyDialog.close();
			unCachedButton.setVisibility(View.GONE);
			ntpTrustedTime.setServer(currntTnp);
			findViewById(R.id.cache_ntp_time_progressbar).setVisibility(View.VISIBLE);

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return ntpTrustedTime.forceRefresh();

		}

		@Override
		protected void onPostExecute(Boolean suc) {
			unCachedButton.setVisibility(View.VISIBLE);
			findViewById(R.id.cache_ntp_time_progressbar).setVisibility(View.GONE);
			if(easyDialog !=null)
				easyDialog.close();
			if(suc){
				ntpSucc();
			}else{
				ntpError();
			}

		}
	};




	public String invertWeekday(int weekday) {
		switch (weekday) {
		case 1:
			return "周一";

		case 2:
			return "周二";
		case 3:
			return "周三";
		case 4:
			return "周四";
		case 5:
			return "周五";
		case 6:
			return "周六";
		case 7:
			return "周日";
		}
		return "";
	}

	@Override
	protected void onStop() {
		mHandler.removeMessages(UPDATE_TIME);
		super.onStop();
	//	cancelTimer();
	}
	
	public void openWeb(View v){

		Uri uri = Uri.parse("http://www.time.ac.cn/");  
		Intent it = new Intent(Intent.ACTION_VIEW, uri);  
		startActivity(it);
	}
	@Override
	public void onClick(View v) {
		Intent mIntent;
		L.d("onClick", "click");
		switch (v.getId()) {

		case R.id.setting_timeservice:
				L.d("settiong serivce");
				initTnp();
				break;
		case R.id.citysetting:
			mIntent = new Intent(OldMainActivity.this, SelectCtiyActivity.class);
			startActivity(mIntent);
			break;
		case R.id.cityName:
			mIntent = new Intent(OldMainActivity.this, WheatherActivity.class);
			startActivity(mIntent);
			break;
		case R.id.timesetting:
			if (!timeoutFlag) {
				if (!setTime(ntpTrustedTime.currentTimeMillis())) {
					Toast.makeText(OldMainActivity.this, "应用未获得ROOT权限",
							Toast.LENGTH_SHORT).show();
					// Intent intent = new Intent("/");
					// ComponentName cm = new ComponentName(
					// "com.android.settings",
					// "com.android.settings.DateTimeSettingsSetupWizard");
					// intent.setComponent(cm);
					// intent.setAction("android.intent.action.VIEW");
					// startActivityForResult(intent, 0);
					// Intent floatIntent = new Intent(MainActivity.this,
					// FxService.class);
					// startService(floatIntent);
				}
			//	cancelTimer();
			//	wakeupTimer();
				//mTimerTaskNet.cancel();
				//initThread();
			} else {
				Toast.makeText(OldMainActivity.this, "网络连接失败,无法对时",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.setTimeZone:
			dlg = new AlertDialog.Builder(OldMainActivity.this).create();
			dlg.show();
			View view = LayoutInflater.from(OldMainActivity.this).inflate(
					R.layout.dialog, null);
			dlg.getWindow().setContentView(view);

			ListView lv = (ListView) view.findViewById(R.id.lv);
			lv.setAdapter(new TimeZoneAdapter(OldMainActivity.this, timeZoneList));
			break;
		case R.id.lv_sync:
			if (exitValue != 0) {
				T.show(getApplicationContext(), "该手机没有root权限",
						Toast.LENGTH_SHORT);
			} else {
				if (BaseApplication.isOpen) {
					lv_sync.setImageResource(R.drawable.sync_close);
					mIntent = new Intent(OldMainActivity.this, SyncService.class);
					stopService(mIntent);
				} else {
					lv_sync.setImageResource(R.drawable.sync_open);
					mIntent = new Intent(OldMainActivity.this, SyncService.class);
					startService(mIntent);

				}

			}
			break;
		case R.id.settime_frequent:
			dlg = new AlertDialog.Builder(OldMainActivity.this).create();
			dlg.show();
			View viewfrequent = LayoutInflater.from(OldMainActivity.this).inflate(
					R.layout.frequent, null);
			dlg.getWindow().setContentView(viewfrequent);

			ListView lvfrequent = (ListView) viewfrequent.findViewById(R.id.lv);
			lvfrequent.setAdapter(new TimeFrequentAdapter(OldMainActivity.this,
					timeFrequentList));
			break;
		case R.id.background:
			dlg = new AlertDialog.Builder(OldMainActivity.this).create();
			dlg.show();
			View viewBackground = LayoutInflater.from(OldMainActivity.this)
					.inflate(R.layout.background, null);
			dlg.getWindow().setContentView(viewBackground);
			ListView lvBackground = (ListView) viewBackground
					.findViewById(R.id.lv);
			lvBackground.setAdapter(new BackgroundAdapter(OldMainActivity.this,
					backgroundList));
			break;
		case R.id.setting:
			scrollToMenu();
			break;
		case R.id.notice:
			dlg = new AlertDialog.Builder(OldMainActivity.this).create();
			dlg.show();
			View viewNotice = LayoutInflater.from(OldMainActivity.this).inflate(
					R.layout.notice, null);
			dlg.getWindow().setContentView(viewNotice);
			break;
		case R.id.serverSetting:
			dlg = new AlertDialog.Builder(OldMainActivity.this).create();
			dlg.show();
			dlg.getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			View viewServerSetting = LayoutInflater.from(OldMainActivity.this)
					.inflate(R.layout.serversetting, null);
			dlg.getWindow().setContentView(viewServerSetting);
			break;
		case R.id.news:
			mIntent = new Intent(OldMainActivity.this, NewsListActivity.class);
			startActivity(mIntent);
			break;
		case R.id.openweb:

			Uri uri = Uri.parse("http://www.time.ac.cn/");  
			Intent it = new Intent(Intent.ACTION_VIEW, uri);  
			startActivity(it);
			break;
		case R.id.uncached_ntp_time_button:
			refreshTime();
			break;
		default:
			break;
		}
	}

	private boolean setTime(long timezone) {
		try {
			PermissionUtils.requestPermission();
		}catch (Exception ex){
			L.d("获取权限错误");
			ex.printStackTrace();
		}

		return SystemClock.setCurrentTimeMillis(timezone);
	}

	@Override
	public void setCityName(City city) {
		cityName.setText(city.getName() + " >");
		new GetWeatherTask(mHandler, new City(null, city.getName(), null,
				city.getPinyin(), null)).execute();
	}


	/**
	 * 初始化一些关键性数据。包括获取屏幕的宽度，给content布局重新设置宽度，给menu布局重新设置宽度和偏移距离等。
	 */
	private void initValues() {
		WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		screenWidth = window.getDefaultDisplay().getWidth();
		content = findViewById(R.id.content);
		menu = findViewById(R.id.menu);
		menuParams = (LinearLayout.LayoutParams) menu.getLayoutParams();
		// 将menu的宽度设置为屏幕宽度减去menuPadding
		menuParams.width = screenWidth - menuPadding;
		// 左边缘的值赋值为menu宽度的负数
		leftEdge = -menuParams.width;
		// menu的leftMargin设置为左边缘的值，这样初始化时menu就变为不可见
		menuParams.leftMargin = leftEdge;
		// 将content的宽度设置为屏幕宽度
		content.getLayoutParams().width = screenWidth;

	}

	private void initView() {
		local_date = (TextView) content.findViewById(R.id.local_date);
		local_time = (TextView) content.findViewById(R.id.local_time);
		net_date = (TextView) content.findViewById(R.id.net_date);
		net_time = (TextView) content.findViewById(R.id.net_time);
		openweb = (TextView) content.findViewById(R.id.openweb);
		settime_frequent_text = (TextView) menu
				.findViewById(R.id.settime_frequent_text);
		ntpServiceSetting =(LinearLayout) menu.findViewById(R.id.setting_timeservice);

		citySetting = (LinearLayout) menu.findViewById(R.id.citysetting);
		setTimeZone = (LinearLayout) menu.findViewById(R.id.setTimeZone);
		setBackground = (LinearLayout) menu.findViewById(R.id.background);
		setTimeFrequent = (LinearLayout) menu
				.findViewById(R.id.settime_frequent);
		setBackground = (LinearLayout) menu.findViewById(R.id.background);
		notice = (LinearLayout) menu.findViewById(R.id.notice);
		serverSetting = (LinearLayout) menu.findViewById(R.id.serverSetting);
		cityName = (TextView) content.findViewById(R.id.cityName);
		timeSetting = (ImageView) content.findViewById(R.id.timesetting);
		lv_sync = (ImageView) menu.findViewById(R.id.lv_sync);
		setting = (ImageView) content.findViewById(R.id.setting);
		news = (ImageView) content.findViewById(R.id.news);

		unCachedButton = (Button) content.findViewById(R.id.uncached_ntp_time_button);

		iv_s1 = (ImageView) findViewById(R.id.s1);
		iv_h1 = (ImageView) findViewById(R.id.h1);
		iv_m1 = (ImageView) findViewById(R.id.m1);
		iv_s2 = (ImageView) findViewById(R.id.s2);
		iv_h2 = (ImageView) findViewById(R.id.h2);
		iv_m2 = (ImageView) findViewById(R.id.m2);
		content.setBackgroundResource(sp.getInt("background",
				R.drawable.bg_blue));
		settime_frequent_text.setText(getFrequentTime());
		if (BaseApplication.isOpen) {
			lv_sync.setImageResource(R.drawable.sync_close);
		} else {
			lv_sync.setImageResource(R.drawable.sync_close);
		}
		cityName.setText(sp.getString("cityName", "北京 >"));
		setTimeFrequent.setOnClickListener(this);
		setBackground.setOnClickListener(this);
		setting.setOnClickListener(this);
		lv_sync.setOnClickListener(this);
		timeSetting.setOnClickListener(this);
		setTimeZone.setOnClickListener(this);
		cityName.setOnClickListener(this);
		citySetting.setOnClickListener(this);
		ntpServiceSetting.setOnClickListener(this);
		serverSetting.setOnClickListener(this);
		notice.setOnClickListener(this);
		serverSetting.setOnClickListener(this);
		news.setOnClickListener(this);
		openweb.setOnClickListener(this);

		unCachedButton.setOnClickListener(this);
	}

	public String getFrequentTime() {
		if (sp.getInt("timefrequent", 1 * 60) == 15)
			return "每隔十五分钟设置一次";
		if (sp.getInt("timefrequent", 1 * 60) == 30)
			return "每隔三十分钟设置一次";
		if (sp.getInt("timefrequent", 1 * 60) == 60)
			return "每隔一个小时设置一次";
		if (sp.getInt("timefrequent", 1 * 60) == 60 * 12)
			return "每隔十二小时设置一次";
		if (sp.getInt("timefrequent", 1 * 60) == 60 * 24)
			return "每隔二十四小时设置一次";
		if (sp.getInt("timefrequent", 1 * 60) == 5)
			return "每隔五分钟设置一次";
		return "";
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		createVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 手指按下时，记录按下时的横坐标
			xDown = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			// 手指移动时，对比按下时的横坐标，计算出移动的距离，来调整menu的leftMargin值，从而显示和隐藏menu
			xMove = event.getRawX();
			int distanceX = (int) (xMove - xDown);
			if (isMenuVisible) {
				menuParams.leftMargin = distanceX;
			} else {
				menuParams.leftMargin = leftEdge + distanceX;
			}
			if (menuParams.leftMargin < leftEdge) {
				menuParams.leftMargin = leftEdge;
			} else if (menuParams.leftMargin > rightEdge) {
				menuParams.leftMargin = rightEdge;
			}
			menu.setLayoutParams(menuParams);
			break;
		case MotionEvent.ACTION_UP:
			// 手指抬起时，进行判断当前手势的意图，从而决定是滚动到menu界面，还是滚动到content界面
			xUp = event.getRawX();
			if (wantToShowMenu()) {
				if (shouldScrollToMenu()) {
					scrollToMenu();
				} else {
					scrollToContent();
				}
			} else if (wantToShowContent()) {
				if (shouldScrollToContent()) {
					scrollToContent();
				} else {
					scrollToMenu();
				}
			}
			recycleVelocityTracker();
			break;
		}
		return true;
	}

	/**
	 * 判断当前手势的意图是不是想显示content。如果手指移动的距离是负数，且当前menu是可见的，则认为当前手势是想要显示content。
	 * 
	 * @return 当前手势想显示content返回true，否则返回false。
	 */
	private boolean wantToShowContent() {
		return xUp - xDown < 0 && isMenuVisible;
	}

	/**
	 * 判断当前手势的意图是不是想显示menu。如果手指移动的距离是正数，且当前menu是不可见的，则认为当前手势是想要显示menu。
	 * 
	 * @return 当前手势想显示menu返回true，否则返回false。
	 */
	private boolean wantToShowMenu() {
		return xUp - xDown > 0 && !isMenuVisible;
	}

	/**
	 * 判断是否应该滚动将menu展示出来。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY，
	 * 就认为应该滚动将menu展示出来。
	 * 
	 * @return 如果应该滚动将menu展示出来返回true，否则返回false。
	 */
	private boolean shouldScrollToMenu() {
		return xUp - xDown > screenWidth / 2
				|| getScrollVelocity() > SNAP_VELOCITY;
	}

	/**
	 * 判断是否应该滚动将content展示出来。如果手指移动距离加上menuPadding大于屏幕的1/2，
	 * 或者手指移动速度大于SNAP_VELOCITY， 就认为应该滚动将content展示出来。
	 * 
	 * @return 如果应该滚动将content展示出来返回true，否则返回false。
	 */
	private boolean shouldScrollToContent() {
		return xDown - xUp + menuPadding > screenWidth / 2
				|| getScrollVelocity() > SNAP_VELOCITY;
	}

	/**
	 * 将屏幕滚动到menu界面，滚动速度设定为30.
	 */
	private void scrollToMenu() {
		new ScrollTask().execute(30);
	}

	/**
	 * 将屏幕滚动到content界面，滚动速度设定为-30.
	 */
	private void scrollToContent() {
		new ScrollTask().execute(-30);
	}

	/**
	 * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
	 * 
	 * @param event
	 *            content界面的滑动事件
	 */
	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	/**
	 * 获取手指在content界面滑动的速度。
	 * 
	 * @return 滑动速度，以每秒钟移动了多少像素值为单位。
	 */
	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}

	/**
	 * 回收VelocityTracker对象。
	 */
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}

	class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... speed) {
			int leftMargin = menuParams.leftMargin;
			// 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
			while (true) {
				leftMargin = leftMargin + speed[0];
				if (leftMargin > rightEdge) {
					leftMargin = rightEdge;
					break;
				}
				if (leftMargin < leftEdge) {
					leftMargin = leftEdge;
					break;
				}
				publishProgress(leftMargin);
				// 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
				sleep(20);
			}
			if (speed[0] > 0) {
				isMenuVisible = true;
			} else {
				isMenuVisible = false;
			}
			return leftMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... leftMargin) {
			menuParams.leftMargin = leftMargin[0];
			menu.setLayoutParams(menuParams);
		}

		@Override
		protected void onPostExecute(Integer leftMargin) {
			menuParams.leftMargin = leftMargin;
			menu.setLayoutParams(menuParams);
		}
	}

	/**
	 * 使当前线程睡眠指定的毫秒数。
	 *
	 * @param millis
	 *            指定当前线程睡眠多久，以毫秒为单位
	 */
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class TimeZoneAdapter extends BaseAdapter {

		List<TimeBean> list;
		private LayoutInflater inflater;

		public TimeZoneAdapter(Context context, List<TimeBean> objs) {
			inflater = LayoutInflater.from(context);
			list = objs;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TimeBean tb = list.get(position);
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item, null);
				viewHolder = new ViewHolder();
				viewHolder.name = (TextView) convertView.findViewById(R.id.tv);
				viewHolder.isSelect = (ImageView) convertView
						.findViewById(R.id.iv);
				viewHolder.llt = (LinearLayout) convertView
						.findViewById(R.id.lv_liner);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.name.setText(tb.timezone);
			if (tb.timeFlag) {
				viewHolder.isSelect.setImageResource(R.drawable.selected);
			} else {
				viewHolder.isSelect.setImageResource(R.drawable.notselected);
			}
			viewHolder.llt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setTime(new Date().getTime()
							- (sp.getInt("timezone", 8) - tb.timecode) * 60
							* 60 * 1000);
					updateTimeZoneOffset();

					Editor e = sp.edit();
					e.putInt("timezone", tb.timecode);
					e.putString("timecity", tb.timezone);
					e.commit();
					timeZoneList.clear();
					initTimeZoneList();
					dlg.dismiss();
				}
			});
			return convertView;
		}

		class ViewHolder {
			private LinearLayout llt;
			private TextView name;
			private ImageView isSelect;
		}

	}

	class TimeFrequentAdapter extends BaseAdapter {

		List<TimeFrequent> list;
		private LayoutInflater inflater;

		public TimeFrequentAdapter(Context context, List<TimeFrequent> objs) {
			inflater = LayoutInflater.from(context);
			list = objs;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TimeFrequent tf = list.get(position);
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item, null);
				viewHolder = new ViewHolder();
				viewHolder.name = (TextView) convertView.findViewById(R.id.tv);
				viewHolder.isSelect = (ImageView) convertView
						.findViewById(R.id.iv);
				viewHolder.llt = (LinearLayout) convertView
						.findViewById(R.id.lv_liner);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.name.setText(tf.getFrequentString());
			if (tf.isFrequentFlag()) {
				viewHolder.isSelect.setImageResource(R.drawable.selected);
			} else {
				viewHolder.isSelect.setImageResource(R.drawable.notselected);
			}
			viewHolder.llt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Editor e = sp.edit();
					e.putInt("timefrequent", tf.getFrequentCode());
					e.commit();
					refreshTimeUseFrequence();
					timeFrequentList.clear();
					initTimeFrequntList();
					mHandler.sendEmptyMessage(3);
					dlg.dismiss();
				}
			});
			return convertView;
		}

		class ViewHolder {
			private LinearLayout llt;
			private TextView name;
			private ImageView isSelect;
		}

	}

	class BackgroundAdapter extends BaseAdapter {

		List<Backgound> list;
		private LayoutInflater inflater;

		public BackgroundAdapter(Context context, List<Backgound> objs) {
			inflater = LayoutInflater.from(context);
			list = objs;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Backgound bg = list.get(position);
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item, null);
				viewHolder = new ViewHolder();
				viewHolder.name = (TextView) convertView.findViewById(R.id.tv);
				viewHolder.isSelect = (ImageView) convertView
						.findViewById(R.id.iv);
				viewHolder.llt = (LinearLayout) convertView
						.findViewById(R.id.lv_liner);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.name.setText(bg.getBgString());
			if (bg.isBgFlag()) {
				viewHolder.isSelect.setImageResource(R.drawable.selected);
			} else {
				viewHolder.isSelect.setImageResource(R.drawable.notselected);
			}
			viewHolder.llt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Editor e = sp.edit();
					e.putInt("background", bg.getBgId());
					e.commit();
					backgroundList.clear();
					initBackgroundList();
					mHandler.sendEmptyMessage(4);
					dlg.dismiss();
				}
			});
			return convertView;
		}

		class ViewHolder {
			private LinearLayout llt;
			private TextView name;
			private ImageView isSelect;
		}

	}

	@Override
	public void onPressButton() {

	}

	/**
	 * 连续按两次返回键就退出
	 */
	private long firstTime;

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - firstTime < 3000) {
			finish();
		} else {
			firstTime = System.currentTimeMillis();
			T.showShort(this, R.string.press_again_exit);
		}
	}

	@Override
	public Date getNetDate() {
		return new Date(ntpTrustedTime.currentTimeMillis());
	}

	@Override
	public void onSelectedCity() {
		mHandler.sendEmptyMessage(5);

	}

	public void getNTPList() {
		String url = "";

		HashMap<String, String> params = new HashMap<String, String>();
		MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(
				Request.Method.POST, url, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						System.out.println(response);
						boolean flag = true;
						if (response.opt("result").equals("1")) {

						}

					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
					}
				});
		BaseApplication.getInstance().addToRequestQueue(jsonObjectRequest,
				"MainActivity");
	}


	public void setLocalAnitmation(int h, int m, int s){
		RotateAnimation animation_s1 = new RotateAnimation(
				s * 6, s * 6 + 6,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.81f);
		RotateAnimation animation_h1 = new RotateAnimation(
				h * 30+m/2,
				h * 30 +30,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.75f);
		RotateAnimation animation_m1 = new RotateAnimation(
				m * 6,
				m * 6 + 6 / 60,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.81f);
		animation_s1.setDuration(60 * 1000);
		animation_s1.setFillAfter(true);
		animation_h1.setDuration(3600 * 1000);
		animation_h1.setFillAfter(true);
		animation_m1.setDuration(60 * 60 * 60 * 1000);
		animation_m1.setFillAfter(true);
		iv_s1.clearAnimation();
		iv_h1.clearAnimation();
		iv_m1.clearAnimation();
		iv_s1.setAnimation(animation_s1);
		iv_h1.setAnimation(animation_h1);
		iv_m1.setAnimation(animation_m1);
	}

	public void setNetClockAnimation(int h,int m,int s){
		RotateAnimation animation_s2 = new RotateAnimation(
				s * 6, s * 6 + 6,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.81f);
		RotateAnimation animation_h2 = new RotateAnimation(
				h * 30+m/2,
				h * 30 +30,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.75f);
		RotateAnimation animation_m2 = new RotateAnimation(
				m * 6,
				m * 6 + 6 / 60,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.81f);
		animation_s2.setDuration(60 * 1000);
		animation_s2.setFillAfter(true);
		animation_h2.setDuration(3600 * 1000);
		animation_h2.setFillAfter(true);
		animation_m2.setDuration(60 * 60 * 60 * 1000);
		animation_m2.setFillAfter(true);
		iv_s2.clearAnimation();
		iv_h2.clearAnimation();
		iv_m2.clearAnimation();
		iv_s2.setAnimation(animation_s2);
		iv_h2.setAnimation(animation_h2);
		iv_m2.setAnimation(animation_m2);
	}

}