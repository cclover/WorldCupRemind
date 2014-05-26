package com.cc.worldcupremind.common;

import android.util.Log;

/*
 * This class use to print log
 */
public class LogHelper {
	
	public static int LEVEL_E = 0;
	public static int LEVEL_W = 1;
	public static int LEVEL_D = 2;
	public static int LEVEL_I = 3;
	public static int LEVEL_V = 4;
	private static Boolean isEnable = true;
	private static int level = LEVEL_V;
	
	
	public static void setLogLevel(int lev){
		if(level >= LEVEL_E && level <= LEVEL_V)
			level = lev;
	}
	
	public static void v(String TAG, String msg){
		if(isEnable && level >=  LEVEL_V){
			Log.v(TAG, msg);	
		}
	}
	
	public static void i(String TAG, String msg){
		if(isEnable && level >= LEVEL_I){
			Log.i(TAG, msg);	
		}
	}
	
	public static void d(String TAG, String msg){
		if(isEnable && level >= LEVEL_D){
			Log.d(TAG, msg);	
		}
	}
	
	public static void w(String TAG, String msg){
		if(isEnable && level >= LEVEL_W){
			Log.w(TAG, msg);	
		}
	}
	
	public static void e(String TAG, String msg){
		if(isEnable && level >= LEVEL_E){
			Log.e(TAG, msg);	
		}
	}
	
	public static void e(String TAG, Exception e){
		if(isEnable && level >= LEVEL_E){
			Log.e(TAG, "[Exception]" + e.getMessage());	
			e.printStackTrace();
		}
	}
}
