package com.flyzebra.playmusic.activity;

import java.util.HashMap;
import java.util.List;



import com.flyzebra.playmusic.R;
import com.flyzebra.playmusic.sqlite.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainBaseAdapter extends BaseAdapter {
	private List<HashMap<String, Object>> list = null;
	private String key[] = null;
	private int id[] = null;
	private int idListview;
	private LayoutInflater inflater = null;
	private DBHelper db = null;
	private MyApp myapp=null;
	
	private class ViewHolder {
	    public TextView  tv1 = null; 
	    public TextView  tv2 = null;
	    public TextView  tv3 = null;
	    public ImageButton imgbut1 =null;
	    public RelativeLayout listline = null;
	} 

	public MainBaseAdapter(Context context, List<HashMap<String, Object>> list,
			int idListview, String[] key, int[] id,MyApp myapp) {
		this.myapp=myapp;
		inflater = LayoutInflater.from(context);
		this.idListview = idListview;
		this.list = list;
		this.key = new String[key.length];
		this.id = new int[id.length];
		System.arraycopy(key, 0, this.key, 0, key.length);
		System.arraycopy(id, 0, this.id, 0, id.length);
		db = new DBHelper(context);
	}	

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {		
		ViewHolder holder = new ViewHolder();
		if (convertView == null) {
			convertView = inflater.inflate(idListview, null);
			holder.tv1 = (TextView) convertView.findViewById(id[0]);
			holder.tv2 = (TextView) convertView.findViewById(id[1]);
			holder.tv3 = (TextView) convertView.findViewById(id[2]);
			holder.imgbut1 = (ImageButton) convertView.findViewById(id[3]);
			holder.listline=(RelativeLayout) convertView.findViewById(id[4]);			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		int number = (Integer) list.get(position).get(key[0]) + 1;
		holder.tv1.setText(number+".");
		holder.tv2.setText((String) (list.get(position).get(key[1])));
		holder.tv3.setText((String) (list.get(position).get(key[2])));
		if (1==(Integer)(list.get(position).get("LOVE"))) {
			holder.imgbut1.setBackgroundResource(R.drawable.m_ylove);
		} else {
			holder.imgbut1.setBackgroundResource(R.drawable.m_nlove);
		}
		/* 列表中的按钮事件监听 ,还没有添加保存喜欢听的歌列表的方法*/
		OnClickListener CheckOnClick=new OnClickListener(){			
			@Override			
			public void onClick(View v) {				
				String path=(String) list.get(position).get("PATH");
//				String name=(String) list.get(position).get("NAME");	
//				String time=(String) list.get(position).get("TIME");
//				cv.put("PATH", path);
//				cv.put("NAME", name);
//				cv.put("TIME", time);				
				if (1==(Integer)(list.get(position).get(key[3]))) {
					list.get(position).put(key[3], 0);
					ContentValues cv = new ContentValues();
					cv.put("LOVE", 0);
					db.UpdateSdcard(cv, "PATH=?",new String[]{path});
				} else {
					list.get(position).put(key[3], 1);
					ContentValues cv = new ContentValues();
					cv.put("LOVE", 1);
					db.UpdateSdcard(cv, "PATH=?",new String[]{path});
				}	
//				db.InsertSdcard(cv);
				MainBaseAdapter.this.notifyDataSetChanged();				
			}
		};
		holder.imgbut1.setOnClickListener(CheckOnClick);
		holder.tv3.setOnClickListener(CheckOnClick);
		
		
		/*设置当前播放的歌曲的项的背景颜色MusicService.playstate!=MusicService.MUSICNONE&&*/
		if((Integer)list.get(position).get(key[0])==myapp.getCurrentplay())		{
			holder.tv1.setTextColor(Color.YELLOW);
			holder.tv2.setTextColor(Color.YELLOW);			
			holder.listline.setBackgroundResource(R.drawable.listitembk1);
		}
		else
		{
			holder.tv1.setTextColor(Color.parseColor("#888800"));
			holder.tv2.setTextColor(Color.parseColor("#888888"));
			holder.listline.setBackgroundResource(R.drawable.listitembk0);
		}
		return convertView;
	}
}
