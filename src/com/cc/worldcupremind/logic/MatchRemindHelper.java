package com.cc.worldcupremind.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.MatchesModel;


public class MatchRemindHelper {

	private static final String TAG = "MatchRemindHelper";
	public static final String ACTION_ALARM = "com.cc.worldcupremind.alarm";  
	public static final String REMIND_MATCHES_NO = "NO";			/* Int */
	public static final String REMIND_MATCHES_STAGE = "Stage";		/* Int */
	public static final String REMIND_MATCHES_GROUP = "Group";		/* String */
	public static final String REMIND_MATCHES_TIME = "Time";		/* Date */
	public static final String REMIND_MATCHES_TEAM_1 = "Team1";		/* String */
	public static final String REMIND_MATCHES_TEAM_2 = "Team2";		/* String */
	
	public static void setAlarm(Context context, SparseArray<MatchesModel> remindList, ArrayList<Integer> cancelList){

		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		//Cancel alarm
		LogHelper.d(TAG, "Start to cancel alarm:" + String.valueOf(cancelList.size()));
		Intent intentCancel = new Intent(context, MatchRemindReceiver.class);
		intentCancel.setAction(ACTION_ALARM);
		for(int i : cancelList){
			PendingIntent pi = PendingIntent.getBroadcast(context, i, intentCancel, 0);
			am.cancel(pi); //Will cancel the alarm those have same pendingIntent(request code & intent)
			LogHelper.d(TAG, "Cancel the alarm match:" + String.valueOf(i));
		}
		
		LogHelper.d(TAG, "Start to set alarm:" + String.valueOf(remindList.size()));
		for(int i = 0; i < remindList.size(); i++){
			
			//set the match time
			MatchesModel match = remindList.valueAt(i);
			if(match.getMatchTime().isOver()){
				 LogHelper.d(TAG, String.format("Ignore Alarm(Is Over): [%d][%s-%s][%s %s]",
	        				match.getMatchNo(), 
	        				MatchDataController.getInstance().getTeamNationalName(match.getTeam1Code()),
	        				MatchDataController.getInstance().getTeamNationalName(match.getTeam2Code()),
	        				match.getMatchTime().getDateString(),
	        				match.getMatchTime().getTimeString()));
				continue;
			}
			
			Calendar remindCalendar = match.getMatchTime().getRemindCalendar();

			//Create pending intent
		    Intent intent = new Intent(context, MatchRemindReceiver.class);
		    intent.setAction(ACTION_ALARM);
		    intent.putExtra(REMIND_MATCHES_NO, match.getMatchNo());
		    intent.putExtra(REMIND_MATCHES_STAGE, match.getMatchStage());
		    intent.putExtra(REMIND_MATCHES_GROUP, match.getGroupName());
		    intent.putExtra(REMIND_MATCHES_TIME, match.getMatchTime().getDate());
		    intent.putExtra(REMIND_MATCHES_TEAM_1, match.getTeam1Code());
		    intent.putExtra(REMIND_MATCHES_TEAM_2, match.getTeam2Code());
		    
		    PendingIntent pi = PendingIntent.getBroadcast(context, match.getMatchNo(), intent,0);
		    am.set(AlarmManager.RTC_WAKEUP, remindCalendar.getTimeInMillis(), pi);
	        LogHelper.d(TAG, String.format("Set Alarm: [%d][%s-%s][%s %s %s][%s][%d:%d]",
	        				match.getMatchNo(), 
	        				MatchDataController.getInstance().getTeamNationalName(match.getTeam1Code()),
	        				MatchDataController.getInstance().getTeamNationalName(match.getTeam2Code()),
	        				match.getMatchTime().getDateString(),
	        				match.getMatchTime().getTimeString(),
	        				match.getMatchTime().getWeekdayString(),
	        				TimeZone.getDefault().getID(),
	        				remindCalendar.get(Calendar.HOUR),
	        				remindCalendar.get(Calendar.MINUTE)));
		}
		LogHelper.d(TAG, "Set alarm done!");
	}
}
