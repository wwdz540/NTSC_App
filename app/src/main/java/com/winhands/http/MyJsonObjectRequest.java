package com.winhands.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.winhands.util.L;

/**
 * 专注POST形式提交，返回数据为JSONObject
 * 
 */
public class MyJsonObjectRequest extends Request<JSONObject> {

	/** Charset */
	private static final String PROTOCOL_CHARSET = "utf-8";

	/** ContentType */
	private static final String PROTOCOL_CONTENT_TYPE = String.format("application/x-www-form-urlencoded; charset=%s",
			PROTOCOL_CHARSET);

	/** 键值对形式参数 */
	private HashMap<String, String> mParams;
	private final Listener<JSONObject> mListener;

	public MyJsonObjectRequest(int method, String url, HashMap<String, String> params, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		super(method, url, errorListener);
		mListener = listener;
		mParams = params;

		for (String key : mParams.keySet()) {
			L.i("MyJsonObjectRequest", key + "=" + mParams.get(key));
		}
	}

	@Override
	protected void deliverResponse(JSONObject response) {
		mListener.onResponse(response);
	}

	@Override
	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {
			return Response.error(new ParseError(je));
		}
	}

	@Override
	public String getBodyContentType() {
		return PROTOCOL_CONTENT_TYPE;
	}

	@Override
	public byte[] getBody() {
		return encodeParameters(mParams, PROTOCOL_CHARSET);
	}
}