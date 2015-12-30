package com.winhands.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class NetUtil {
	public static final int NETWORN_NONE = 0;
	public static final int NETWORN_WIFI = 1;
	public static final int NETWORN_MOBILE = 2;

	public static int getNetworkState(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);


		NetworkInfo networkInfo;
		// Wifi
		networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(networkInfo!=null){
			State state = networkInfo.getState();
			if (state == State.CONNECTED || state == State.CONNECTING) {
				return NETWORN_WIFI;
			}
		}


			// 3G
		networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(networkInfo!=null) {
			State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.getState();
			if (state == State.CONNECTED || state == State.CONNECTING) {
				return NETWORN_MOBILE;
			}
		}

		return NETWORN_NONE;
	}
}
