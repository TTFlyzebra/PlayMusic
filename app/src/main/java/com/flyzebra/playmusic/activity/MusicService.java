package com.flyzebra.playmusic.activity;

import java.util.Random;

import com.flyzebra.playmusic.R;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
//import android.util.Log;
import android.widget.RemoteViews;

public class MusicService extends Service {
	/* 常量 */
//	private final static String TAG = "PLAYMUSIC";

	/* 全局变量 */
	private MyApp myapp;
	/* 广播 */
	private final static String MAIN_ACTION_BROADCAST_EXIT = "BROADCAST_MAIN_EXIT";
	private final static String MAIN_ACTION_BROADCAST_NEXTPLAY = "BROADCAST_MAIN_NEXTPLAY";
	private final static String SERVICEACTION = "BROADCAST_SERVICE";
	private ServiceBroadCast broadcastreceiver = new ServiceBroadCast();

	/* 媒体的播放状态 */

	private final int MUSICNONE = 0;
	private final int MUSICPLAY = 1;
	private final int MUSICPAUSE = 2;

	/* 歌曲按什么顺序播放 */
	private final int PLAYLISTLOOP = 1;
	private final int PLAYRANDOM = 2;
	private final int PLAYSINGLELOOP = 3;
	private final int PLAYLISTEND = 4;	

	/* 电话监听 */
	private int playposition = 0;	
	private TelephonyManager telephonymanager = null;

	/* 通知栏 */
	private RemoteViews remoteviews = null;
//	private NotificationManager notimanager = null;
	private final int NOTIFICATION_ID = 1;
	private Notification noti = null;

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		super.onCreate();
		myapp = (MyApp) getApplication();
		/* 电话监听 */
		telephonymanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonymanager.listen(ListenerPhoneState,
				PhoneStateListener.LISTEN_CALL_STATE);

		/* 注册广播 */
		broadcastreceiver = new ServiceBroadCast();
		IntentFilter filter = new IntentFilter(SERVICEACTION);
		registerReceiver(broadcastreceiver, filter);
		
		/* 初始化数据 */
		if(myapp.getMediaplayer()==null)
		{
			myapp.setMediaplayer(new MediaPlayer());			
		}
		myapp.getMediaplayer().setOnCompletionListener(ListenermdCompletion);
//		Log.i(TAG, "service oncreate is run!");

		/* 通知栏 */
//		notimanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= 16) {
			remoteviews = new RemoteViews(getPackageName(),	R.layout.notification);
			remoteviews.setImageViewResource(R.id.noti_butfore,	R.drawable.butfore);
			remoteviews.setImageViewResource(R.id.noti_butplay,	R.drawable.butstop);
			remoteviews.setImageViewResource(R.id.noti_butnext,	R.drawable.butnext);
			remoteviews.setImageViewResource(R.id.noti_butexit,	R.drawable.butexit_noti);
			Intent intent = new Intent();
			intent.setAction(SERVICEACTION);
			intent.putExtra("ACTION", "FORE");
			PendingIntent nextPI = PendingIntent.getBroadcast(this, 1, intent,PendingIntent.FLAG_UPDATE_CURRENT);
			intent.putExtra("ACTION", "PLAY");
			PendingIntent playPI = PendingIntent.getBroadcast(this, 2, intent,PendingIntent.FLAG_UPDATE_CURRENT);
			intent.putExtra("ACTION", "NEXT");
			PendingIntent forePI = PendingIntent.getBroadcast(this, 3, intent,PendingIntent.FLAG_UPDATE_CURRENT);
			intent.putExtra("ACTION", "EXIT");
			PendingIntent exitPI = PendingIntent.getBroadcast(this, 4, intent,PendingIntent.FLAG_UPDATE_CURRENT);
			remoteviews.setOnClickPendingIntent(R.id.noti_butfore, nextPI);
			remoteviews.setOnClickPendingIntent(R.id.noti_butplay, playPI);
			remoteviews.setOnClickPendingIntent(R.id.noti_butnext, forePI);
			remoteviews.setOnClickPendingIntent(R.id.noti_butexit, exitPI);
			SetRemoteViews();
			Intent notiintent = new Intent(this, MainActivity.class);
			notiintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent PdIntent = PendingIntent.getActivity(this, 0,	notiintent, PendingIntent.FLAG_UPDATE_CURRENT);
			// Bitmap icon =
			// BitmapFactory.decodeResource(getResources(),R.drawable.icon);
			noti = new Builder(MusicService.this)
			.setContent(remoteviews)
			.setContentIntent(PdIntent)
			.setOngoing(true)
			.build();
			// noti.defaults=Notification.DEFAULT_SOUND;
			noti.bigContentView = remoteviews;
			noti.icon = android.R.drawable.ic_media_play;
		}
		
//		Intent intent = new Intent(this,MainActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
//		startActivity(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (myapp.isHideMainActivity()==false) {
			stopForeground(true);
//			notimanager.cancelAll();
		} else {
			//降低程序被回收可能,好像木有意义
			ShowNotification();			
		}	
