package com.winhands.bean;

public class TimeBean {
	public String timezone;
	public int timecode;
	public boolean timeFlag;
	public TimeBean(String s, int i,String temp) {
		timezone = s;
		timecode = i;
		timeFlag = isSelected(temp);
	}
	public boolean isSelected(String temp){
		if (timezone.equals(temp))
			return true;
		else
			return false;
	}
}