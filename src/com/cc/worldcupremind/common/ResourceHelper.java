package com.cc.worldcupremind.common;

import java.util.HashMap;

import com.cc.worldcupremind.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;


/**
 * 
 * Help to access the String and Drawable resource about the team
 *
 */
public class ResourceHelper {
	
	private static final String TAG = "ResourceHelper";
	private static final String IMAGE_PIEFIX = "t_";
	
	private HashMap<String, Integer> nationalNameMap;
	private HashMap<String, Integer> nationalImageMap;
	private Context context;
	private Resources res;
	
	/**
	 * Construct
	 */
	public ResourceHelper(Context appContext){
		nationalNameMap = new HashMap<String, Integer>();
		nationalImageMap = new HashMap<String, Integer>();
		context = appContext;
		res = context.getResources();
	}
	
	/**
	 * Init the resource 
	 * 
	 * @param count 
	 * The team number
	 * 
	 * @return true if success, otherwise false
	 * 
	 */
	public Boolean Init(int count){
		
		LogHelper.d(TAG, "Init resource");
		
		nationalNameMap.clear();
		nationalImageMap.clear();
		
		if(!loadNationalNameResourceMap() || !loadNationalImageResourceMap()){
			LogHelper.w(TAG, "Init resource failed!");
			return false;
		}
		
		if(nationalNameMap.size() != count || nationalImageMap.size() != count){
			LogHelper.w(TAG, "Load resource count error!");
			return false;
		}
		LogHelper.d(TAG, "Init resource success");
		return true;
	}
	
	/**
	 * Get the @String from resource
	 * 
	 * @param teamCode
	 * Team code
	 * 
	 * @return
	 * The Team national name
	 * 
	 * @throws
	 * @Resources.NotFoundException
	 */
	public String getStringRescourse(String teamCode){
		
		if(nationalNameMap.containsKey(teamCode)){
			int resourceID = nationalNameMap.get(teamCode);
			return res.getString(resourceID);	
		}
		throw new Resources.NotFoundException(teamCode);
	}
	
	/**
	 * Get the @Drawable from resource
	 * 
	 * @param resourceID
	 * Resource ID
	 * 
	 * @return
	 * The Drawable object in drawable folder
	 * 
	 */
	public Drawable getDrawableRescourse(String teamCode){
		
		if(nationalImageMap.containsKey(teamCode)){
			int resourceID = nationalImageMap.get(teamCode);
			return res.getDrawable(resourceID);	
		}
		return res.getDrawable(R.drawable.t_null);
	}
	
	public int getFlagResourceIDByCode(String teamCode){
		if(nationalImageMap.containsKey(teamCode)){
			return nationalImageMap.get(teamCode);
		}
		return R.drawable.t_null;	
	}
	
	public int getNaemResourceIDByCode(String teamCode){
		return nationalNameMap.get(teamCode);
	}
	
	/**
	 * Load the team's name id from resource
	 * 
	 * @return true if success, otherwise false
	 */
	private Boolean loadNationalNameResourceMap(){
		
		LogHelper.d(TAG, "loadNationalNameResourceMap");
		
		try{
			String[] nationalCodeList = res.getStringArray(R.array.national);
			for(String code : nationalCodeList){
				nationalNameMap.put(code, res.getIdentifier(code, "string", context.getPackageName()));
			}
		} catch (Exception e){
			LogHelper.e(TAG, e);
			return false;
		}
		return true;
	}
	
	
	/**
	 * Load the team's national flag image id from resource
	 * 
	 * @return true if success, otherwise false
	 */
	private Boolean loadNationalImageResourceMap(){
		
		LogHelper.d(TAG, "loadNationalImageResourceMap");
		
		try{
			String[] nationalCodeList = res.getStringArray(R.array.national);
			for(String code : nationalCodeList){
				nationalImageMap.put(code, res.getIdentifier(IMAGE_PIEFIX+code, "drawable", context.getPackageName()));
			}
		} catch (Exception e){
			LogHelper.e(TAG, e);
			return false;
		}
		return true;
	} 
	
	
	public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  


}
