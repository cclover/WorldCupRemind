package com.cc.worldcupremind.logic;

public interface MatchDataListener {

	public void onInitDone(Boolean isSuccess);
	public void onCheckUpdateDone(Boolean isSuccess, Boolean haveNewVersion);
	public void onUpdateDone(Boolean isSuccess);
}
