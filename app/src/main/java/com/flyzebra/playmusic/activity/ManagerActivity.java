package com.flyzebra.playmusic.activity;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.flyzebra.playmusic.R;
import com.flyzebra.playmusic.activity.ManagerBaseAdapter.CallbackToActivity;
import com.flyzebra.playmusic.sqlite.DBHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("InflateParams") public class ManagerActivity extends Activity implements CallbackToActivity{
	private MyApp myapp;
	private final int MUSICPLAY = 1;
	private TextView manager_tv01=null;
	private TextView manager_tv02=null;
	private TextView manager_tv03=null;
	private TextView manager_tv04=null;
	private ListView listview =null;
	private List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
	private ManagerBaseAdapter baseadapter=null;
	private Intent intent=null;
	private DBHelper db;
	
//	private final static String MAIN_ACTION_BROADCAST_EXIT = "BROADCAST_MAIN_EXIT";
	
	/* AboutActivity返回类型 */
	private final int RESULT_ABOUT_MYLOVE = 12;
	private final int RESULT_ABOUT_SDCARD = 13;
	private int lovecount = 0;
	private int sdcardcount = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		myapp=(MyApp) getApplication();
		intent=getIntent();
		super.onCreate(savedInstanceState);
		/* 去掉标题栏 *//* 设置全屏显示 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (myapp.getWindowmode()==2){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		setContentView(R.layout.activity_manager); 
		manager_tv01=(TextView) findViewById(R.id.manager_tv01);
		manager_tv02=(TextView) findViewById(R.id.manager_tv02);
		manager_tv03=(TextView) findViewById(R.id.manager_tv03);
		manager_tv04=(TextView) findViewById(R.id.manager_tv04);
		listview=(ListView) findViewById(R.id.manager_listview);
		manager_tv01.setOnClickListener(ListenerTextViewOnClick);
		manager_tv02.setOnClickListener(ListenerTextViewOnClick);
		manager_tv03.setOnClickListener(ListenerTextViewOnClick);
		listview.setOnItemClickListener(ListenerListViewItemOnClick);
		db = new DBHelper(this);
		lovecount = db.QueryMyloveSdcard(list);
		list.clear();
		sdcardcount = db.QueryAllSdcard(list);
		for(int i=0;i<list.size();i++)
		{
			list.get(i).put("NUMB", i);
			list.get(i).put("DELE", false);
		}
		if (baseadapter == null) {
			baseadapter = new ManagerBaseAdapter(this, list, R.layout.listviewmanager, 
					new String[] { "NAME","DELE", "LOVE","NUMB"}, 
					new int[] { R.id.manager_lv_tv01, R.id.manager_tv04, R.id.manager_lv_but2, R.id.manager_lv_tv02,},this);
		}
		listview.setAdapter(baseadapter);		
		manager_tv04.setText("一共"+sdcardcount+"首歌曲，已设为收藏歌曲"+lovecount+"首。");
	}	
	
	@Override
	protected void onStart() {
		myapp.setHideManagerActivity(false);		
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		myapp.setHideManagerActivity(true);
		if (myapp.getPlaystate() == MUSICPLAY || myapp.isListenphone()) {			
			Intent intent = new Intent(ManagerActivity.this, MusicService.class);
			startService(intent);
		}
//		else{
//			Intent intentexit = new Intent();
//			intentexit.setAction(MAIN_ACTION_BROADCAST_EXIT);
//			sendBroadcast(intentexit);
//			finish();
//		}
		super.onStop();
	}


	@Override
	public void onBackPressed() {
		myapp.setHideManagerActivity(true);
		finish();
		overridePendingTransition(0, 0);
	}
	private OnClickListener ListenerTextViewOnClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.manager_tv01:
				setResult(RESULT_ABOUT_SDCARD,intent);	
				myapp.setHideManagerActivity(true);
				finish();
				overridePendingTransition(0, 0);				
				break;
			case R.id.manager_tv02:
				setResult(RESULT_ABOUT_MYLOVE,intent);	
				myapp.setHideManagerActivity(true);
				finish();
				overridePendingTransition(0, 0);				
				break;
			case R.id.manager_tv03:
				break;
			}			
		}};
		
	private OnItemClickListener ListenerListViewItemOnClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				final long arg3) {
			final String path=(String) list.get((int) arg3).get("PATH");
			final File f = new File(path);
			String strfilesize;
						
			LayoutInflater factory = LayoutInflater.from(ManagerActivity.this);
			final View dlgview = factory.inflate(R.layout.dialogfile,null);
			TextView tv1=(TextView)dlgview.findViewById(R.id.df_filename);
			TextView tv2=(TextView)dlgview.findViewById(R.id.df_filesize);
			TextView tv3=(TextView)dlgview.findViewById(R.id.df_filetime);
			TextView tv4=(TextView)dlgview.findViewById(R.id.df_filepath);
			AlertDialog.Builder builder = new AlertDialog.Builder(ManagerActivity.this);
			builder.setTitle("歌曲文件管理");
			builder.setView(dlgview);			
			if (f.exists()) {
				builder.setPositiveButton("取消",new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});	
				builder.setNegativeButton("试听",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which) {
								if(myapp.getMediaplayer()==null){
									myapp.setMediaplayer(new MediaPlayer());
								}
								try {
									myapp.setCurrentplay((int) arg3);
									String name=f.getName();
									myapp.setPlayname(name.substring(0,name.length()-4));
									myapp.setPlaystate(MUSICPLAY);
									myapp.getMediaplayer().reset();
									myapp.getMediaplayer().setDataSource(path);
									myapp.getMediaplayer().prepare();
									myapp.getMediaplayer().start();
								} 
								catch (IOException e) {
									e.printStackTrace();
								}
								dialog.dismiss();
							}
						});
				builder.setNeutralButton("删除",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which) {
								list.get((int) arg3).put("DELE", true);
								db.DeleteOneSdcard(path);
								f.delete();
								sdcardcount--;
								if(1==(Integer)list.get((int) arg3).get("LOVE")) lovecount--;
								manager_tv04.setText("一共"+sdcardcount+"首歌曲，已设为收藏歌曲"+lovecount+"首。");
								baseadapter.notifyDataSetChanged();
								dialog.cancel();
							}
						});
			}
			else{
				builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});	
			}
			tv1.setText("文件名称： "+(String) list.get((int) arg3).get("NAME"));				
			if (f.exists()) {
				long filesize = f.length();
				if ((filesize >> 20) > 0) {
					strfilesize = (new DecimalFormat(".00").format((float) filesize / 1024 / 1024)) + "M";
				} else if ((filesize >> 10) > 0) {
					strfilesize = (new DecimalFormat(".00").format((float) filesize / 1024)) + "KB";
				} else {
					strfilesize = filesize + "Byte";
				}
				tv2.setTextColor(Color.WHITE);				
				tv2.setText("文件大小："+strfilesize);				
			} else {
				tv1.setTextColor(Color.RED);
				tv2.setTextColor(Color.RED);
				tv3.setTextColor(Color.RED);
				tv4.setTextColor(Color.RED);
				tv2.setText("文件大小：文件已被删除！");				
			}
			tv3.setText("播放时间："+(String) list.get((int) arg3).get("TIME"));	
			tv4.setText(Html.fromHtml("文件位置："+(String) list.get((int) arg3).get("PATH")
					+"<br><br><font color=red>警告：删除后不能恢复！</font><br>"));
			builder.show();
		}
	};

	@Override
	public void click(View v) {
		int position=(Integer) v.getTag();
		String path=(String) list.get(position).get("PATH");
		if (1==(Integer)(list.get(position).get("LOVE"))) {
			list.get(position).put("LOVE", 0);
			ContentValues cv = new ContentValues();
			cv.put("LOVE", 0);
			if (db.UpdateSdcard(cv, "PATH=?",new String[]{path})>0) {
				lovecount--;				
				baseadapter.notifyDataSetChanged();
				manager_tv04.setText("一共"+sdcardcount+"首歌曲，已设为收藏歌曲"+lovecount+"首。");
			}
		} else {
			list.get(position).put("LOVE", 1);
			ContentValues cv = new ContentValues();
			cv.put("LOVE", 1);
			if(db.UpdateSdcard(cv, "PATH=?",new String[]{path})>0){
				lovecount++;
				baseadapter.notifyDataSetChanged();
				manager_tv04.setText("一共"+sdcardcount+"首歌曲，已设为收藏歌曲"+lovecount+"首。");
			}
		}			
	}
}
