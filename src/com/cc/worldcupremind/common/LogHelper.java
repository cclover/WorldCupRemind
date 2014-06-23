package com.cc.worldcupremind.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

/*
 * This class use to print log
 */
public class LogHelper {
	
	public static int LEVEL_E = 0;
	public static int LEVEL_W = 1;
	public static int LEVEL_I = 2;
	public static int LEVEL_D = 3;
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
	
	public static void d(String TAG, String msg){
		if(isEnable && level >= LEVEL_D){
			Log.d(TAG, msg);	
		}
	}
	
	public static void i(String TAG, String msg){
		if(isEnable && level >= LEVEL_I){
			Log.i(TAG, msg);	
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
//			writeLog(msg);
		}
	}
	
	public static void e(String TAG, Exception e){
		if(isEnable && level >= LEVEL_E){
			Log.e(TAG, "[Exception]" + e.getMessage());	
			e.printStackTrace();
//			writeLog(e);
		}
	}
	

	private static void writeLog(String log){
		
		LogHelper.d("loghelper", "writeLog");
		File logFile = new File(Environment.getExternalStorageDirectory()+"/worldcupremind_crash_" + new Date().getTime() + ".log");
		FileWriter writer = null; 
		try {
			writer = new FileWriter(logFile);
			writer.write("==========================================\r\n");
			writer.write(log);
			writer.write("==========================================\r\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void writeLog(Exception ex){
		writeLog(getExceptionStack(ex));
	}
	
	
	private static String getExceptionStack(Exception e){
		
		StringBuffer bs = new StringBuffer();
        StackTraceElement[] a = e.getStackTrace();
        String format = "at: %s.%s(%s:%S)\r\n";
        bs.append("[Exception]: " + e.fillInStackTrace() + "\r\n"); 
        for (int i = 0; i < a.length; i++) {
        	bs.append(String.format(format, 
        			a[i].getClassName(),
        			a[i].getMethodName(),
        			a[i].getFileName(),
        			a[i].getLineNumber()));
        }
        return bs.toString();
	}
}
