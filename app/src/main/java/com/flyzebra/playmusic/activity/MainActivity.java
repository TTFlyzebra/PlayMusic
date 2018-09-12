package com.flyzebra.playmusic.activity;

import java.io.File; 
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import com.flyzebra.playmusic.R;
import com.flyzebra.playmusic.sqlite.DBHelper;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
//	private final static String TAG = "PLAYMUSIC";
	private MyApp myapp;
	
	private DBHelper db = new DBHelper(this);

	/* 媒体的播放状态playstate */
	private final int MUSICNONE = 0;
	private final int MUSICPLAY = 1;
	private final int MUSICPAUSE = 2;

	/* 歌曲播放顺序playmode */
	private final int PLAYLISTLOOP = 1;
	private final int PLAYRANDOM = 2;
	private final int PLAYSINGLELOOP = 3;
	private final int PLAYLISTEND = 4;

	/* 当前播放列表 */
	private final int LOVEMODE = 1;
	private final int SDCARDMODE = 2;
	private final int ABOUTMODE = 3;

	/* SetActivity返回类型 */
	private final int RESULT_SET_ID = 1;	
	private final int RESULT_SET_BACK = 2;
	private final int RESULT_SET_EXIT = 3;
	private final int RESULT_SET_RESET = 4;
	
	/* AboutActivity返回类型 */
	private final int RESULT_ABOUT_ID = 11;
	private final int RESULT_ABOUT_MYLOVE = 12;
	private final int RESULT_ABOUT_SDCARD = 13;
	private final int RESULT_ABOUT_EXIT=14;

	/* 广播 */
	private final static String MAIN_ACTION_BROADCAST_EXIT = "BROADCAST_MAIN_EXIT";
	private final static String MAIN_ACTION_BROADCAST_NEXTPLAY = "BROADCAST_MAIN_NEXTPLAY";
	private final static String MAIN_ACTION_BROADCAST_UPMUSIC="BROADCAST_MAIN_UPMUSIC";
	private final static String MAIN_ACTION_BROADCAST_UPPLAYLIST="BROADCAST_MAIN_UPPLAYLIST";
	private MainBroadcast broadcastreceiver = null;

	private final int HDMAIN_SK01 = 2;
	private final int HDMAIN_LVADD = 3;
	private final int HDMAIN_LVEND = 4;
	private final int HDMAIN_UPDBEND=5;
	private final int HDMAIN_LVCLEAR= 6;
	private final int HDMAIN_ADDOPEN = 7;
	

	private int position = 0;// 歌曲播放到哪个位置

	private ImageButton main_butplay = null;
	private ImageButton main_butfore = null;
	private ImageButton main_butnext = null;
	private ImageButton main_butstop = null;
	private ImageButton main_butsets = null;

	private TextView main_tv01 = null;
	private TextView main_tv02 = null;
	private TextView main_tv03 = null;
	private TextView main_tv04 = null;
	private TextView main_tv05 = null;
	private TextView main_tv06 = null;
	private LinearLayout main_line01 = null;
	private SeekBar main_sk01 = null;
	private ListView main_lv01 = null;
	private MainBaseAdapter baseadapter = null;
	private boolean exitstate = false;

	/* 线程控制安全 */
	private boolean TCSetSeekBar = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		myapp = (MyApp) getApplication();
		super.onCreate(savedInstanceState);
		/* 去掉标题栏 *//* 设置全屏显示 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (myapp.getWindowmode()==2) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		setContentView(R.layout.activity_main);		

		/* 注册广播 */
		broadcastreceiver = new MainBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MAIN_ACTION_BROADCAST_NEXTPLAY);
		filter.addAction(MAIN_ACTION_BROADCAST_UPPLAYLIST);
		filter.addAction(MAIN_ACTION_BROADCAST_UPMUSIC);
		filter.addAction(MAIN_ACTION_BROADCAST_EXIT);
		registerReceiver(broadcastreceiver, filter);

		/* 启动Service */
		StartMusicService();

		/* 获取组件关联数据 */
		main_butplay = (ImageButton) super.findViewById(R.id.main_butplay);
		main_butfore = (ImageButton) super.findViewById(R.id.main_butfore);
		main_butnext = (ImageButton) super.findViewById(R.id.main_butnext);
		main_butstop = (ImageButton) super.findViewById(R.id.main_butstop);
		main_butsets = (ImageButton) super.findViewById(R.id.main_butsets);
		main_lv01 = (ListView) super.findViewById(R.id.main_listview);
		main_tv01 = (TextView) super.findViewById(R.id.main_tv01);
		main_tv02 = (TextView) super.findViewById(R.id.main_tv02);
		main_tv03 = (TextView) super.findViewById(R.id.main_tv03);
		main_tv04 = (TextView) super.findViewById(R.id.main_tv04);
		main_tv05 = (TextView) super.findViewById(R.id.main_tv05);
		main_tv06 = (TextView) super.findViewById(R.id.main_tv06);
		main_sk01 = (SeekBar) super.findViewById(R.id.main_sk01);
		main_line01 = (LinearLayout) super.findViewById(R.id.main_line01);

		if (baseadapter == null) {
			baseadapter = new MainBaseAdapter(this, myapp.getPlaylist(),	R.layout.listviewmain, 
					new String[] { "NUMB", "NAME", "TIME", "LOVE" }, 
					new int[] { R.id.listtv01, R.id.listtv02, R.id.listtv03, R.id.listbt01, R.id.listline },
					myapp);
		}
		main_lv01.setAdapter(baseadapter);
		baseadapter.notifyDataSetChanged();
		
		myapp.setWaitlist(false);
		main_tv01.setText("正在加载歌曲列表!");		
		if (myapp.getPlaylist().size() < 1) {
			switch (myapp.getListmode()) {
			case ABOUTMODE:
			case LOVEMODE:
				// 读取数据库喜欢歌曲列表
				if (db.QueryMyloveSdcard(myapp.getPlaylist()) > 0) {
					main_tv01.setText("歌曲列表加载完毕.");
				} else {
					myapp.setListmode(SDCARDMODE);
					if (db.QueryAllSdcard(myapp.getPlaylist()) > 0) {
						main_tv01.setText("歌曲列表加载完毕!");
					} else {
						myapp.setWaitlist(true);
						new Thread(new FindSdcardMusic()).start();
					}
				}
				break;
			case SDCARDMODE:
				// 查找存储卡上的歌曲文件
				if (db.QueryAllSdcard(myapp.getPlaylist()) > 0) {
					main_tv01.setText("歌曲列表加载完毕!");
				} else {
					myapp.setWaitlist(true);
					new Thread(new FindSdcardMusic()).start();
				}
				break;
			}
		}
		/* 设置各组件监听事件 */
		main_butplay.setOnClickListener(ListenerImgButOnClick);
		main_butplay.setOnTouchListener(ListenerImgButOnTouch);
		main_butfore.setOnClickListener(ListenerImgButOnClick);
		main_butnext.setOnClickListener(ListenerImgButOnClick);
		main_butstop.setOnClickListener(ListenerImgButOnClick);
		main_butsets.setOnClickListener(ListenerImgButOnClick);

		main_lv01.setOnItemClickListener(ListenerListViewItemOnClick);
		main_sk01.setOnSeekBarChangeListener(ListenerSeekBarChange);
		main_tv01.setOnClickListener(ListenerTextViewOnClick);
		main_tv04.setOnClickListener(ListenerTextViewOnClick);
		main_tv05.setOnClickListener(ListenerTextViewOnClick);
		main_tv06.setOnClickListener(ListenerTextViewOnClick);
		OpenPlayFile();
