package com.cc.worldcupremind.view;

import java.io.File;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.ImageCreator;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class KonckoutMatchActivity extends Activity {

	private static final String TAG = "KonckoutMatchActivity";

	private WebView mWebView;
	private ProgressBar mProgressBar;
	private TextView mTextFail;
	private CreateImageReceive mReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LogHelper.d(TAG, "onCreate");
		
		setContentView(R.layout.activity_konckout);
		mWebView = (WebView)findViewById(R.id.secondstageView);
		mProgressBar = (ProgressBar)findViewById(R.id.secondstageProgress);
		mTextFail = (TextView)findViewById(R.id.txtFail);
		
		//Init the web view
		mWebView.getSettings().setJavaScriptEnabled(false);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		//register
		IntentFilter filter = new IntentFilter();
		filter.addAction(ImageCreator.ACTION_CRATEA_IAMGE_DONE);
		mReceiver = new CreateImageReceive();
		registerReceiver(mReceiver, filter);
		
		//load image
		loadSecondStageImage();
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView.setVisibility(View.GONE);
		unregisterReceiver(mReceiver);
	}
	
	private void loadSecondStageImage(){
		File file = getFileStreamPath(ImageCreator.DATA_SECOND_STAGE_IMAGE);
		if (!file.exists()) {
			LogHelper.d(TAG, "Waitting the iamge");
			mProgressBar.setVisibility(View.VISIBLE);
			MatchDataController.getInstance().makeSecondStageImage();
		}else{
			//Clear the cache!!!
			mWebView.loadDataWithBaseURL(null, "","text/html", "utf-8",null);
			mWebView.clearCache(true);
			String imgPath = Uri.fromFile(file).toString();
			String html = "<center><img src=\"" + imgPath + "\"></center>";
			mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", "");
			LogHelper.d(TAG, "Load from local" + imgPath);
			mProgressBar.setVisibility(View.GONE);
			mTextFail.setVisibility(View.GONE);
		}
	}
	
	
	class CreateImageReceive extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getBooleanExtra(ImageCreator.KEY_CRATEA_IAMGE_DONE, false)){
				LogHelper.d(TAG, "Create Image done, show it");
				loadSecondStageImage();
			}else{
				mWebView.loadDataWithBaseURL(null, "","text/html", "utf-8",null);
				mWebView.clearCache(true);
				mProgressBar.setVisibility(View.GONE);
				mTextFail.setVisibility(View.VISIBLE);
				LogHelper.w(TAG, "Fail to create the secondstage image");
			}
		}
	}

}
