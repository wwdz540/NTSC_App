package com.winhands.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class News {
	private String title;
	private String createTime;
	private String content;
	private String img_path;
	private String resId;


	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImg_path() {
		return img_path;
	}

	public void setImg_path(String img_path) {
		this.img_path = img_path;
	}

	public News(){};
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public static News parseFromJson(JSONObject obj){
		News n = new News();
//		String c = obj.optString("content");
//		c.replaceAll("\n\n", "<br/>");
//		System.out.println(c);
		n.setContent(obj.optString("content"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		n.setCreateTime(sdf.format(new Date(Long.parseLong(obj.optString("create_time")))));
		n.setImg_path("http://120.24.64.153:8080"+obj.optString("img_path"));
		n.setResId(obj.optString("id"));
		n.setTitle(obj.optString("title"));
		return n;
		
	}
}
