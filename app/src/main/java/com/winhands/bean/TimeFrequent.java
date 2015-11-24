package com.winhands.bean;

public class TimeFrequent {
	private String frequentString;
	private int frequentCode;
	private boolean frequentFlag;

	public TimeFrequent(String frequentString, int frequentCode, int curruntCode) {
		this.frequentCode = frequentCode;
		this.frequentString = frequentString;
		this.frequentFlag = isSelected(curruntCode);
	}

	public boolean isFrequentFlag() {
		return frequentFlag;
	}

	public void setFrequentFlag(boolean frequentFlag) {
		this.frequentFlag = frequentFlag;
	}

	public String getFrequentString() {
		return frequentString;
	}

	public void setFrequentString(String frequentString) {
		this.frequentString = frequentString;
	}

	public int getFrequentCode() {
		return frequentCode;
	}

	public void setFrequentCode(int frequentCode) {
		this.frequentCode = frequentCode;
	}

	public boolean isSelected(int curruntCode) {
		if (curruntCode == frequentCode)
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return "TimeFrequent [frequentString=" + frequentString
				+ ", frequentCode=" + frequentCode + ", frequentFlag="
				+ frequentFlag + "]";
	}
}
