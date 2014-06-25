package com.cc.worldcupremind.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class KonckoutMatchActivity extends Activity {

	private static final String TAG = "KonckoutMatchActivity";

	private WebView mWebView;
	private ProgressBar mProgressBar;
	private CreateImageReceive mReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LogHelper.d(TAG, "onCreate");
		
		setContentView(R.layout.activity_konckout);
		mWebView = (WebView)findViewById(R.id.secondstageView);
		mProgressBar = (ProgressBar)findViewById(R.id.secondstageProgress);
		
		//Init the web view
		mWebView.getSettings().setJavaScriptEnabled(false);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.setVisibility(View.INVISIBLE);
		
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
		unregisterReceiver(mReceiver);
	}
	
	private void loadSecondStageImage(){
		File file = getFileStreamPath(ImageCreator.DATA_SECOND_STAGE_IMAGE);
		if (!file.exists()) {
			LogHelper.d(TAG, "Waitting the iamge");
			mProgressBar.setVisibility(View.VISIBLE);
			MatchDataController.getInstance().makeSecondStageImage();
		}else{
			String imgPath = Uri.fromFile(file).toString();
			String html = "<center><img src=\"" + imgPath + "\"></center>";
			mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", "");
			LogHelper.d(TAG, "Load from local：" + imgPath);
			mWebView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
		}
	}
	
	
	class CreateImageReceive extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			LogHelper.d(TAG, "Create Image done, show it");
			loadSecondStageImage();
		}
	}

}
