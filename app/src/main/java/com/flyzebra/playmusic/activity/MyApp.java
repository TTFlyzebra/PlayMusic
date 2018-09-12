package com.flyzebra.playmusic.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.flyzebra.playmusic.sqlite.DBHelper;

import android.app.Application;
import android.content.Intent;
import android.media.MediaPlayer;

public class MyApp extends Application {
	private MediaPlayer mediaplayer;
	private List<HashMap<String, Object>> playlist;
	/* 正在播放第几首歌 */
	private int currentplay = 0;
	private String playname;
	
	/* 歌曲播放顺序playmode (PLAYLISTLOOP = 1;PLAYRANDOM = 2;PLAYSINGLELOOP = 3;PLAYLISTEND = 4;)*/
	private int playmode = 1;
	/* 当前播放列表 listmode(LOVEMODE = 1;SDCARDMODE = 2;ABOUTMODE = 3;) */
	private int listmode = 1;
	/* 歌曲播放状态playstate(MUSICNONE = 0;MUSICPLAY = 1;MUSICPAUSE = 2;) */
	private int playstate = 0;	
	/*窗口显示模式windowmode(SHOWFULL=1;SHOWWINDOW=2)*/
	private int windowmode = 1;
	
	public int getWindowmode() {
		return windowmode;
	}

	public void setWindowmode(int windowmode) {
		this.windowmode = windowmode;
	}

	/* MainActivity是否被隐藏 */
	private boolean hideSetActivity = true;
	private boolean hideManagerActivity = true;
	private boolean hideMainActivity = true;
	public boolean isHideSetActivity() {
		return hideSetActivity;
	}

	public void setHideSetActivity(boolean hideSetActivity) {
		this.hideSetActivity = hideSetActivity;
	}

	public boolean isHideManagerActivity() {
		return hideManagerActivity;
	}

	public void setHideManagerActivity(boolean hideManagerActivity) {
		this.hideManagerActivity = hideManagerActivity;
	}

	/* 窗口显示模式 */
	
	private boolean waitlist = false;
	public boolean isWaitlist() {
		return waitlist;
	}

	public void setWaitlist(boolean waitlist) {
		this.waitlist = waitlist;
	}

	public boolean isListenphone() {
		return listenphone;
	}

	public void setListenphone(boolean listenphone) {
		this.listenphone = listenphone;
	}

	/*电话接听标识*/
	private boolean listenphone = false; 	

	
	public boolean isHideMainActivity() {
		return hideMainActivity;
	}

	public void setHideMainActivity(boolean hideMainActivity) {
		this.hideMainActivity = hideMainActivity;
	}	
	
	public String getPlayname() {
		return playname;
	}

	public void setPlayname(String playname) {
		this.playname = playname;
	}

	public int getListmode() {
		return listmode;
	}

	public void setListmode(int listmode) {
		this.listmode = listmode;
	}

	public int getPlaymode() {
		return playmode;
	}

	public void setPlaymode(int playmode) {
		this.playmode = playmode;
	}

	public int getPlaystate() {
		return playstate;
	}

	public void setPlaystate(int playstate) {
		this.playstate = playstate;
	}

	public MediaPlayer getMediaplayer() {
		return mediaplayer;
	}

	public void setMediaplayer(MediaPlayer mediaplayer) {
		this.mediaplayer = mediaplayer;
	}

	public List<HashMap<String, Object>> getPlaylist() {
		return playlist;
	}

	public void setPlaylist(List<HashMap<String, Object>> playlist) {
		this.playlist = playlist;
	}

	public int getCurrentplay() {
		return currentplay;
	}

	public void setCurrentplay(int currentplay) {
		this.currentplay = currentplay;
	}

	@Override
	public void onCreate() {		
		DBHelper db = new DBHelper(this);
		setPlaymode(db.ReadSet("PLAYMODE"));
		setWindowmode(db.ReadSet("WINDOWMODE"));
		db.close();
		mediaplayer=new MediaPlayer();
		playlist = new ArrayList<HashMap<String, Object>>();		
		super.onCreate();
	}
	
	public void SendMainBroadcast(String action){
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent);
	}

}