//		flags =  START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	/* 电话监听事件 */
	private PhoneStateListener ListenerPhoneState = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:// 电话铃响起
				myapp.setListenphone(true);
				if (myapp.getPlaystate() == MUSICPLAY) {
					playposition = myapp.getMediaplayer().getCurrentPosition();
					myapp.getMediaplayer().pause();
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:// 接听电话
				break;
			case TelephonyManager.CALL_STATE_IDLE:// 挂断电话
				if (myapp.getPlaystate() == MUSICPLAY) {
					myapp.getMediaplayer().seekTo(playposition);
					myapp.getMediaplayer().start();
				}
				myapp.setListenphone(false);
				break;
			}
		}
	};

	private OnCompletionListener ListenermdCompletion = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			if (myapp.getPlaymode() == PLAYSINGLELOOP
					|| myapp.getPlaystate() == MUSICNONE) {
				PlayMusic(myapp.getCurrentplay());
			} else if (myapp.getPlaymode() == PLAYLISTEND) {
				myapp.setCurrentplay(myapp.getCurrentplay() + 1);
				if (myapp.getCurrentplay() < myapp.getPlaylist().size()) {
					PlayMusic(myapp.getCurrentplay());
				} else {
					myapp.setCurrentplay(0);
					myapp.setPlaystate(MUSICPAUSE);
					try {
						myapp.getMediaplayer().reset();
						myapp.getMediaplayer().setDataSource((String) (myapp.getPlaylist().get(
										myapp.getCurrentplay()).get("PATH")));
						myapp.getMediaplayer().prepare();
						myapp.setPlayname((String) (myapp.getPlaylist().get(myapp.getCurrentplay()).get("NAME")));
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (myapp.isHideMainActivity()) {
						ShowNotification();
					} else
					// 用广播更新Activity中的UI
					{
						Intent intent = new Intent();
						intent.setAction(MAIN_ACTION_BROADCAST_NEXTPLAY);
						sendBroadcast(intent);
					}
				}
			} else {
				PlayNextMusic();
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
		if (myapp.isHideMainActivity()) {
			ShowNotification();
		}
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
		if (myapp.isHideMainActivity()) {
			ShowNotification();
		}
	}

	private void PlayMusic(int numbar) {
//		Log.i(TAG, "PLAY--->" + numbar);
		if ((myapp.getMediaplayer() != null && myapp.getPlaylist().size() > 0)) {
			myapp.getMediaplayer().reset();
			try {
				myapp.getMediaplayer().setDataSource(
						(String) (myapp.getPlaylist().get(numbar).get("PATH")));
				myapp.getMediaplayer().prepare();
				myapp.getMediaplayer().start();
				myapp.setPlayname((String) myapp.getPlaylist().get(numbar).get("NAME"));
				myapp.setPlaystate(MUSICPLAY);
				// 用广播更新Activity中的UI
				if (myapp.isHideMainActivity() == false) {
					Intent intent = new Intent();
					intent.setAction(MAIN_ACTION_BROADCAST_NEXTPLAY);
					sendBroadcast(intent);
				}
				ShowNotification();
			} catch (Exception e) {
//				Log.i(TAG, "playmusic trows exception!");
			}
		}
	}

	@Override
	public void onDestroy() {
//		Log.i(TAG, "MusicService.onDestroy().");
		stopForeground(true);
		myapp.getMediaplayer().release();
		myapp.setMediaplayer(null);
		myapp.setCurrentplay(0);
		myapp.getPlaylist().clear();
		myapp.setPlaystate(MUSICNONE);
		myapp.setListmode(1);
//		notimanager.cancelAll();
		unregisterReceiver(broadcastreceiver);
		super.onDestroy();
	}

	private void ShowNotification() {
		if (myapp.isHideMainActivity() && myapp.isHideManagerActivity()) {
			SetRemoteViews();
//			notimanager.notify(NOTIFICATION_ID, noti);
			startForeground(NOTIFICATION_ID, noti);
		}
	}

	private void SetRemoteViews() {
		remoteviews.setTextViewText(R.id.noti_tv01_musicname,	myapp.getPlayname());
		if (myapp.getPlaystate() == MUSICPLAY) {
			remoteviews.setImageViewResource(R.id.noti_butplay,
					R.drawable.butstop);
		} else {
			remoteviews.setImageViewResource(R.id.noti_butplay,
					R.drawable.butplay);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class ServiceBroadCast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
//			Log.i(TAG, SERVICEACTION);
			if (action.equals(SERVICEACTION)) {
				if (intent.getStringExtra("ACTION").equals("FORE")) {
					PlayForeMusic();
//					Log.i(TAG, "按了FORE");
				} else if (intent.getStringExtra("ACTION").equals("PLAY")) {
					switch (myapp.getPlaystate()) {
					case MUSICPLAY:
						myapp.getMediaplayer().pause();
						myapp.setPlaystate(MUSICPAUSE);
						break;
					case MUSICPAUSE:
						myapp.setPlaystate(MUSICPLAY);
						myapp.getMediaplayer().start();
						break;
					case MUSICNONE:
						PlayMusic(myapp.getCurrentplay());
						break;
					}
					ShowNotification();
//					Log.i(TAG, "按了PLAY");
				} else if (intent.getStringExtra("ACTION").equals("NEXT")) {
					PlayNextMusic();
//					Log.i(TAG, "按了NEXT");
				} else if (intent.getStringExtra("ACTION").equals("EXIT")) {
					Intent bintent = new Intent();
					bintent.setAction(MAIN_ACTION_BROADCAST_EXIT);
					sendBroadcast(bintent);
					stopSelf();
//					Log.i(TAG, "按了EXIT");
				}
			}
		}
	}
}
