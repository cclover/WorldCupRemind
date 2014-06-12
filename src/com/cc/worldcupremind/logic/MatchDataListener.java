package com.cc.worldcupremind.logic;

public interface MatchDataListener {

	public static final int UPDATE_STATE_CHECK_ERROR = 1;
	public static final int UPDATE_STATE_CHECK_NONE = 2;
	public static final int UPDATE_STATE_CHECK_NEW_APK = 3;
	public static final int UPDATE_STATE_UPDATE_START = 4;
	public static final int UPDATE_STATE_UPDATE_ERROR = 5;
	public static final int UPDATE_STATE_UPDATE_DONE = 6;
	
	public void onInitDone(Boolean isSuccess);
	public void onUpdateDone(int status, String appURL);
	public void onSetRemindDone(Boolean isSuccess);
	public void onTimezoneChanged();
	public void onLocalChanged();
	public void onResetDone(Boolean issBoolean);
}
