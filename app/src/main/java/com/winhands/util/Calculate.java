package com.winhands.util;

import java.util.ArrayList;
import java.util.List;

import com.winhands.activity.BaseApplication;
import com.winhands.bean.Milliseconds;

public class Calculate {

	public static long getEstimateTime(long now) {
		long temp = 0;
		long eTime = 0;

		List<Milliseconds> list = BaseApplication.getInstance().getDB()
				.getTime();  //获取本地存储的北京时间，及获取北京时间的手机时间
		if (list.size() > 0) {
			for (Milliseconds m : list) {

				temp = temp + m.getNettime() - (m.getLocaltime()+8*60*60*1000);
			}//累加每次获取时间的本地时间与网络时间差
			temp = temp / list.size();//求平均时间差
				eTime = list.get(0).getNettime() + now
						- list.get(0).getLocaltime() + temp;//通过本地时间与时间差计算标准时间
		}
		return eTime;
	}

}
