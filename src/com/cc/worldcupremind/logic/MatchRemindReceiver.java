package com.cc.worldcupremind.logic;

import com.cc.worldcupremind.common.LogHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class MatchRemindReceiver extends BroadcastReceiver {

	private static final String TAG = "MatchRemindReceiver";
	private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";  
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equals(ACTION_BOOT)){
			LogHelper.d(TAG, "Receive BOOT_COMPLETED..Register the alarm");
			
			
		} else if(intent.getAction().equals(MatchRemindHelper.ACTION_ALARM)){
			LogHelper.d(TAG, "Receive the match alarm");
		}
	}
}
