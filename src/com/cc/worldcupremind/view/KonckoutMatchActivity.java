package com.cc.worldcupremind.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;

public class KonckoutMatchActivity extends Activity {

	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWebView = new WebView(this);
		// 支持javascript
		mWebView.getSettings().setJavaScriptEnabled(true);
		// 设置可以支持缩放
		mWebView.getSettings().setSupportZoom(true);
		// 设置出现缩放工具
		mWebView.getSettings().setBuiltInZoomControls(true);
		// 扩大比例的缩放
		mWebView.getSettings().setUseWideViewPort(true);
		// 自适应屏幕
		mWebView.getSettings()
				.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		File dir = getFilesDir();
		File file = new File(dir, "img.png");
		if (!file.exists()) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				InputStream ios = getAssets().open("img.png");
				byte[] buffer = new byte[1000];
				int size = ios.read(buffer);
				while (size > 0) {
					fos.write(buffer, 0, size);
					size = ios.read(buffer);
				}
				fos.flush();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String imgPath = Uri.fromFile(file).toString();
		String html = "<center><img src=\"" + imgPath + "\"></center>";
		mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", "");
		setContentView(mWebView);
	}
}