//		Log.i(TAG, "Activity.onCreate() is finished!");
	}
	
//	
	
	/**
	 * 处理打开文件的形式进来的情况
	 */
	private void OpenPlayFile(){		
		Intent intent = getIntent();			
		String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {
			myapp.setListmode(SDCARDMODE);
			main_tv04.setTextColor(Color.parseColor("#FF00FF"));
			main_tv05.setTextColor(Color.parseColor("#770077"));
			main_tv06.setTextColor(Color.parseColor("#770077"));
			main_line01.setBackgroundResource(R.drawable.topbk1);
			myapp.getPlaylist().clear();
			db.QueryAllSdcard(myapp.getPlaylist());
			Uri uri = intent.getData();  
			final String path = Uri.decode(intent.getDataString()).substring(7);
			final String name = path.substring(path.lastIndexOf("/")+1,path.lastIndexOf("."));
			myapp.setPlayname(name);
			if (myapp.getMediaplayer() == null) {
				myapp.setMediaplayer(new MediaPlayer());
			}
			myapp.setPlaystate(MUSICPLAY);
			myapp.getMediaplayer().reset();			
			try {
				myapp.getMediaplayer().setDataSource(uri.toString());
				myapp.getMediaplayer().prepare();
				myapp.getMediaplayer().start();
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (myapp.isWaitlist() == true) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						int i = db.QueryOneInRowSdcard(path);
						if (i != -1) {
							myapp.setCurrentplay(i);
							ShowCurrentPlayMusic();
						} else {
							ContentValues cv = new ContentValues();
							int num = myapp.getMediaplayer().getDuration();
							int max = num / 1000;
							String time = null;
							if (max % 60 < 10) {
								time = max / 60 + ":0" + max % 60;
							} else {
								time = max / 60 + ":" + max % 60;
							}
							cv.put("PATH", path);
							cv.put("NAME", name);
							cv.put("TIME", time);
							db.InsertSdcard(cv);
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("PATH", path);
							map.put("NAME", name);
							map.put("TIME", time);
							map.put("NUMB", myapp.getPlaylist().size());
							Message msg = new Message();
							msg.obj = map;
							msg.what = HDMAIN_ADDOPEN;
							HandlerMain.sendMessage(msg);
						}
					}
				}).start();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
	     } 
	}
	
	@Override
	protected void onStart() {
		StartMusicService();
		myapp.setHideMainActivity(false);
		/* 在onStart中更新UI */
		switch (myapp.getListmode()) {
		case ABOUTMODE:
		case LOVEMODE:
			main_tv04.setTextColor(Color.parseColor("#770077"));
			main_tv05.setTextColor(Color.parseColor("#FF00FF"));
			main_tv06.setTextColor(Color.parseColor("#770077"));
			main_line01.setBackgroundResource(R.drawable.topbk2);
			break;
		case SDCARDMODE:
			main_tv04.setTextColor(Color.parseColor("#FF00FF"));
			main_tv05.setTextColor(Color.parseColor("#770077"));
			main_tv06.setTextColor(Color.parseColor("#770077"));
			main_line01.setBackgroundResource(R.drawable.topbk1);
			break;
		}
		if (myapp.isWaitlist())
		{
			main_tv01.setText("正在扫描手机SDCARD中的歌曲文件!");
		}else if (myapp.getPlaystate() == MUSICPLAY) {
			main_tv01.setText(myapp.getPlayname());
			int num = myapp.getMediaplayer().getDuration();
			main_sk01.setMax(num);
			int max = num / 1000;
			if (max % 60 < 10) {
				main_tv03.setText(max / 60 + ":0" + max % 60);
			} else {
				main_tv03.setText(max / 60 + ":" + max % 60);
			}
			if (!TCSetSeekBar) {
				TCSetSeekBar = true;
				new Thread(new CtrlSeekBar()).start();
			}
			if (myapp.getPlaystate() == MUSICPLAY) {
				main_butplay.setBackgroundResource(R.drawable.m_stop);
			} else {
				main_butplay.setBackgroundResource(R.drawable.m_play);
			}
			ShowCurrentPlayMusic();
		}		
		super.onStart();
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_SET_ID) {
			if (resultCode == RESULT_SET_EXIT) {
//				Log.i(TAG,"SET--->RESULT_SET_EXIT");
				if (TCSetSeekBar) {
					TCSetSeekBar = false;
				}
				Intent intent = new Intent(MainActivity.this, MusicService.class);
				stopService(intent);
				finish();
			}
			if (resultCode == RESULT_SET_RESET) {
//				Log.i(TAG,"SET--->RESULT_SET_RESET");
				recreate();
				myapp.setHideSetActivity(false);
				Intent intentset = new Intent();
				intentset.setClass(MainActivity.this, SetActivity.class);
				startActivityForResult(intentset, RESULT_SET_ID);
			}
			if (resultCode == RESULT_SET_BACK) {
			}
		}
		if (requestCode == RESULT_ABOUT_ID) {
			if (resultCode == RESULT_ABOUT_MYLOVE) {
				if (myapp.getListmode() != LOVEMODE) {
					myapp.setListmode(LOVEMODE);
					main_tv04.setTextColor(Color.parseColor("#770077"));
					main_tv05.setTextColor(Color.parseColor("#FF00FF"));
					main_tv06.setTextColor(Color.parseColor("#770077"));
					main_line01.setBackgroundResource(R.drawable.topbk2);					
					myapp.getPlaylist().clear();
					db.QueryMyloveSdcard(myapp.getPlaylist());						
					baseadapter.notifyDataSetChanged();
				}			
			}
			if (resultCode == RESULT_ABOUT_SDCARD) {
				if (myapp.getListmode() != SDCARDMODE) {

					myapp.setListmode(SDCARDMODE);
					main_tv04.setTextColor(Color.parseColor("#FF00FF"));
					main_tv05.setTextColor(Color.parseColor("#770077"));
					main_tv06.setTextColor(Color.parseColor("#770077"));
					main_line01.setBackgroundResource(R.drawable.topbk1);
					myapp.getPlaylist().clear();
					db.QueryAllSdcard(myapp.getPlaylist());						
					baseadapter.notifyDataSetChanged();
				}
			}
			if (resultCode == RESULT_ABOUT_EXIT) {
//				if ((myapp.getPlaystate() == MUSICPLAY )|| (myapp.isListenphone())) {			
//					StartMusicService();
//						finish();
//					}
//				} else if (myapp.isHideSetActivity()&&myapp.isHideManagerActivity()) {
//					Intent sintent = new Intent(MainActivity.this, MusicService.class);
//					stopService(sintent);
//					finish();
//				}
			}
		}

	}

	private OnClickListener ListenerImgButOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(myapp.isWaitlist())
			{
				main_tv01.setText("正在扫描手机中的歌曲文件，请稍候!");
				return ;
			}
			switch (v.getId()) {
			case R.id.main_butsets:
				Intent intentset = new Intent();
				intentset.setClass(MainActivity.this, SetActivity.class);
				myapp.setHideSetActivity(false);
				startActivityForResult(intentset, RESULT_SET_ID);
				break;
			case R.id.main_butplay:
				switch (myapp.getPlaystate()) {
				case MUSICNONE:
					if (myapp.getPlaymode() == PLAYRANDOM) {
						PlayNextMusic();
					} else {
						PlayMusic(myapp.getCurrentplay());
					}
					break;
				case MUSICPLAY:
					myapp.getMediaplayer().pause();
					position = myapp.getMediaplayer().getCurrentPosition();
					myapp.setPlaystate(MUSICPAUSE);
					main_butplay.setBackgroundResource(R.drawable.m_play);
					break;
				case MUSICPAUSE:
					myapp.getMediaplayer().seekTo(position);
					myapp.getMediaplayer().start();
					myapp.setPlaystate(MUSICPLAY);
					main_butplay.setBackgroundResource(R.drawable.m_stop);
					break;
				}
				break;
			case R.id.main_butnext:
				PlayNextMusic();
				break;
			case R.id.main_butfore:
				PlayForeMusic();
				break;
			case R.id.main_butstop:
				// new Thread(new FindSdcardMusic()).start();
//				finish();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				intent.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent);
				break;
			}
		}
	};

	private void ShowCurrentPlayMusic() {
		int i = myapp.getCurrentplay() - 1;
		if (i < 0)
			i = 0;
		if (main_lv01 != null) {
			baseadapter.notifyDataSetChanged();
			main_lv01.setSelection(i);// 让当前选择列位于第一行.(这句要在上一句前面好像没用)
		}
	}

	private OnClickListener ListenerTextViewOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(myapp.isWaitlist())
			{
				main_tv01.setText("正在扫描手机的歌曲文件，请稍候......");
				return ;
			}
			switch (v.getId()) {
			case R.id.main_tv01:
				ShowCurrentPlayMusic();
				break;
			case R.id.main_tv04:// SDCARD
				if (myapp.getListmode() == SDCARDMODE) {
					break;}	
				myapp.setListmode(SDCARDMODE);
				main_tv04.setTextColor(Color.parseColor("#FF00FF"));
				main_tv05.setTextColor(Color.parseColor("#770077"));
				main_tv06.setTextColor(Color.parseColor("#770077"));
				main_line01.setBackgroundResource(R.drawable.topbk1);
				myapp.getPlaylist().clear();
				db.QueryAllSdcard(myapp.getPlaylist());
				baseadapter.notifyDataSetChanged();
				break;
			case R.id.main_tv05://LOVEMODE
				if (myapp.getListmode() == LOVEMODE) {
					break;
				}
				myapp.setListmode(LOVEMODE);
				main_tv04.setTextColor(Color.parseColor("#770077"));
				main_tv05.setTextColor(Color.parseColor("#FF00FF"));
				main_tv06.setTextColor(Color.parseColor("#770077"));
				main_line01.setBackgroundResource(R.drawable.topbk2);	
				myapp.getPlaylist().clear();	
				db.QueryMyloveSdcard(myapp.getPlaylist());
				baseadapter.notifyDataSetChanged();
				break;
			case R.id.main_tv06:
				myapp.setListmode(ABOUTMODE);
				main_tv04.setTextColor(Color.parseColor("#770077"));
				main_tv05.setTextColor(Color.parseColor("#770077"));
				main_tv06.setTextColor(Color.parseColor("#FF00FF"));
				main_line01.setBackgroundResource(R.drawable.topbk3);
				Intent intentabout = new Intent();
				intentabout.setClass(MainActivity.this, ManagerActivity.class);
				myapp.setHideManagerActivity(false);
				startActivityForResult(intentabout,RESULT_ABOUT_ID);
				overridePendingTransition(0, 0);
				break;
			}
		}
	};

	/* 监听按下按钮事件，设置按钮背景图片 */
	@SuppressLint("ClickableViewAccessibility") 
	private OnTouchListener ListenerImgButOnTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (v.getId()) {
			case R.id.main_butplay:
				switch (myapp.getPlaystate()) {
				case MUSICNONE:
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						main_butplay.setBackgroundResource(R.drawable.m_play1);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						main_butplay.setBackgroundResource(R.drawable.m_play);
					}
					break;
				case MUSICPLAY:
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						main_butplay.setBackgroundResource(R.drawable.m_stop1);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						main_butplay.setBackgroundResource(R.drawable.m_stop);
					}
					break;
				case MUSICPAUSE:
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						main_butplay.setBackgroundResource(R.drawable.m_play1);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						main_butplay.setBackgroundResource(R.drawable.m_play);
					}
					break;
				}
				break;
			}
			return false;
		}
	};
	private OnItemClickListener ListenerListViewItemOnClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			myapp.setCurrentplay((int) arg3);
			PlayMusic(myapp.getCurrentplay());
			baseadapter.notifyDataSetChanged();
		}
	};

	private OnSeekBarChangeListener ListenerSeekBarChange = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (MUSICNONE != myapp.getPlaystate()) {
				myapp.getMediaplayer().seekTo(seekBar.getProgress());
			}
		}
	};

	private void PlayForeMusic() {
		if (myapp.getPlaymode() == PLAYRANDOM) {
			Random random = new Random();
			int i = random.nextInt();
			if (i < 0) {
				i = 0 - i;
			}
			if (myapp.getPlaylist().size() > 0) {
				myapp.setCurrentplay(i % (myapp.getPlaylist().size()));
				PlayMusic(myapp.getCurrentplay());
			}
		} else {
			myapp.setCurrentplay(myapp.getCurrentplay() - 1);
			if (myapp.getCurrentplay() < 0) {
				myapp.setCurrentplay(0);
			}
		}
		PlayMusic(myapp.getCurrentplay());
		ShowCurrentPlayMusic();
	}

	private void PlayNextMusic() {
		switch (myapp.getPlaymode()) {
		case PLAYSINGLELOOP:
			// PlayMusic(myapp.getCurrentplay());
			// break;
		case PLAYLISTEND:
			myapp.setCurrentplay(myapp.getCurrentplay() + 1);
			if (myapp.getCurrentplay() < myapp.getPlaylist().size()) {
				PlayMusic(myapp.getCurrentplay());
			} else {
				myapp.setCurrentplay(myapp.getCurrentplay() - 1);
			}
			break;
		case PLAYRANDOM:
			Random random = new Random();
			int i = random.nextInt();
			if (i < 0) {
				i = 0 - i;
			}
			if (myapp.getPlaylist().size() > 0) {
				myapp.setCurrentplay(i % (myapp.getPlaylist().size()));
				PlayMusic(myapp.getCurrentplay());
			}
			break;
		case PLAYLISTLOOP:
			myapp.setCurrentplay(myapp.getCurrentplay() + 1);
			if (myapp.getCurrentplay() >= myapp.getPlaylist().size()) {
				myapp.setCurrentplay(0);
			}
			PlayMusic(myapp.getCurrentplay());
			break;
		}
		ShowCurrentPlayMusic();
	}

	private void PlayMusic(int number) {
		if(myapp.getPlaylist()==null) return;
		if(myapp.getPlaylist().size()<=0) return;	
		myapp.setPlayname((String) (myapp.getPlaylist().get(number).get("NAME")));
		if (myapp.getMediaplayer() == null )
		{
			myapp.setMediaplayer(new MediaPlayer());
		}		
		try {			
			myapp.getMediaplayer().reset();
			myapp.getMediaplayer().setDataSource((String) (myapp.getPlaylist().get(number).get("PATH")));
			myapp.getMediaplayer().prepare();
			myapp.setPlaystate(MUSICPLAY);
			int num = myapp.getMediaplayer().getDuration();
			main_sk01.setMax(num);
			int max = num / 1000;
			if (max % 60 < 10) {
				main_tv03.setText(max / 60 + ":0" + max % 60);
			} else {
				main_tv03.setText(max / 60 + ":" + max % 60);
			}
			main_butplay.setBackgroundResource(R.drawable.m_stop);
			if (!TCSetSeekBar) {
				TCSetSeekBar = true;
				new Thread(new CtrlSeekBar()).start();
			}
			myapp.getMediaplayer().start();
			if (myapp.isWaitlist()==false) {
				main_tv01.setText(myapp.getPlayname());
			}
		} catch (Exception e) {
//			Log.i(TAG, "playmusic trows exception!");
			main_tv01.setText("歌曲文件已删除或文件已损坏:(" + myapp.getPlayname() + ")");
		}
		
	}

	private class CtrlSeekBar implements Runnable {
		@Override
		public void run() {
			while (TCSetSeekBar) {
				Message msg = new Message();
				msg.what = HDMAIN_SK01;
				HandlerMain.sendMessage(msg);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	
	
	private class UpDBSdcardMusic implements Runnable {
		@Override
		public void run() {
			{	
				String state = Environment.getExternalStorageState();
				
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					File file = Environment.getExternalStorageDirectory();
					db.DetectionSdcard();
					FindAndList(file);
				}				
				Message msg = new Message();
				msg.what = HDMAIN_UPDBEND;
				HandlerMain.sendMessage(msg);
			}
		}	

		private void FindAndList(File f) {
			if (f.isFile()) {
				String filepath = f.getAbsolutePath();
				if (db.QueryOneSdcard(filepath)==false) {
					String filename = f.getName();
					String filetype = filename.substring(filename.length() - 4,	filename.length());
					if (filetype.equalsIgnoreCase(".mp3")) {
						ContentValues cv = new ContentValues();
						cv.put("NAME", filename.substring(0, filename.length() - 4));
						cv.put("PATH", filepath);
						try {
							MediaPlayer md = new MediaPlayer();							
							md.setDataSource(filepath);
							md.prepare();
							int num = md.getDuration();
							int max = num / 1000;
							if (max % 60 < 10) {
								cv.put("TIME", (max / 60 + ":0" + max % 60));								
							} else {
								cv.put("TIME", (max / 60 + ":" + max % 60));								
							}
							db.InsertSdcard(cv);
						} catch (Exception e) {
//							db.InsertBad(cv);							
						}
					}
				}
			} else if (f.isDirectory()) {
				try {
					File fillist[] = f.listFiles();
					if (fillist != null) {
						for (int i = 0; i < fillist.length; i++) {
							FindAndList(fillist[i]);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class FindSdcardMusic implements Runnable {
		private int count=0;
		@Override
		public void run() {
			{	
				count=0;
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					File file = Environment.getExternalStorageDirectory();
					FindAndList(file);
				}				
				Message msg = new Message();
				msg.what = HDMAIN_LVEND;
				HandlerMain.sendMessage(msg);
			}
		}	

		private void FindAndList(File f) {
			if (f.isFile()) {
				String filename = f.getName();
				String filepath = f.getAbsolutePath();
				String filetype = filename.substring(filename.length() - 4,	filename.length());
				if (filetype.equalsIgnoreCase(".mp3")) {					
					ContentValues cv=new ContentValues();
					cv.put("NAME", filename.substring(0, filename.length() - 4));
					cv.put("PATH", filepath);						
					try {
						MediaPlayer md = new MediaPlayer();
						HashMap<String,Object> map = new HashMap<String,Object>();
						md.setDataSource(filepath);
						md.prepare();
						int num = md.getDuration();						
						int max = num / 1000;
						if (max % 60 < 10) {
							cv.put("TIME", (max / 60 + ":0" + max % 60));
							map.put("TIME", (max / 60 + ":0" + max % 60));
						} else {
							cv.put("TIME", (max / 60 + ":" + max % 60));
							map.put("TIME", (max / 60 + ":" + max % 60));
						}						
						db.InsertSdcard(cv);						
						map.put("NAME", filename.substring(0, filename.length() - 4));
						map.put("PATH", filepath);
						map.put("NUMB", count);
						Message msg = new Message();
						msg.obj=map;
						msg.what=HDMAIN_LVADD;
						HandlerMain.sendMessage(msg);
						md.release();
						count++;
					}
					catch(Exception e)
					{
//						db.InsertBad(cv);
//						Log.i(TAG,"扫描发现不能打开的歌曲文件");
					}					
				}
			} else if (f.isDirectory()) {
				try {
					File fillist[] = f.listFiles();
					if (fillist != null) {
						for (int i = 0; i < fillist.length; i++) {
							FindAndList(fillist[i]);
						}
					}
				} catch (Exception e) {
//					Log.i(TAG, e.toString());
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (exitstate) {
			exitstate = false;
			Intent intent = new Intent(MainActivity.this, MusicService.class);
			stopService(intent);
			finish();
		} else {
			Toast.makeText(getApplicationContext(), "再按一次退出!", Toast.LENGTH_SHORT).show();
			exitstate = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					exitstate = false;
				}
			}).start();
		}
	}

	@Override
	protected void onDestroy() {
//		Log.i(TAG, "MainActivity.OnDestroy().");
		if (TCSetSeekBar) {
			TCSetSeekBar = false;
		}
		unregisterReceiver(broadcastreceiver);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
//		Log.i(TAG, "MainActivity.onStop()."+myapp.isHideSetActivity()+myapp.isHideManagerActivity());
		myapp.setHideMainActivity(true);
		if ((myapp.getPlaystate() == MUSICPLAY )|| (myapp.isListenphone())) {			
			StartMusicService();
//			if(myapp.isHideSetActivity()&&myapp.isHideManagerActivity()){
//				finish();
//			}
		} else if (myapp.isHideSetActivity()&&myapp.isHideManagerActivity()) {
			Intent sintent = new Intent(MainActivity.this, MusicService.class);
			stopService(sintent);
			finish();
		}
		super.onStop();
	}

	private void StartMusicService() {
		Intent intent = new Intent(MainActivity.this, MusicService.class);
		startService(intent);
	}	

	private Handler HandlerMain = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HDMAIN_UPDBEND:
				myapp.setWaitlist(false);
				main_tv01.setText("歌曲更新完毕!");
				UpListPlayList();
				break;
			case HDMAIN_LVCLEAR:
				myapp.getPlaylist().clear();
				baseadapter.notifyDataSetChanged();
				break;			
			case HDMAIN_LVEND:				
				myapp.setWaitlist(false);
				if (myapp.getPlaylist().size() <= 0) {
					if (myapp.isHideMainActivity() == false) {
						main_tv01.setText("歌曲搜索完毕，手机上未找到任何歌曲，请添加歌曲！");
					}
				} else {
					db.QueryAllSdcard(myapp.getPlaylist());
					if (myapp.isHideMainActivity() == false) {
						main_tv01.setText("歌曲搜索完毕!");
						baseadapter.notifyDataSetChanged();
					}					
				}
				break;
			case HDMAIN_LVADD:
				HashMap<String, Object> map=(HashMap<String, Object>) msg.obj;
				map.put("LOVE",1);
				myapp.getPlaylist().add(map);
				baseadapter.notifyDataSetChanged();
				break;
			case HDMAIN_ADDOPEN:
				HashMap<String, Object> map1=(HashMap<String, Object>) msg.obj;
				map1.put("LOVE",1);
				myapp.getPlaylist().add(map1);
				baseadapter.notifyDataSetChanged();
				myapp.setCurrentplay(myapp.getPlaylist().size()-1);
				ShowCurrentPlayMusic();
				break;
			case HDMAIN_SK01:
				if (myapp.getPlaystate() == MUSICPLAY) {
					position = myapp.getMediaplayer().getCurrentPosition();
					main_sk01.setProgress(position);
					int num = position / 1000;
					if (num % 60 < 10) {
						main_tv02.setText(num / 60 + ":0" + num % 60);
					} else {
						main_tv02.setText(num / 60 + ":" + num % 60);
					}
				}
				break;
			}
		}
	};
	
	private void UpListPlayList(){
		switch(myapp.getListmode()){
		case LOVEMODE:
			myapp.getPlaylist().clear();
			db.QueryMyloveSdcard(myapp.getPlaylist());
			break;					
		case SDCARDMODE:
			myapp.getPlaylist().clear();
			db.QueryAllSdcard(myapp.getPlaylist());
			break;
		}
		if (myapp.isHideMainActivity() == false) {
			baseadapter.notifyDataSetChanged();
		}
	}

	private class MainBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (action.equals(MAIN_ACTION_BROADCAST_UPMUSIC)){
				myapp.setWaitlist(true);
				main_tv01.setText("正在更新手机中的歌曲!");
				new Thread(new UpDBSdcardMusic()).start();
			}
			if (action.equals(MAIN_ACTION_BROADCAST_UPPLAYLIST)){
				UpListPlayList();
			}
			if (action.equals(MAIN_ACTION_BROADCAST_EXIT)) {
				Intent sintent = new Intent(MainActivity.this,MusicService.class);
				stopService(sintent);
				finish();				
			}
			if (action.equals(MAIN_ACTION_BROADCAST_NEXTPLAY)) {
				if (main_tv01 != null) {
					main_tv01.setText(myapp.getPlayname());
				}
				if (main_tv03 != null && main_sk01 != null) {
					int num = myapp.getMediaplayer().getDuration();
					main_sk01.setMax(num);
					int max = num / 1000;
					if (max % 60 < 10) {
						main_tv03.setText(max / 60 + ":0" + max % 60);
					} else {
						main_tv03.setText(max / 60 + ":" + max % 60);
					}
				}
				ShowCurrentPlayMusic();
				if (myapp.getPlaystate() == MUSICPAUSE) {
					if (main_butplay != null) {
						main_sk01.setProgress(0);
						main_butplay.setBackgroundResource(R.drawable.butplay);
					}
				}
			}
		}
	}
}
