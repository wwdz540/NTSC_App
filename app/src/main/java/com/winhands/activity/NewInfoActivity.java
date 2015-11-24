package com.winhands.activity;

import com.android.volley.toolbox.NetworkImageView;
import com.winhands.settime.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class NewInfoActivity extends Activity{
	NetworkImageView pic;
	TextView title,content;
	
			@Override
			protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_newitem);
				initView();
			}

			private void initView() {
				pic = (NetworkImageView) findViewById(R.id.newsPic);
				title = (TextView) findViewById(R.id.title);
				content = (TextView) findViewById(R.id.content);
				pic.setImageUrl(getIntent().getExtras().get("imgUrl")+"", BaseApplication.getInstance().getImageLoader());
				title.setText(getIntent().getExtras().get("title")+"");
				content.setText(getIntent().getExtras().get("content")+"");
			}
}
