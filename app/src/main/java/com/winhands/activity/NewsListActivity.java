package com.winhands.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.winhands.bean.News;
import com.winhands.bean.URLConfig;
import com.winhands.http.MyJsonArrayRequest;
import com.winhands.http.MyJsonObjectRequest;
import com.winhands.settime.R;
import com.winhands.util.L;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class NewsListActivity extends Activity{
	protected static final String TAG = "NewsListActivity";
	ListView lv;
	MyAdapter myAdpater;
	List<News> list;
	ImageView avatorImgBtn;
	static boolean isLogin;
	BaseApplication mBaseApplication;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listview);
		mBaseApplication = (BaseApplication) getApplication();

		lv = (ListView) findViewById(R.id.list);
		avatorImgBtn = (ImageView)findViewById(R.id.avator);
		list = new ArrayList<News>();
	//	getList();

		avatorImgBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isLogin) {
					Intent intent = new Intent(NewsListActivity.this, LoginActivity.class);
					startActivity(intent);
				} else {
					logOut();
					checkLogin();
					getList();
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		getList();
			checkLogin();
	}

	private  void logOut(){
		//BaseApplication mBaseApplication = (BaseApplication)getApplication();
		mBaseApplication.isLogin = false;
		//isLogin = false;

	}

	private void checkLogin(){
		list.clear();
		if(isLogin!=mBaseApplication.isLogin){
			//getList();
			isLogin = mBaseApplication.isLogin;

		}

	//	BaseApplication mBaseApplication = (BaseApplication)getApplication();

	//	L.d("mBaseApplication="+mBaseApplication.isLogin);
		if(mBaseApplication.isLogin){
			avatorImgBtn.setImageResource(R.drawable.avatar_on);
		}else{
			avatorImgBtn.setImageResource(R.drawable.avatar_off);
		}
		isLogin = mBaseApplication.isLogin;
	}

	private void getList() {
		list = new ArrayList<News>();
		String url = URLConfig.NewsURL;

		HashMap<String, String> params = new HashMap<String, String>();

		if(mBaseApplication.isLogin){
			url=URLConfig.LoginNewsUrl;
			params.put("loginName",mBaseApplication.loginName);
			params.put("password",mBaseApplication.loginPassword);
		}

		L.d("url="+url);

		MyJsonArrayRequest request = new MyJsonArrayRequest(Request.Method.POST, url, params,
				new Response.Listener<JSONArray>() {

					@Override
					public void onResponse(JSONArray response) {
						System.out.println(response);
						for(int i = 0 ;i<response.length();i++){
							try {
								JSONObject obj = (JSONObject)response.get(i);
								list.add(News.parseFromJson(obj));
								myAdpater = new MyAdapter(getApplicationContext(), list);
								lv.setAdapter(myAdpater);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
							System.out.println(arg0.getMessage());
							arg0.printStackTrace();
					}
				});
		BaseApplication.getInstance().addToRequestQueue(request, TAG);
		
	}
	class MyAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private List<News> mData;

		public MyAdapter(Context context, List<News> objs) {
			inflater = LayoutInflater.from(context);
			mData = objs;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final News obj = mData.get(position);
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.news_item, null);
				viewHolder = new ViewHolder();
				viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
				viewHolder.mCreateTime = (TextView) convertView.findViewById(R.id.createtime);
				viewHolder.select_layout = (LinearLayout) convertView.findViewById(R.id.select_layout);
				viewHolder.mImageView = (NetworkImageView) convertView.findViewById(R.id.news_image);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.mTitle.setText(obj.getTitle());
			viewHolder.mCreateTime.setText(obj.getCreateTime());
//			viewHolder.mImageView.setDefaultImageResId(defaultImage)
			viewHolder.mImageView.setImageUrl(obj.getImg_path(), BaseApplication.getInstance().getImageLoader());
			viewHolder.select_layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
						Intent mIntent = new Intent(NewsListActivity.this,NewInfoActivity.class);
						mIntent.putExtra("title", obj.getTitle());
						mIntent.putExtra("content", obj.getContent());
						mIntent.putExtra("imgUrl", obj.getImg_path());
						startActivity(mIntent);
				}
			});
			return convertView;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		class ViewHolder {
			private TextView mTitle;
			private TextView mCreateTime;
			private NetworkImageView mImageView;
			private LinearLayout select_layout;
		}
	}
}
