package com.cc.worldcupremind.view;

import com.cc.worldcupremind.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class NewsFragment extends Fragment {
	
	private static final String TAG = "NewsFragment";
	private WebView myWebView;
	private final static String NEWS_URL = "http://m.baidu.com/from=1001560k/bd_page_type=1/ssid=0/uid=0/pu=usm%400%2Csz%401320_1001%2Cta%40iphone_2_4.4_3_537/baiduid=2683510BC40C3F9B1B105B7D76C797A9/w=0_10_2014%E4%B8%96%E7%95%8C%E6%9D%AF%E6%96%B0%E9%97%BB/t=iphone/l=3/tc?ref=www_iphone&lid=13474486478699937440&order=4&vit=osres&tj=www_normal_4_0_10&m=8&srd=1&cltj=cloud_title&dict=21&sec=39178&di=87075c1d2e706286&bdenc=1&nsrc=IlPT2AEptyoA_yixCFOxXnANedT62v3IEQGG_ztO1GjezJnthPXrZQRAEG3eBGiOGkv5wWC0e2VMbj4uQXEobq";
  
  	@Override
  	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
   
  		View view = inflater.inflate(R.layout.fragment_news, container, false);
	 	myWebView = (WebView)view.findViewById(R.id.webview);
	 	myWebView.getSettings().setJavaScriptEnabled(false);
	 	myWebView.setWebViewClient(new WebViewClient());
	  	myWebView.loadUrl(NEWS_URL);
      	return view;
  	}
  

  	@Override
  	public void onActivityCreated(Bundle savedInstanceState) {
  		super.onActivityCreated(savedInstanceState);
  		setRetainInstance(true);
  	}
  	
}
