package com.cc.worldcupremind.view;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;


public class BaseFragment extends ListFragment {

	private static final String TAG = "BaseFragment";
	protected Context context;
	protected LayoutInflater mInflater;
	protected Resources resource;
	protected BaseAdapter adapter;  
	protected int fragmentIndex;
	protected Boolean isFragmentShow;
	protected Boolean isDataInit;
	protected MatchDataController controller;
	protected ListView listView;
	
	public BaseFragment(){
		context = null;
		mInflater = null;
		resource = null;
		adapter = null;
		isFragmentShow = false;
		isDataInit = false;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		controller = MatchDataController.getInstance();
		resource = context.getResources();
        mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setListAdapter(adapter);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView = getListView();
		listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}
	
	protected void refresh(){
		
		LogHelper.d(TAG, this.getClass().getName() + "::refresh");
		if(adapter != null){
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * When receive onUpdateDone or onInitDone need invoke this method
	 */
	protected void setData(){
		
		if(!isDataInit){
			LogHelper.d(TAG, this.getClass().getName() + "--The date init!");
			isDataInit = true;
		}
	}
	
	public void showData(){
		LogHelper.d(TAG, this.getClass().getName() + "::showData");
		LogHelper.d(TAG, String.format("isDataInit:%b, isFragmentShow:%b", 
				isDataInit, isFragmentShow));
		//only when fragment shown and data init done or need refresh to invoke refresh
		if(isDataInit && isFragmentShow){ 
			refresh();
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
	
	public Boolean hasFragmentShown(){
		return isFragmentShow;
	}

}
