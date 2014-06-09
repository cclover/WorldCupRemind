package com.cc.worldcupremind.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.text.format.DateUtils;

import com.cc.worldcupremind.common.LogHelper;

public class MatchDate {

	private static final String TAG = "DateHelper";
	private static final String DATE_FORMAT_24 = "yyyy-MM-ddHH:mm:ss";
	private static final int REMIND_AHEAD_MIN = -15;
	private static final String DEFAULT_TIME_ZONE_ID = "GMT+8"; 
	
	private Date date = null;
	private Context context = null;
	
	public Date covertString2Date(String dateString){	
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_24, Locale.CHINA);
			dateFormat.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIME_ZONE_ID));
			return dateFormat.parse(dateString);
		}
		catch (ParseException e)
		{
			LogHelper.e(TAG, e);
			return null;
		}
	}
	
	public MatchDate(Context context, String dateStr){
		this.date = covertString2Date(dateStr);
		this.context = context.getApplicationContext();
	}
	
	public Date getDate(){
		return date;
	}
	
	public Calendar getRemindCalendar(){
		Calendar remindCalendar = Calendar.getInstance();
		remindCalendar.setTime(date);
		remindCalendar.add(Calendar.MINUTE, REMIND_AHEAD_MIN);
		return remindCalendar;
	}
	
	public String getDateString(){
		if(date == null){
			return "";
		}
		return DateUtils.formatDateTime(context, date.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);  
	}
	
	public String getTimeString(){
		if(date == null){
			return "";
		}
		return DateUtils.formatDateTime(context, date.getTime(), DateUtils.FORMAT_SHOW_TIME);  
	}

	public String getWeekdayString(){
		if(date == null){
			return "";
		}
		return DateUtils.formatDateTime(context, date.getTime(), DateUtils.FORMAT_SHOW_WEEKDAY);  
	}
	
}
