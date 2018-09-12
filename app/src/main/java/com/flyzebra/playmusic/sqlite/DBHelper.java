package com.flyzebra.playmusic.sqlite;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "palymusic.db";
	private static final String TABLE_BAD = "badmusic";
	private static final String TABLE_SET = "setmusic";
	private static final String TABLE_SDCARD = "sdcardmusic";
	private static final String CREATE_TABLE_SET = "create table setmusic(MODE text primary key, ST integer DEFAULT 1)";
	private static final String CREATE_TABLE_BAD = "create table badmusic(PATH text primary key, NAME text)";
	private static final String CREATE_TABLE_SDCARD = "create table sdcardmusic(PATH text primary key, NAME text ,TIME text ,LOVE integer DEFAULT 1)";
	private SQLiteDatabase db;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;		
		db.execSQL(CREATE_TABLE_SDCARD);
		db.execSQL(CREATE_TABLE_SET);
		db.execSQL(CREATE_TABLE_BAD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	//检测存储数据的正确性(数据库中的文件是否已经被删除损坏什么的呀)	
	public int DetectionSdcard()
	{
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_SDCARD, null, null, null, null, null, null);
		int i = 0;
		while (c.moveToNext()) {
			String path = c.getString(c.getColumnIndex("PATH"));
			File f = new File (path);
			if (f.exists() == false) {
				db.delete(TABLE_SDCARD, "PATH=?", new String[]{path});
				i++;
			}
		}
		c.close();
		return i;
	}
	// 插入方法
	public boolean InsertBad(ContentValues value) {
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_BAD, null, value);
		db.close();
		return true;
	}

	public boolean InsertSdcard(ContentValues value) {
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_SDCARD, null, value);
		db.close();
		return true;
	}	

	// 列出整个表的数据
	public int QueryAllBad(List<HashMap<String, Object>> list) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_BAD, null, null, null, null, null, null);
		int i = 0;
		while (c.moveToNext()) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("NUMB", i);
			map.put("NAME", c.getString(c.getColumnIndex("NAME")));
			map.put("PATH", c.getString(c.getColumnIndex("PATH")));
			map.put("TIME", c.getString(c.getColumnIndex("TIME")));
			map.put("LOVE", true);
			list.add(map);
			i++;
		}
		c.close();
		return i;
	}	
	
	public int QueryAllSdcard(List<HashMap<String, Object>> list) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_SDCARD, null, null, null, null, null, null);
		int i = 0;
		while (c.moveToNext()) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("NUMB", i);
			map.put("NAME", c.getString(c.getColumnIndex("NAME")));
			map.put("PATH", c.getString(c.getColumnIndex("PATH")));
			map.put("TIME", c.getString(c.getColumnIndex("TIME")));
			map.put("LOVE", c.getInt(c.getColumnIndex("LOVE")));
			list.add(map);
			i++;
		}
		c.close();
		return i;
	}
	
	//列出所有收藏的数据
	public int QueryMyloveSdcard(List<HashMap<String, Object>> list) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.rawQuery("select * from sdcardmusic where LOVE = 1", null);
		int i = 0;
		while (c.moveToNext()) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("NUMB", i);
			map.put("NAME", c.getString(c.getColumnIndex("NAME")));
			map.put("PATH", c.getString(c.getColumnIndex("PATH")));
			map.put("TIME", c.getString(c.getColumnIndex("TIME")));
			map.put("LOVE", 1);
			list.add(map);
			i++;
		}
		c.close();
		return i;
	}
	/*
	 * 查找数据在第几行(这个行有待研究)
	 */
	public int QueryOneInRowSdcard(String path) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_SDCARD, null, null, null, null, null, null);
		int i = 0;
		Log.e("PLAYMUSIC",path);
		while (c.moveToNext()) {
			String s = c.getString(c.getColumnIndex("PATH"));			
			if(path.equalsIgnoreCase(s)){
				Log.e("PLAYMUSIC",path);
				c.close();
				return i;
			}
			i++;
		}
		c.close();
		return -1;
	}
		
	//查找一条数据
//	public boolean QueryOneBad(String path) {
//		SQLiteDatabase db = getWritableDatabase();
//		Cursor c = db.rawQuery("select * from badmusic where PATH = ?",	new String[] { path });
//		if (c.moveToNext()) {
//			c.close();
//			return true;
//		} else {
//			c.close();
//			return false;
//		}
//	}
	
	public boolean QueryOneSdcard(String path) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.rawQuery("select * from sdcardmusic where PATH = ?", new String[] { path });
		if (c.moveToNext()) {
			c.close();
			return true;
		} else {
			c.close();
			return false;
		}
	}
	
	//更新一条记录	
//	public void UpdateOneSdcard(int up,String path) {
//		SQLiteDatabase db = getWritableDatabase();
//		String sql="update sdcardmusic set LOVE="+up+" where PATH='"+sqliteEscape(path)+"'";
//		db.execSQL(sql);
//	}
	
	public int UpdateSdcard(ContentValues cv,String Clause,String[]Args ) {
		SQLiteDatabase db = getWritableDatabase();
		return db.update(TABLE_SDCARD, cv, Clause,Args);
	}

	// 删除一条记录
	public int DeleteOneBad(String path) {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_BAD, "PATH=?" , new String[]{path});
		// String sql = "delete from playmusic where PATH = '"+path+"'";
		// db.execSQL(sql);
		// Log.i("TANG", path);
		// Log.i("TANG", sql);

	}
	
	public int DeleteOneSdcard(String path) {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_SDCARD, "PATH=?", new String[]{path});
//		String sql="DELETE FROM where PATH='"+sqliteEscape(path)+"'";
//		db.execSQL(sql);
	}

	// DROP TABLE IF EXISTS TABLE_NAME;删除表

	// 删除所有数据
	public int DeleteAllBad() {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_BAD, null, null);
		// SQLiteDatabase db = getWritableDatabase();
		// String sql1 = "DROP TABLE IF EXISTS playmusic";
		// db.execSQL(sql1);
		// String sql2
		// ="create table playmusic(PATH text , NAME text ,TIME text)";
		// db.execSQL(sql2);
	}
	
	public int DeleteAllSdcard() {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(TABLE_SDCARD, null, null);					
	}

	// 关闭数据库
	public void close() {
		if (db != null) {
			db.close();
		}
	}
//	public static String sqliteEscape(String keyWord){
//	    keyWord = keyWord.replace("/", "//");
//	    keyWord = keyWord.replace("'", "''");
//	    keyWord = keyWord.replace("[", "/[");
//	    keyWord = keyWord.replace("]", "/]");
//	    keyWord = keyWord.replace("%", "/%");
//	    keyWord = keyWord.replace("&","/&");
//	    keyWord = keyWord.replace("_", "/_");
//	    keyWord = keyWord.replace("(", "/(");
//	    keyWord = keyWord.replace(")", "/)");
//	    return keyWord;
//	}
	
	// 读取设置
		public int ReadSet(String mode) {
			int i = 1;
			SQLiteDatabase db = getWritableDatabase();
			Cursor c = db.rawQuery("select * from setmusic where MODE = ?", new String[] {mode});
			if(c.moveToNext()) {
				i = c.getInt(c.getColumnIndex("ST"));
			}
			else
			{
				ContentValues cv = new ContentValues();
				cv.put("MODE", mode);
				cv.put("ST", 1);
				db.insert(TABLE_SET, null, cv);
			}
			return i;
		}
			
		// 保存设置
		public int SaveSet(String mode,int set) {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put("ST", set);
			return db.update(TABLE_SET, cv, "MODE = ?",new String[]{mode});
		}

}
