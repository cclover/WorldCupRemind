package com.cc.worldcupremind.common;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.wandoujia.ads.sdk.Ads;
import com.wandoujia.ads.sdk.loader.Fetcher;

public class AdsHelper {
	
	private static final String TAG = "AdsHelper";
	private static final String ADS_APP_ID = "100007839";
	private static final String ADS_SECRET_KEY = "92c2a12d7dedea6dbefdc882eb9ac800";
	private static final String TAG_LIST = "2f4d3a22bcfd9f59cfd8e1e06f1d62c1";
	private static final String TAG_INTERSTITIAL_FULLSCREEN = "7c616e9d3c444b37cea46e0397094018";
	private static final String TAG_INTERSTITIAL_WIDGET = "7c616e9d3c444b37cea46e0397094018";
	private Boolean isAdsInit;
	private Boolean showAds;
	private Boolean isAdsPreLoaded;
	private Context context;
	private ViewGroup adsWidgetContainer;
	private ExecutorService thread;
	private ADSListener adsListener;

	public AdsHelper(Context activity) {
		isAdsInit = false;
		isAdsPreLoaded = false;
		showAds = true;
		context = activity;
		adsListener = null;
		thread = Executors.newSingleThreadExecutor();
	}
	
	public void setAdsWidgetContainer(ViewGroup container){
		adsWidgetContainer = container;
		adsWidgetContainer.addView(Ads.showAppWidget(context, null, TAG_INTERSTITIAL_WIDGET, Ads.ShowMode.WIDGET, 
				new View.OnClickListener(){
			
					@Override
					public void onClick(View v) {
						adsWidgetContainer.setVisibility(View.GONE);
					}
		    	}));
	}
	
	public void setAdsListener(AdsHelper.ADSListener listener){
		adsListener = listener;
	}
	
	public Boolean isAdsPreLoad(){
		return isAdsPreLoaded;
	}
	
	public Boolean isAdsInited(){
		return isAdsInit;
	}
	
	public void setAdsStatus(Boolean enable){
		showAds = enable;
	}
	
	public void initAds(){
		
		LogHelper.d(TAG, "Init ADS");
		try {
			Ads.init(context, ADS_APP_ID, ADS_SECRET_KEY);
			isAdsInit = true;
			//load ADS
			loadAds();
		}catch (Exception e) {
			LogHelper.e(TAG, e);       
			if(adsListener != null){   	
				adsListener.onPerloaderDone();
			}
		}
	}
	
	public void loadAds(){

		LogHelper.d(TAG, "Load ADS");
		
		//load ADS
		thread.execute(new Runnable() {
			
			@Override
			public void run() {
				//preload
				Ads.preLoad(context, Fetcher.AdFormat.appwall, TAG_LIST); 
				Ads.preLoad(context, Fetcher.AdFormat.interstitial, TAG_INTERSTITIAL_FULLSCREEN);
				
				//Wait the interstitial preload done 
		        while (!Ads.isLoaded(Fetcher.AdFormat.interstitial, TAG_INTERSTITIAL_WIDGET)) {
		        	
		        	LogHelper.d(TAG, "Wait loading ads for a while...");
		            try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						LogHelper.e(TAG, e);
					}
		        }
		        
		        //Load done
		        isAdsPreLoaded = true;
		        if(adsListener != null){
		        	adsListener.onPerloaderDone();
		        }
			}
		});
	}
	
	public void showAppWall(){
		
		LogHelper.d(TAG, "showAppWall");
		try{
			if(showAds && Ads.isLoaded(Fetcher.AdFormat.appwall, TAG_LIST)){
				Ads.showAppWall(context, TAG_LIST);
			}
		}catch(Exception ex){
			LogHelper.e(TAG, ex);
		}
	}
	
	public void showAdsInWidget(){
		
		LogHelper.d(TAG, "showAdsInWidget");
		if(!showAds){
			return;
		}
		if(adsWidgetContainer == null){
			LogHelper.d(TAG, "adsWidgetContainer is null");
			return;
		}
		adsWidgetContainer.setVisibility(View.VISIBLE);
	}
	
	
	public void showAdsInFullScreen(){
		
		try{
			LogHelper.d(TAG, "showAdsInScreen");
			if(showAds && Ads.isLoaded(Fetcher.AdFormat.interstitial, TAG_INTERSTITIAL_FULLSCREEN)){
				Ads.showAppWidget(context, null, TAG_INTERSTITIAL_FULLSCREEN, Ads.ShowMode.FULL_SCREEN);
			}
		}catch(Exception ex){
			LogHelper.e(TAG, ex);
		}
	}
	
	public void showAdsInFullScreenRandom(){
		int count = (int)(Math.random()*10+1) ;
		if(count == 1 || count == 3 || count == 9 ||  count == 7){
			showAdsInFullScreen();
		}
	}
	
	public interface ADSListener{
		void onPerloaderDone();
		void onInitAdsFail();
	}
}
