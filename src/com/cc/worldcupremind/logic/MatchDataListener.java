package com.cc.worldcupremind.logic;

public interface MatchDataListener {

	public void onInitDone(Boolean isSuccess);
	public void onUpdateDone(Boolean haveNewVersion, Boolean isSuccess);
	public void onSetRemindDone(Boolean isSuccess);
	public void onTimezoneChanged();
	public void onLocalChanged();
}
