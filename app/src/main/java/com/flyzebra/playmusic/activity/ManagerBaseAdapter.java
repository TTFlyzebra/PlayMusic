package com.flyzebra.playmusic.activity;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ManagerBaseAdapter extends BaseAdapter implements OnClickListener  {
	private List<HashMap<String,Object>> list = null;
	private String key[] = null;
	private int id[] = null;
	private int idListview;
	private LayoutInflater inflater = null;
	private CallbackToActivity mCallback;
	private class ViewHolder {
	    public TextView  tv1 = null;
	    public TextView  tv2 = null;
	    public Button ib2 = null;
	} 
	
	public ManagerBaseAdapter(Context context, List<HashMap<String, Object>> list,
			int idListview, String[] key, int[] id,CallbackToActivity mCallback){
		this.mCallback=mCallback;
		inflater = LayoutInflater.from(context);
		this.idListview = idListview;
		this.list = list;
		this.key = new String[key.length];
		this.id = new int[id.length];
		System.arraycopy(key, 0, this.key, 0, key.length);
		System.arraycopy(id, 0, this.id, 0, id.length);
	}
	
	/* 自定义接口，用于回调按钮点击事件到Activity*/
	public interface CallbackToActivity {
		public void click(View v);
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
			holder.tv2 = (TextView) convertView.findViewById(id[3]);
			holder.ib2 = (Button) convertView.findViewById(id[2]);					
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		/*holder.ib2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String path=(String) list.get(position).get("PATH");
				if (1==(Integer)(list.get(position).get(key[2]))) {
					list.get(position).put(key[2], 0);
					ContentValues cv = new ContentValues();
					cv.put("LOVE", 0);
					myapp.getDbhelper().UpdateSdcard(cv, "PATH=?",new String[]{path});
				} else {
					list.get(position).put(key[2], 1);
					ContentValues cv = new ContentValues();
					cv.put("LOVE", 1);
					myapp.getDbhelper().UpdateSdcard(cv, "PATH=?",new String[]{path});
				}	
				ManagerBaseAdapter.this.notifyDataSetChanged();
			}});*/
		
		/*用回调函数将按钮事件传回Activity*/
		holder.ib2.setOnClickListener(this);
		holder.ib2.setTag(position);
		
		int number = (Integer) list.get(position).get(key[3]) + 1;
		holder.tv2.setText(number+".");		
		holder.tv1.setText((String) list.get(position).get("NAME"));
		if((Boolean) list.get(position).get("DELE")==true){
			holder.tv1.setBackgroundColor(Color.parseColor("#444444"));
			holder.tv2.setBackgroundColor(Color.parseColor("#444444"));
			holder.tv1.setTextColor(Color.parseColor("#FF0000"));
			holder.tv2.setTextColor(Color.parseColor("#FF0000"));
			holder.ib2.setBackgroundColor(Color.parseColor("#444444"));
			holder.ib2.setTextColor(Color.parseColor("#FF0000"));
			holder.ib2.setText("已经删除");
		}
		else
		{			
			if((Integer)list.get(position).get("LOVE")==0){
				holder.ib2.setBackgroundColor(Color.parseColor("#0088FF"));
				holder.ib2.setText("收藏歌曲");
			}
			else
			{
				holder.ib2.setBackgroundColor(Color.parseColor("#00FFFF"));
				holder.ib2.setText("取消收藏");
			}
			holder.tv1.setBackgroundColor(Color.parseColor("#00FFAA"));
			holder.tv2.setBackgroundColor(Color.parseColor("#00FFAA"));
			holder.tv1.setTextColor(Color.parseColor("#000000"));
			holder.tv2.setTextColor(Color.parseColor("#000000"));
			holder.ib2.setTextColor(Color.parseColor("#000000"));
		}		
		return convertView;
	}
	
	/*用回调函数将按钮事件传回Activity*/
	@Override
	public void onClick(View v) {
		mCallback.click(v);
	}

}
