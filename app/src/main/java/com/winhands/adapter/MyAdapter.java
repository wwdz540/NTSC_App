package com.winhands.adapter;

import java.util.List;

import com.winhands.bean.TimeBean;
import com.winhands.settime.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


//class MyAdapter extends BaseAdapter {
//
//	List<TimeBean> list;
//	private LayoutInflater inflater;
//
//	public MyAdapter(Context context, List<TimeBean> objs) {
//		inflater = LayoutInflater.from(context);
//		list = objs;
//	}
//
//	@Override
//	public int getCount() {
//		return list.size();
//	}
//
//	@Override
//	public Object getItem(int position) {
//		return list.get(position);
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return position;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		final TimeBean tb = list.get(position);
//		ViewHolder viewHolder;
//		if (convertView == null) {
//			convertView = inflater.inflate(R.layout.item, null);
//			viewHolder = new ViewHolder();
//			viewHolder.name = (TextView) convertView.findViewById(R.id.tv);
//			viewHolder.llt = (LinearLayout) convertView
//					.findViewById(R.id.lv_liner);
//			convertView.setTag(viewHolder);
//		} else {
//			viewHolder = (ViewHolder) convertView.getTag();
//		}
//		viewHolder.name.setText(tb.timezone);
//		viewHolder.llt.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				setTime(new Date().getTime()
//						- (sp.getInt("timezone", 8) - tb.timecode) * 60
//						* 60 * 1000);
//				Editor e = sp.edit();
//				e.putInt("timezone", tb.timecode);
//				e.commit();
//				cancelTimer();
//				restartTimer();
//				dlg.dismiss();
//			}
//		});
//		return convertView;
//	}
//
//	class ViewHolder {
//		private LinearLayout llt;
//		private TextView name;
//	}
//
//}