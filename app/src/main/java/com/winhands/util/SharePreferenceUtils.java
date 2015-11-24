package com.winhands.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtils {
	private SharedPreferences sp;
	private static final String SP_NAME = "ds";

	public SharePreferenceUtils(Context context) {
		sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
	}

	public SharedPreferences getSP() {
		return sp;
	}
}