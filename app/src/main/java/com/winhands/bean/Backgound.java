package com.winhands.bean;

public class Backgound {
	private int bgId;
	private String bgString;
	private boolean bgFlag;

	public Backgound(int bgId, String bgString,int curruntBgId) {
		this.bgId = bgId;
		this.bgString = bgString;
		this.bgFlag = isSelected(curruntBgId);
	}

	public boolean isBgFlag() {
		return bgFlag;
	}

	public void setBgFlag(boolean bgFlag) {
		this.bgFlag = bgFlag;
	}

	public int getBgId() {
		return bgId;
	}

	public void setBgId(int bgId) {
		this.bgId = bgId;
	}

	public String getBgString() {
		return bgString;
	}

	public void setBgString(String bgString) {
		this.bgString = bgString;
	}
	public boolean isSelected(int curruntBgId){
		if(bgId == curruntBgId)
			return true;
		else 
			return false;
	}
}
