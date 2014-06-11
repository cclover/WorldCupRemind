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
	private final static String NEWS_URL = "http://3g.163.com/ntes/special/00340H1J/worldcup2014.html?from=index";
  
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
