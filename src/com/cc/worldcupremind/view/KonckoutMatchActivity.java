package com.cc.worldcupremind.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.cc.worldcupremind.common.LogHelper;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.RelativeLayout;

public class KonckoutMatchActivity extends Activity {

	private WebView mWebView;
	private RelativeLayout rootLayout;
	private static final String TAG = "KonckoutMatchActivity";
	private static final String DATA_SECOND_STAGE_PIC = "secondstage.jpg";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWebView = new WebView(this);
		// ֧��javascript
		mWebView.getSettings().setJavaScriptEnabled(false);
		// ���ÿ���֧������
		mWebView.getSettings().setSupportZoom(true);
		// ���ó������Ź���
		mWebView.getSettings().setBuiltInZoomControls(true);
		// ������������
		mWebView.getSettings().setUseWideViewPort(true);
		// ����Ӧ��Ļ
		mWebView.getSettings()
				.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		File dir = getFilesDir();
		File file = new File(dir, DATA_SECOND_STAGE_PIC);
		if (!file.exists()) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				InputStream ios = getAssets().open(DATA_SECOND_STAGE_PIC);
				byte[] buffer = new byte[1000];
				int size = ios.read(buffer);
				while (size > 0) {
					fos.write(buffer, 0, size);
					size = ios.read(buffer);
				}
				fos.flush();
				fos.close();
			} catch (Exception e) {
				LogHelper.e(TAG, e);
			}
		}
		String imgPath = Uri.fromFile(file).toString();
		String html = "<center><img src=\"" + imgPath + "\"></center>";
		mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", "");
		
		rootLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams params =  new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		rootLayout.addView(mWebView, params);
		setContentView(rootLayout);
	}
}
