package com.cc.worldcupremind.view;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.DataOperateHelper;
import com.cc.worldcupremind.common.ImageCreator;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.GroupStatistics;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.view.KonckoutMatchActivity.CreateImageReceive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

public class GroupFragment extends BaseFragment {

	private static final String TAG = "GroupFragment";
	private ArrayList<GroupStatistics> mGroupStaticsList;
	private static final int ITEM_TYPE_TITLE = 0;
	private static final int ITEM_TYPE_TEAM = 1;	
	private static final int GROUP_TEAM_COUNT = 4;
	private static final int TITLE_SPAN = GROUP_TEAM_COUNT + 1;
	
	private FrameLayout sencondStageLayout;
	private ProgressBar progressImage;
	private ImageView imgFullScreen;
	private ImageView imgSecondStage;
	private TextView txtImageFail;
	private TextView txtFullScreen;
	private CreateImageReceive receiver;

	public GroupFragment(){
		mGroupStaticsList = null;
		sencondStageLayout = null;
		progressImage = null;
		receiver = null;
	}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_group, container, false);
		listView = (ListView)view.findViewById(android.R.id.list);
		progressBar = (ProgressBar)view.findViewById(R.id.progress_load);
		progressImage = (ProgressBar)view.findViewById(R.id.progress_img_load);
		sencondStageLayout = (FrameLayout)view.findViewById(R.id.layoutSecondStage);
		txtImageFail = (TextView)view.findViewById(R.id.txtSecondFail);
		txtFullScreen = (TextView)view.findViewById(R.id.txtSecondFull);
		imgSecondStage = (ImageView)view.findViewById(R.id.imgSecondStage);
		imgFullScreen = (ImageView)view.findViewById(R.id.imgFullScreen);
		imgFullScreen.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LogHelper.d(TAG, "Click the full screen image");
				Intent intent = new Intent(getActivity(), KonckoutMatchActivity.class);
				startActivity(intent);
			}
		});
		super.onCreateView(inflater, container, savedInstanceState); 
        return view;
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(controller.getMatchStage() == MatchStage.STAGE_GROUP){
			sencondStageLayout.setVisibility(View.GONE);
		}else{
			imgFullScreen.setVisibility(View.GONE);
			txtFullScreen.setVisibility(View.GONE);
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(ImageCreator.ACTION_CRATEA_IAMGE_DONE);
		receiver = new CreateImageReceive();
		context.registerReceiver(receiver, filter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(receiver != null){
			context.unregisterReceiver(receiver);
		}
	}
	
	public void setData(ArrayList<GroupStatistics> groupStaticsData) {
		
		LogHelper.d(TAG, "GroupFragment::setData");
		mGroupStaticsList = groupStaticsData;
		if(controller.getMatchStage() != MatchStage.STAGE_GROUP){
			progressImage.setVisibility(View.VISIBLE);
			loadThumbnail();
		}
		super.setAdapter();
		super.refresh();
	}
	
	@Override
	public BaseAdapter createAdapter() {
		return new GropStaticsListAdapter();
	}
	
	private void loadThumbnail(){
		
		LogHelper.d(TAG, "loadThumbnail");
		if(DataOperateHelper.isLocalFileExist(context, ImageCreator.DATA_SECOND_STAGE_IMAGE)){
			try {
				InputStream imageStream = DataOperateHelper.loadFileFromLocal(context, ImageCreator.DATA_SECOND_STAGE_IMAGE);
				Bitmap thumbnail = BitmapFactory.decodeStream(imageStream);
				imgSecondStage.setImageBitmap(thumbnail);
				imgFullScreen.setVisibility(View.VISIBLE);
				txtFullScreen.setVisibility(View.VISIBLE);
	           } catch (Exception e) {
	        	   txtImageFail.setVisibility(View.VISIBLE);
	        	   LogHelper.e(TAG, e);
	           }
			progressImage.setVisibility(View.GONE);
		}else{
			MatchDataController.getInstance().makeSecondStageImage();
		}		
	}
	
	
	class CreateImageReceive extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getBooleanExtra(ImageCreator.KEY_CRATEA_IAMGE_DONE, false)){
				LogHelper.d(TAG, "Create Image done, show it");
				loadThumbnail();
			}else{
				LogHelper.w(TAG, "Fail to create the secondstage image");
				progressImage.setVisibility(View.GONE);
				 txtImageFail.setVisibility(View.VISIBLE);
			}
		}
	}
	
	class GropStaticsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (mGroupStaticsList == null) {
				return 0;
			}
			return mGroupStaticsList.size() + mGroupStaticsList.size()/GROUP_TEAM_COUNT;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			if((position) % TITLE_SPAN == 0){
				return ITEM_TYPE_TITLE;
			}
			return ITEM_TYPE_TEAM;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(getItemViewType(position) == ITEM_TYPE_TITLE){
				LogHelper.d(TAG, "ITEM_TYPE_TITLE");
				//Set title holder
				TextView txtGroup = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.group_title_item, null);
					txtGroup = (TextView)convertView.findViewById(R.id.key_group);
					convertView.setTag(txtGroup);
				}else{
					txtGroup = (TextView)convertView.getTag();
				}
				
				//Set title value
				txtGroup.setText(String.format(getResources().getString(R.string.str_stage_group),GetTitleGroupCode(position)));
			}else{
				//Set team holder
				ViewHolderTeam holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.group_statics_item, null);
					holder = new ViewHolderTeam();
					holder.flag = (ImageView)convertView.findViewById(R.id.flag);
					holder.value_team = (TextView)convertView.findViewById(R.id.value_team);
					holder.star = (ImageView)convertView.findViewById(R.id.star);
					holder.value_w = (TextView)convertView.findViewById(R.id.value_w);
					holder.value_d = (TextView)convertView.findViewById(R.id.value_d);
					holder.value_l = (TextView)convertView.findViewById(R.id.value_l);
					holder.value_gf = (TextView)convertView.findViewById(R.id.value_gf);
					holder.value_ga = (TextView)convertView.findViewById(R.id.value_ga);
					holder.value_pts = (TextView)convertView.findViewById(R.id.value_pts);
					convertView.setTag(holder);
				}else{
					holder = (ViewHolderTeam) convertView.getTag();
				}
				
				//Set value
				GroupStatistics item = getTeamInfo(position);
				Drawable drawable = controller.getTeamNationalFlag(item.getTeamCode());
				holder.flag.setImageDrawable(drawable);
				holder.value_team.setText(controller.getTeamNationalName(item.getTeamCode()));
				if(item.getPosition() <= 2 && item.getPoint() > 0){
					holder.star.setVisibility(View.VISIBLE);
				}else{
					holder.star.setVisibility(View.INVISIBLE);
				}
				holder.value_pts.setText(String.valueOf(item.getPoint()));
				holder.value_w.setText(String.valueOf(item.getWinCount()));
				holder.value_d.setText(String.valueOf(item.getDrawCount()));
				holder.value_l.setText(String.valueOf(item.getLoseCount()));
				holder.value_gf.setText(String.valueOf(item.getGoalFor()));
				holder.value_ga.setText(String.valueOf(item.getGoalAgainst()));
			}
			return convertView;
		}
		
		
		/**
		 * Get tht tilte group name by pos
		 */
		private String GetTitleGroupCode(int pos){
			
			int index = pos / TITLE_SPAN;
			switch(index){
				case 0:
					return "A";
				case 1:
					return "B";
				case 2:
					return "C";
				case 3:
					return "D";
				case 4:
					return "E";
				case 5:
					return "F";
				case 6:
					return "G";
				case 7:
					return "H";
				default:
					return "";
			}
		}
		
		/**
		 * Get the team by position
		 */
		private GroupStatistics getTeamInfo(int position) {
			
			int pos = position - (position/TITLE_SPAN +1) ; //pos in mGroupStaticsList
			
			int p = 0;
			int start = 0;
			int end = 0;
			if (pos >= 0 && pos <= 3) {
				start = 0;
				end = 3;
				p = pos + 1;
			} else if (pos >= 4 && pos <= 7) {
				start = 4;
				end = 7;
				p = pos - 3;
			} else if (pos >= 8 && pos <= 11) {
				start = 8;
				end = 11;
				p = pos - 7;
			} else if (pos >= 12 && pos <= 15) {
				start = 12;
				end = 15;
				p = pos - 11;
			} else if (pos >= 16 && pos <= 19) {
				start = 16;
				end = 19;
				p = pos - 15;
			} else if (pos >= 20 && pos <= 23) {
				start = 20;
				end = 23;
				p = pos - 19;
			} else if (pos >= 24 && pos <= 27) {
				start = 24;
				end = 27;
				p = pos - 23;
			} else if (pos >= 28 && pos <= 31) {
				start = 28;
				end = 31;
				p = pos - 27;
			}

			for (int i = start; i <= end; i++) {
				GroupStatistics item = mGroupStaticsList.get(i);
				if (item.getPosition() == p) {
					return item;
				}
			}
			return null;
		}
	}
		
	
	class ViewHolderTeam{
		ImageView flag;
		TextView value_team;
		ImageView star;
		TextView value_w;
		TextView value_d;
		TextView value_l;
		TextView value_gf;
		TextView value_ga;
		TextView value_pts;
	}
}
