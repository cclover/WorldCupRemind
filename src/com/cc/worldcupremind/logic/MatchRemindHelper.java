package com.cc.worldcupremind.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.MatchesModel;


public class MatchRemindHelper {

	private static final String TAG = "MatchRemindHelper";
	private static final int AHEAD_MIN = -15;
	public static final String ACTION_ALARM = "com.cc.worldcupremind.alarm";  
	public static final String REMIND_MATCHES_NO = "NO";			/* Int */
	public static final String REMIND_MATCHES_STAGE = "Stage";		/* Int */
	public static final String REMIND_MATCHES_GROUP = "Group";		/* String */
	public static final String REMIND_MATCHES_MONTH = "Month";		/* String */
	public static final String REMIND_MATCHES_DAY = "Day";			/* String */
	public static final String REMIND_MATCHES_WEEK = "Week";		/* Int */
	public static final String REMIND_MATCHES_HOUR = "Hour";		/* String */
	public static final String REMIND_MATCHES_MIN = "Minute";		/* String */
	public static final String REMIND_MATCHES_TEAM_1 = "Team1";		/* String */
	public static final String REMIND_MATCHES_TEAM_2 = "Team2";		/* String */
	
	public void setAlarm(Context context, SparseArray<MatchesModel> remindList, ArrayList<Integer> cancelList){


		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		//Cancel alarm
		LogHelper.d(TAG, "Start to cancel alarm:" + String.valueOf(remindList.size()));
		Intent intentCancel = new Intent(context, MatchRemindReceiver.class);
		intentCancel.setAction(ACTION_ALARM);
		for(int i : cancelList){
			PendingIntent pi = PendingIntent.getBroadcast(context, i, intentCancel, 0);
			am.cancel(pi); //Will cancel the alarm those have same pendingIntent(request code & intent)
			LogHelper.d(TAG, "Cancel the alarm match:" + String.valueOf(i));
		}
		
		LogHelper.d(TAG, "Start to set alarm:" + String.valueOf(remindList.size()));
		TimeZone zone = TimeZone.getTimeZone("GMT+8");
		for(int i = 0; i < remindList.size(); i++){
			
			//set the match time
			MatchesModel match = remindList.valueAt(i);
			Calendar remindCalendar = Calendar.getInstance();
			remindCalendar.set(Calendar.YEAR, 2014);
			remindCalendar.set(Calendar.MONTH, Integer.parseInt(match.getMatchMonth()));
			remindCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(match.getMatchDay()));
			remindCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(match.getMatchHour()));
			remindCalendar.set(Calendar.MINUTE, 0);
			remindCalendar.set(Calendar.SECOND, 0);
			remindCalendar.setTimeZone(zone);
			
			//remind ahead of 15mins
			remindCalendar.add(Calendar.MINUTE, AHEAD_MIN);

			//Create pending intent
		    Intent intent = new Intent(context, MatchRemindReceiver.class);
		    intentCancel.setAction(ACTION_ALARM);
		    intent.putExtra(REMIND_MATCHES_NO, match.getMatchNo());
		    intent.putExtra(REMIND_MATCHES_STAGE, match.getMatchStage());
		    intent.putExtra(REMIND_MATCHES_GROUP, match.getGroupName());
		    intent.putExtra(REMIND_MATCHES_MONTH, match.getMatchMonth());
		    intent.putExtra(REMIND_MATCHES_DAY, match.getMatchDay());
		    intent.putExtra(REMIND_MATCHES_WEEK, match.getMatchWeek());
		    intent.putExtra(REMIND_MATCHES_HOUR, match.getMatchHour());
		    intent.putExtra(REMIND_MATCHES_TEAM_1, match.getMatchTeam1());
		    intent.putExtra(REMIND_MATCHES_TEAM_2, match.getMatchTeam2());
		    
		    PendingIntent pi = PendingIntent.getBroadcast(context, match.getMatchNo(), intent,0);
		    am.set(AlarmManager.RTC_WAKEUP, remindCalendar.getTimeInMillis(), pi);
	        Log.d(TAG, String.format("Set Alarm: [%d][%s-%s][%s]",
	        				match.getMatchNo(), match.getMatchTeam1(), match.getMatchTeam2(), remindCalendar.toString()));
		}
	}
}
