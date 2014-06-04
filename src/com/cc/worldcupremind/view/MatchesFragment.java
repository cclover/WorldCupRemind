package com.cc.worldcupremind.view;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.R.id;
import com.cc.worldcupremind.R.layout;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.MatchesModel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MatchesFragment extends ListFragment {
	
	private static final String TAG = "MatchesFragment";
	private ListView matchesList;  
	private MatchesAdapter adapter;  
	private Context context;
	private SparseArray<MatchesModel> matchList;
	private MatchDataController controller;
	private LayoutInflater mInflater;
			
	public MatchesFragment(){
        adapter = new MatchesAdapter();  
	}
	
	public void setData(SparseArray<MatchesModel> list){
		matchList = list;
		adapter.refresh();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);
        matchesList = (ListView) view.findViewById(android.R.id.list);  
        setListAdapter(adapter);    
        return view;
    }
	
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        
        controller = MatchDataController.getInstance();
		context = this.getActivity();

        mInflater = LayoutInflater.from(context);
    }  

	
	class MatchesAdapter extends BaseAdapter{
		
		public MatchesAdapter(){

		}
		
		public void refresh(){
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {

			if(matchList == null)
				return 0;
			return matchList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.matches_item, null);
			     holder = new ViewHolder();
			     holder.group = (TextView)convertView.findViewById(R.id.txtGroup);
			     holder.time = (TextView)convertView.findViewById(R.id.txtTime);
			     holder.flag1 = (ImageView)convertView.findViewById(R.id.imgFlag1);
			     holder.team1 = (TextView)convertView.findViewById(R.id.txtTeam1);
			     holder.score = (TextView)convertView.findViewById(R.id.txtScore);
			     holder.team2 = (TextView)convertView.findViewById(R.id.txtTeam2);
			     holder.flag2 = (ImageView)convertView.findViewById(R.id.imgFlag2);
			     holder.remind = (CheckBox)convertView.findViewById(R.id.chkRemind);
			     convertView.setTag(holder);
			} else {
			     holder = (ViewHolder)convertView.getTag();
			}
			MatchesModel model = matchList.valueAt(position);
			holder.group.setText(model.getGroupName());
			holder.time.setText(model.getMatchTime().getTimeString());
			holder.team1.setText(controller.getTeamNationalName(model.getTeam1Code()));
			Drawable drawable1= controller.getTeamNationalFlag(model.getTeam1Code());
			if(drawable1 != null){
				holder.flag1.setImageDrawable(drawable1);
			}
			holder.team2.setText(controller.getTeamNationalName(model.getTeam2Code()));
			Drawable drawable2 = controller.getTeamNationalFlag(model.getTeam2Code());
			if(drawable2 != null){
				holder.flag2.setImageDrawable(drawable2);
			}
			holder.score.setText(":");
			return convertView;
		}
		
		
		class ViewHolder{
			TextView group;
			TextView time;
			ImageView flag1;
			TextView team1;
			TextView score;
			TextView team2;
			ImageView flag2;
			CheckBox remind;
		}
		
	}

}
