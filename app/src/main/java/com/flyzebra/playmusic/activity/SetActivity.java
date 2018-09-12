package com.flyzebra.playmusic.activity;

import java.io.File;

import com.flyzebra.playmusic.R;
import com.flyzebra.playmusic.sqlite.DBHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SetActivity extends Activity {
	private final static String MAIN_ACTION_BROADCAST_UPPLAYLIST="BROADCAST_MAIN_UPPLAYLIST";
	private final static String MAIN_ACTION_BROADCAST_UPMUSIC="BROADCAST_MAIN_UPMUSIC";
	private TextView tv_setviewfull = null;
	private TextView tv_setviewwindow = null;
	private TextView tv_setplaylistend = null;
	private TextView tv_setplaylistloop = null;
	private TextView tv_setplaysingleloop = null;
	private TextView tv_setplayrandom = null;
	private TextView tv_storeaddall = null;
	private TextView tv_storecancleall = null;
	private TextView tv_updatemusic = null;
	private TextView tv_delallstore = null;
	private Button set_butcancel = null;
	private Button set_butexit = null;
	private DBHelper db = new DBHelper(this);
	private final int RESULT_SET_BACK = 2;
	private final int RESULT_SET_EXIT = 3;
	private final int RESULT_SET_RESET = 4;
	
	/* 歌曲按什么顺序播放 */
	private final int PLAYLISTLOOP = 1;
	private final int PLAYRANDOM = 2;
	private final int PLAYSINGLELOOP = 3;
	private final int PLAYLISTEND = 4;
	private MyApp myapp;
	private Intent intent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		myapp = (MyApp) getApplication();
//		Log.i("SET", "SET---->onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set);
		intent = getIntent();
		tv_setviewfull = (TextView) super.findViewById(R.id.setviewfull);
		tv_setviewwindow = (TextView) super.findViewById(R.id.setviewwindow);
		tv_setplaylistend = (TextView) super.findViewById(R.id.playlistend);
		tv_setplaylistloop = (TextView) super.findViewById(R.id.playlistloop);
		tv_setplaysingleloop = (TextView) super.findViewById(R.id.playsingleloop);
		tv_setplayrandom = (TextView) super.findViewById(R.id.playrandom);

		tv_storeaddall = (TextView) super.findViewById(R.id.storeaddall);
		tv_storecancleall = (TextView) super.findViewById(R.id.storecancleall);
		tv_updatemusic = (TextView) super.findViewById(R.id.updatemusic);
		tv_delallstore = (TextView) super.findViewById(R.id.delallstore);

		set_butcancel = (Button) super.findViewById(R.id.set_butcancel);
		set_butexit = (Button) super.findViewById(R.id.set_butexit);

		tv_setviewfull.setOnClickListener(ListenerSetViewOnClick);
		tv_setviewwindow.setOnClickListener(ListenerSetViewOnClick);

		tv_setplaylistend.setOnClickListener(ListenerSetPlayOnClick);
		tv_setplaylistloop.setOnClickListener(ListenerSetPlayOnClick);
		tv_setplaysingleloop.setOnClickListener(ListenerSetPlayOnClick);
		tv_setplayrandom.setOnClickListener(ListenerSetPlayOnClick);

		tv_storeaddall.setOnClickListener(ListenerTextViewOnClick);
		tv_storecancleall.setOnClickListener(ListenerTextViewOnClick);
		tv_updatemusic.setOnClickListener(ListenerTextViewOnClick);
		tv_delallstore.setOnClickListener(ListenerTextViewOnClick);

		set_butcancel.setOnClickListener(ListenerButtonOnClick);
		set_butexit.setOnClickListener(ListenerButtonOnClick);

		if (myapp.getWindowmode()==2) {
			tv_setviewfull.setBackgroundResource(R.drawable.tvbklight);
			tv_setviewwindow.setBackgroundResource(R.drawable.tvbkdark);
		} else {
			tv_setviewfull.setBackgroundResource(R.drawable.tvbkdark);
			tv_setviewwindow.setBackgroundResource(R.drawable.tvbklight);
		}
		switch (myapp.getPlaymode()) {
		case PLAYLISTEND:
			tv_setplaylistend.setBackgroundResource(R.drawable.tvbklight);
			tv_setplaylistloop.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplayrandom.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplaysingleloop.setBackgroundResource(R.drawable.tvbkdark);
			break;
		case PLAYLISTLOOP:
			tv_setplaylistend.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplaylistloop.setBackgroundResource(R.drawable.tvbklight);
			tv_setplayrandom.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplaysingleloop.setBackgroundResource(R.drawable.tvbkdark);
			break;
		case PLAYRANDOM:
			tv_setplaylistend.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplaylistloop.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplayrandom.setBackgroundResource(R.drawable.tvbklight);
			tv_setplaysingleloop.setBackgroundResource(R.drawable.tvbkdark);
			break;
		case PLAYSINGLELOOP:
			tv_setplaylistend.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplaylistloop.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplayrandom.setBackgroundResource(R.drawable.tvbkdark);
			tv_setplaysingleloop.setBackgroundResource(R.drawable.tvbklight);
			break;
		}

	}

	@Override
	protected void onStop() {
//		Log.i("SET", "SET---->onStop");
		myapp.setHideSetActivity(true);
		super.onStop();
	}

	@Override
	protected void onStart() {
//		Log.i("SET", "SET---->onStart");
		myapp.setHideSetActivity(false);
		super.onStart();
	}	
	
	private OnClickListener ListenerTextViewOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.storeaddall:
				ContentValues addcv = new ContentValues();
				addcv.put("LOVE", 1);
				db.UpdateSdcard(addcv, "LOVE=?", new String[] { String.valueOf(0) });
				myapp.SendMainBroadcast(MAIN_ACTION_BROADCAST_UPPLAYLIST);
				break;
			case R.id.storecancleall:
				ContentValues delcv = new ContentValues();
				delcv.put("LOVE", 0);
				db.UpdateSdcard(delcv, "LOVE=?", new String[] { String.valueOf(1) });
				myapp.SendMainBroadcast(MAIN_ACTION_BROADCAST_UPPLAYLIST);
				break;
			case R.id.updatemusic:
				myapp.SendMainBroadcast(MAIN_ACTION_BROADCAST_UPMUSIC);
				myapp.setHideMainActivity(true);
				finish();
				break;
			case R.id.delallstore:
				AlertDialog.Builder builder = new AlertDialog.Builder(SetActivity.this);
				builder.setTitle("删除文件");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {	
								for(int i=0;i<myapp.getPlaylist().size();i++)
								{
									if(0==(Integer)myapp.getPlaylist().get(i).get("LOVE")){
										String path=(String) myapp.getPlaylist().get(i).get("PATH");
										File f = new File(path);
										f.delete();
										db.DeleteOneSdcard(path);
										myapp.SendMainBroadcast(MAIN_ACTION_BROADCAST_UPPLAYLIST);
									}
								}
								dialog.dismiss();
							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which) {
								dialog.cancel();
							}
						});
				builder.create();
				builder.setMessage("警告：一旦删除将不能恢复，按确定将执行删除操作，按取消放弃操作！");
				builder.show();
				break;
			}
		}
	};

	private OnClickListener ListenerSetViewOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.setviewfull:
				tv_setviewfull.setBackgroundResource(R.drawable.tvbklight);
				tv_setviewwindow.setBackgroundResource(R.drawable.tvbkdark);
				if (myapp.getWindowmode()==1) {
					myapp.setWindowmode(2);
					db.SaveSet("WINDOWMODE", 2);
					setResult(RESULT_SET_RESET, intent);
					SetActivity.this.finish();
				}
				break;
			case R.id.setviewwindow:
				tv_setviewfull.setBackgroundResource(R.drawable.tvbkdark);
				tv_setviewwindow.setBackgroundResource(R.drawable.tvbklight);
				if (myapp.getWindowmode()==2) {
					myapp.setWindowmode(1);
					db.SaveSet("WINDOWMODE", 1);
					setResult(RESULT_SET_RESET, intent);
					SetActivity.this.finish();
				}
				break;
			}
		}
	};
	private OnClickListener ListenerSetPlayOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.playlistend:
				tv_setplaylistend.setBackgroundResource(R.drawable.tvbklight);
				tv_setplaylistloop.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplayrandom.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplaysingleloop.setBackgroundResource(R.drawable.tvbkdark);
				myapp.setPlaymode(PLAYLISTEND);
				db.SaveSet("PLAYMODE", PLAYLISTEND);
				break;
			case R.id.playlistloop:
				tv_setplaylistend.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplaylistloop.setBackgroundResource(R.drawable.tvbklight);
				tv_setplayrandom.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplaysingleloop.setBackgroundResource(R.drawable.tvbkdark);
				myapp.setPlaymode(PLAYLISTLOOP);
				db.SaveSet("PLAYMODE", PLAYLISTLOOP);
				break;
			case R.id.playrandom:
				tv_setplaylistend.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplaylistloop.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplayrandom.setBackgroundResource(R.drawable.tvbklight);
				tv_setplaysingleloop.setBackgroundResource(R.drawable.tvbkdark);
				myapp.setPlaymode(PLAYRANDOM);
				db.SaveSet("PLAYMODE", PLAYRANDOM);
				break;
			case R.id.playsingleloop:
				tv_setplaylistend.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplaylistloop.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplayrandom.setBackgroundResource(R.drawable.tvbkdark);
				tv_setplaysingleloop.setBackgroundResource(R.drawable.tvbklight);
				myapp.setPlaymode(PLAYSINGLELOOP);
				db.SaveSet("PLAYMODE", PLAYSINGLELOOP);
				break;
			}
		}
	};
	private OnClickListener ListenerButtonOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.set_butcancel:
				setResult(RESULT_SET_BACK, intent);
				myapp.setHideSetActivity(true);
				SetActivity.this.finish();
				break;
			case R.id.set_butexit:
				setResult(RESULT_SET_EXIT, intent);
				myapp.setHideSetActivity(true);
				SetActivity.this.finish();
				break;
			}
		}
	};

}
