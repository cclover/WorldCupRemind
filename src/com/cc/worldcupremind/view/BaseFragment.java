package com.cc.worldcupremind.view;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;


public abstract class BaseFragment extends ListFragment {

	private static final String TAG = "BaseFragment";
	protected Context context;
	protected LayoutInflater mInflater;
	protected Resources resource;
	protected BaseAdapter adapter;  
	protected int fragmentIndex;
	protected Boolean isFragmentShow;
	protected Boolean isDataInit;
	protected MatchDataController controller;
	protected ProgressBar progressBar;
	private Boolean isProgressShown;
	
	public BaseFragment(){
		context = null;
		mInflater = null;
		resource = null;
		adapter = null;
		isFragmentShow = false;
		isDataInit = false;
		progressBar = null;
		isProgressShown = true;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		LogHelper.d(TAG,  this.getClass().getName() + " onCreate");
		super.onCreate(savedInstanceState);
		context = getActivity();
		controller = MatchDataController.getInstance();
		resource = context.getResources();
        mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LogHelper.d(TAG,  this.getClass().getName() + " onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		LogHelper.d(TAG,  this.getClass().getName() + " onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		//Show progress bar if adapter has not been set
  		if(getListAdapter() == null){
  			setProcessShown(true);
  		}
	}
	
	public abstract BaseAdapter createAdapter();
	
	
	/**
	 * We can delay to create and set adapter to speed up the view launch
	 * (Move create adapter into setDataInit, and invoke setAdapter in setDat)
	 * 
	 * Then each fragment will set adapter when it first be visited.
	 * But it will increase the TAB switch time. So we delay the fragment laod data by sendMessageDelay in MainActivity
	 */
	public void setAdapter(){
		
		if(getListAdapter() == null){
			LogHelper.d(TAG, this.getClass().getName() + "::setAdapter");
			setListAdapter(adapter);
			//Hide progress bar if adapter  set
			setProcessShown(false);
		}
	}
	
	public void refresh(){
		
		LogHelper.d(TAG, this.getClass().getName() + "::refresh");
		if(adapter != null){
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * When tab switch to fragment need invoke this method
	 */
	public void showFragment(){
		if(!isFragmentShow){
			LogHelper.d(TAG, this.getClass().getName() + "::showFragment");
			isFragmentShow = true;
		}
	}
	
	/**
	 * When receive onInitDone need invoke this method
	 */
	public void setDataInit(){
		
		if(!isDataInit){
			LogHelper.d(TAG, this.getClass().getName() + "--The date init!");
			isDataInit = true;
			adapter = createAdapter();
		}
	}
	
	public void setProcessShown(Boolean isShow){
		if(progressBar != null){
			isProgressShown = isShow;
			if(isProgressShown){
				progressBar.setVisibility(View.VISIBLE);
			}else{
				progressBar.setVisibility(View.GONE);
			}
		}		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		LogHelper.d(TAG, this.getClass().getName() + "onStop");
	}
	
	public Boolean hasFragmentShown(){
		return isFragmentShow;
	}
	
	public Boolean isDataInit(){
		return isDataInit;
	}
}
