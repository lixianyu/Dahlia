package us.nb9.dahlia;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

import weibo4andriod.Weibo;
import weibo4andriod.Status;
import weibo4andriod.Comment;
import weibo4andriod.WeiboException;
import weibo4andriod.org.json.JSONException;
import weibo4andriod.http.ImageItem;

import android.widget.Toast;
import android.os.SystemClock;


public class tswbService extends Service {
	private static int iCount = 0;
	private static tswbService mTswbService = null;
	static private final String TAG = "tswbService_lxy";
	int mYear, mMonth, mDay, mHour, mMinute;
	long mMs;
	String mContent;
	Bundle bundle;
	Uri mUri;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();
		mTswbService = this;
		mYear = 0;
		mMonth = 0;
		mDay = 0;
		mHour = 0;
		mMinute = 0;
		mMs = 0;
		mUri = Uri.parse("content://media/external/images/media/2");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart(), startId="+startId+", intent="+intent);
		super.onStart(intent, startId);

		//bundle = intent.getExtras();
		//int flag = bundle.getInt("now");
		int flag = intent.getExtras().getInt("now");
		Log.w(TAG, "flag = " + flag);
		if (flag == 2) { //从Receiver过来的，要发送一条微博：因为时间到了
			tswbDatabaseHelper mDBHelper = new tswbDatabaseHelper(this);
			SQLiteDatabase db = mDBHelper.getWritableDatabase();
			try {
				//sendNow(bundle);
				//long sentTime = intent.getExtras().getLong("sendTime");
				//Log.d(TAG, "sentTime = " + sentTime);
				sendNow(db);
				//db.close();
			} catch (WeiboException e) {
				e.printStackTrace();
			}
			return;
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		bundle = intent.getExtras();
		if (flag == 0) { //立即发送
			/*
			try {
				sendNow(bundle);
			} catch (WeiboException e) {
				e.printStackTrace();
			}*/
			mMs = bundle.getLong("sendMs");
			if (null != bundle.getString("uri")) {
				mUri = Uri.parse(bundle.getString("uri"));
			}
			else {
				mUri = null;
			}
			setTimingSendNow();
			return;
		}

		//////////////////////////////////////////////////////////////////////////////////
		if (flag == 1) { //  这是要预发微博，先设置一个闹钟
			mYear = bundle.getInt("year");
			mMonth = bundle.getInt("month");
			mDay = bundle.getInt("day");
			mHour = bundle.getInt("hour");
			mMinute = bundle.getInt("minute");
			mMs = bundle.getLong("sendMs");
			mContent = bundle.getString("content");
			setTimingSend();
			return;
		}

		//////////////////////////////////////////////////////////////////////////////////
		
		if (flag == 3) { //手机开机之后，如果预发队列有微博需要发送……
			long sentTime = intent.getExtras().getLong("sendTime");
			long nowMs = System.currentTimeMillis();
			Log.d(TAG, "sentTime = " + sentTime + ", nowMs = " + nowMs);
			if (nowMs > sentTime) {
				sentTime = nowMs + 25000;
			}
			setTimingSend(sentTime);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind(), intent="+intent);
		return null;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		Intent i = new Intent(this, tswbReceiver.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		am.cancel(pi);
		
		tswbDatabaseHelper mDBHelper = new tswbDatabaseHelper(this);
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		db.close();
		super.onDestroy();
	}

	public static tswbService getService() {
		return mTswbService;
	}

	private void setTimingSendNow() {
		Log.d(TAG, "Enter setTimingSendNow(), mMs = " + mMs);
		
		//获取AlarmManager
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		//获取当前的时间
		//Calendar c = Calendar.getInstance();
		//c.setTimeInMillis(System.currentTimeMillis());
		long msTemp = System.currentTimeMillis();
		//c.set(mYear, mMonth, mDay, mHour, mMinute, 0);

		// 只对秒 做修改 
		// c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		// c.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
		//c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 5);   //定时5秒
		//c.set(Calendar.MILLISECOND, 0);

		//long ms = c.getTimeInMillis();
		long ms = mMs;
		//设置消息的响应
		Intent ii = new Intent(this, tswbReceiver.class);
		//Bundle bundle = new Bundle();
		//bundle.putLong("sendTime", ms);
		//ii.putExtras(bundle);
		PendingIntent pii = PendingIntent.getBroadcast(this, 0, ii, 0);
		
		am.set(AlarmManager.RTC_WAKEUP, ms, pii);
		//使用Toast提示用户
		//Toast.makeText(this, "AlarmSet Finish.. HowManySeconds = "+ ((ms-msTemp)/1000), Toast.LENGTH_LONG).show();
		Toast.makeText(this, "开始发送……", Toast.LENGTH_LONG).show();
		Log.d(TAG, "Leave setTimingSendNow(), HowManySeconds = " + ((ms-msTemp)/1000));
		//SystemClock.sleep(3000);
	}
	
	private void setTimingSend() {
		Log.d(TAG, "Enter setTimingSend(), mMs = " + mMs);
		tswbDatabaseHelper mDBHelper = new tswbDatabaseHelper(this);
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String col[] = {tswbDatabaseHelper.FIELD_SEND_TIME };
		Cursor cur = db.query(tswbDatabaseHelper.TABLE_TIMING_SEND, col, null, null, null, null,
					tswbDatabaseHelper.FIELD_SEND_TIME);
		Integer n = cur.getCount();
		
		Log.d(TAG, "n = " + n);
		if (cur.moveToNext()) {
			long sendtimeMs = cur.getLong(0);
			Log.i(TAG, "sendtimeMs = " + sendtimeMs+", mMs = "+mMs);
			if (sendtimeMs <= mMs) {
				mMs = sendtimeMs;
			}
		}
		cur.close();
		db.close();
		
		//获取AlarmManager
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		//获取当前的时间
		//Calendar c = Calendar.getInstance();
		//c.setTimeInMillis(System.currentTimeMillis());
		long msTemp = System.currentTimeMillis();
		//c.set(mYear, mMonth, mDay, mHour, mMinute, 0);

		// 只对秒 做修改 
		// c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		// c.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
		//c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 5);   //定时5秒
		//c.set(Calendar.MILLISECOND, 0);

		//long ms = c.getTimeInMillis();
		long ms = mMs;
		//设置消息的响应
		Intent ii = new Intent(this, tswbReceiver.class);
		//Bundle bundle = new Bundle();
		//bundle.putLong("sendTime", ms);
		//ii.putExtras(bundle);
		PendingIntent pii = PendingIntent.getBroadcast(this, 0, ii, 0);
		
		am.set(AlarmManager.RTC_WAKEUP, ms, pii);
		//使用Toast提示用户
		Toast.makeText(this, "AlarmSet Finish.. HowManySeconds = "+ ((ms-msTemp)/1000), Toast.LENGTH_LONG).show();
		Log.d(TAG, "Leave setTimingSend(), HowManySeconds = " + ((ms-msTemp)/1000));
		//SystemClock.sleep(3000);
	}
	
	public void setTimingSend(long msNext) {
		Log.d(TAG, "Enter setTimingSend(), msNext = " + msNext);
		//获取AlarmManager
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		//获取当前的时间
		//Calendar c = Calendar.getInstance();
		//c.setTimeInMillis(msNext);
		long msTemp = System.currentTimeMillis();
		//c.set(mYear, mMonth, mDay, mHour, mMinute, 0);

		// 只对秒 做修改 
		// c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		// c.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
		//c.set(Calendar.SECOND, c.get(Calendar.SECOND) + (int)ms*60);   //定时70秒
		//c.set(Calendar.MILLISECOND, 0);

		//long ms = c.getTimeInMillis();
		//设置消息的响应
		Intent ii = new Intent(tswbService.this, tswbReceiver.class);
		//Bundle bundle = new Bundle();
		//bundle.putLong("sendTime", msNext);
		//ii.putExtras(bundle);
		PendingIntent pii = PendingIntent.getBroadcast(tswbService.this, 0, ii, 0);
		
		am.set(AlarmManager.RTC_WAKEUP, msNext, pii);
		//使用Toast提示用户
		//Toast.makeText(tswbService.this, "AlarmSet Finish.. HowManySeconds = "+ ((ms-msTemp)/1000), Toast.LENGTH_LONG).show();
		Log.d(TAG, "AlarmSet Finish.. HowManySeconds = "+ ((msNext-msTemp)/1000));
		//Log.d(TAG, "Leave setTimingSend_1_minute(), HowManySeconds = " + ((ms-msTemp)/1000));
		//SystemClock.sleep(3000);
	}

	static final String[] sTest = {
		"1.如果你不给自己烦恼，别人也永远不可能给你烦恼。因为你自己的内心，你放不下。",
		"2.人之所以痛苦，在于追求错误的东西。",
		"3.你永远要感谢给你逆境的众生。",
		"4.你永远要宽恕众生，不论他有多坏，甚至他伤害过你，你一定要放下，才能得到真正的快乐。",
		"5.当你快乐时，你要想这快乐不是永恒的。当你痛苦时，你要想这痛苦也不是永恒的。",
		"6.今日的执著，会造成明日的后悔。",
		"7.你可以拥有爱，但不要执著，因为分离是必然的。",
		"8.不要浪费你的生命在你一定会后悔的地方上。",
		"9.你什么时候放下，什么时候就没有烦恼。",
		"10.每一种创伤，都是一种成熟。"
	};
	private void insertOneTest(long createMs, long sendMs, String sWeibo, SQLiteDatabase db) {
		Log.v(TAG, "Enter insertOneTest(), sWeibo = " + sWeibo);
		sWeibo = sTest[(int)(Math.random()*10)];
		ContentValues values = new ContentValues();
    	values.put(tswbDatabaseHelper.FIELD_CREATE_TIME, createMs);
    	values.put(tswbDatabaseHelper.FIELD_SEND_TIME, sendMs);
    	values.put(tswbDatabaseHelper.FIELD_WEIBO_CONTENT, sWeibo);
    	values.put(tswbDatabaseHelper.FIELD_SEND_TO_SINA, 1);//1, 准备发送；2, 发送失败了
    	values.put(tswbDatabaseHelper.FIELD_SEND_TO_TENGXUN, 0);
    	values.put(tswbDatabaseHelper.FIELD_SEND_TO_SOHU, 0);
    	values.put(tswbDatabaseHelper.FIELD_SEND_TO_WANGYI, 0);

    	if (mUri == null) {
    		mUri = Uri.parse("content://media/external/images/media/3");
    	}
    	else {
    		mUri = Uri.parse("content://media/external/images/media/4");
    	}
    	StringBuilder haha = new StringBuilder(mUri.toString());
    	int idx = haha.lastIndexOf("/");
    	int len = haha.length();
    	haha.delete(idx+1, len);
    	haha.append((int)(Math.random()*117+1));
    	Uri uriHa = Uri.parse(haha.toString());
    	Log.v(TAG, "uriHa = " + uriHa);
/*
    	ContentResolver cr = getContentResolver();
    	InputStream inStream = null;
		try {
			inStream = cr.openInputStream(uriHa);
			Log.d(TAG, "inStream = " + inStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Bitmap photo = BitmapFactory.decodeStream(inStream);
		ByteArrayOutputStream mBaos = new ByteArrayOutputStream();
		photo.compress(CompressFormat.JPEG, 99, mBaos);
    	
    	values.put(tswbDatabaseHelper.FIELD_CONTENTS_PIC, mBaos.toByteArray());
*/
    	values.put(tswbDatabaseHelper.FIELD_CONTENTS_PIC_URI, uriHa.toString());
    	long lInsertReturn = db.insert(tswbDatabaseHelper.TABLE_TIMING_SEND, null, values);
    	if (lInsertReturn != -1)
    	{
    		Log.d(TAG, "db.insert sucess!!");
    	}
    	else
    	{
    		Log.d(TAG, "db.insert error!!");
    	}
	}
	
	public void sentToSina(SQLiteDatabase db) {
		
	}

	public ContentValues[] getUserIDPassword(SQLiteDatabase db, String sWeiboID) {
		
		String col[] = {tswbDatabaseHelper.FIELD_WEIBO_ID, 
				tswbDatabaseHelper.FIELD_USER_ID,
				tswbDatabaseHelper.FIELD_PASSWORD,
				tswbDatabaseHelper.FIELD_SELECTED,
				};
		String selection = "WeiboID=?";
    	String selectionArgs[] = {sWeiboID};
		Cursor cursor = db.query(tswbDatabaseHelper.TABLE_ACCOUNT, 
				col,
				selection,
				selectionArgs,
				null,
				null,
				null
				);
		Log.i(TAG, "cursor.getCount() = " + cursor.getCount());

		int i = 0;
		final int count = cursor.getCount();
		ContentValues []value = new ContentValues[count];
		//ContentValues aValues = new ContentValues();
		Log.i(TAG, "value.length = " + value.length);
		while (cursor.moveToNext()) {
			value[i] = new ContentValues();
			value[i].put(tswbDatabaseHelper.FIELD_WEIBO_ID, cursor.getString(0));
			value[i].put(tswbDatabaseHelper.FIELD_USER_ID, cursor.getString(1));
			value[i].put(tswbDatabaseHelper.FIELD_PASSWORD, cursor.getString(2));
			value[i].put(tswbDatabaseHelper.FIELD_SELECTED, cursor.getInt(3));
			i++;
		}
		cursor.close();
		//value[0] = aValues;
		return value;
	}
	
	private void sendNow(final SQLiteDatabase db) throws WeiboException {
		//setTimingSend(5);
		new Thread() {
			public void run() {
				try {
					long l1 = System.currentTimeMillis();
					//byte bytePhoto[] = new byte[1024*1024];
					byte bytePhoto[] = null;
					Uri uri = null;
					int iSendTo[] = {0, 0, 0, 0};
					long senttimeNext = 0;
					long createtimeNext = 0;
					long senttime = 0;
					long createtime = 0;
					String sWeibo = null;
					String col[] = {tswbDatabaseHelper.FIELD_SEND_TIME, 
							tswbDatabaseHelper.FIELD_CREATE_TIME,
							tswbDatabaseHelper.FIELD_WEIBO_CONTENT,
							tswbDatabaseHelper.FIELD_SEND_TO_SINA,
							tswbDatabaseHelper.FIELD_SEND_TO_TENGXUN,
							tswbDatabaseHelper.FIELD_SEND_TO_SOHU,
							tswbDatabaseHelper.FIELD_SEND_TO_WANGYI,
							tswbDatabaseHelper.FIELD_CONTENTS_PIC_URI
							};
					Cursor cursor = db.query(tswbDatabaseHelper.TABLE_TIMING_SEND, 
							col, 
							null, 
							null, 
							null, 
							null, 
							tswbDatabaseHelper.FIELD_SEND_TIME+", "+ 
							tswbDatabaseHelper.FIELD_CREATE_TIME
							);
							//tswbDatabaseHelper.FIELD_CREATE_TIME);
					Log.d(TAG, "cursor.getCount() = " + cursor.getCount());
					int i = 0;
					while (cursor.moveToNext()) {
						if (i > 1) break;

						Log.d(TAG, "Enter while cursor.moveToNext()...");
						if (0 == i)
						{
							senttime = cursor.getLong(0);
							createtime = cursor.getLong(1);
							Log.d(TAG, "senttime = " + senttime+ ", createtime = "+createtime);
							Log.d(TAG, "iSendTo.length = " + iSendTo.length);
							for (int j = 0; j < iSendTo.length; j++) {
								iSendTo[j] = cursor.getInt(j+3);
								Log.d(TAG, "iSendTo[" + j + "] = " + iSendTo[j]);
							}
							sWeibo = cursor.getString(2);
							//bytePhoto = cursor.getBlob(7);
							String uriString = cursor.getString(7);
							if (uriString != null) {
								uri = Uri.parse(cursor.getString(7));
							}
							else {
								uri = null;
							}
						}
						else if (1 == i)
						{
							senttimeNext = cursor.getLong(0);
							createtimeNext = cursor.getLong(1);
							Log.d(TAG, "senttimeNext = " + senttimeNext+ ", createtimeNext = "+createtimeNext);
							
						}
						Log.d(TAG, "cursor.getString(2) = " + cursor.getString(2));
						++i;
		            }
					cursor.close();
					//String sWeibo = bundle.getString("content");
					if (iSendTo[0] == 1) {
						//sendToSina(sWeibo, bytePhoto, createtime, senttimeNext, db);
						sendToSina(sWeibo, uri, createtime, senttimeNext, db);
					}

					long l2 = System.currentTimeMillis();
					Log.w(TAG, "Time elapsed all weibo sent : " + (l2 - l1));
				    db.close();
					}catch (Exception e) { //既然发送失败了，就隔一段时间再发之
						db.close();
						e.printStackTrace();
						Log.e(TAG, "haha2...");
						
						Calendar c = Calendar.getInstance();
				    	long nowMs = System.currentTimeMillis();
				    	c.setTimeInMillis(nowMs);
			    		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 3);
				    	mTswbService.setTimingSend(c.getTimeInMillis());
					}
				}
		}.start();
	}

	private void sendToSina(String sWeibo, byte bytes[], long createtime, long senttimeNext, SQLiteDatabase db)
		throws WeiboException {
		Log.i(TAG, "Enter sendToSina()");
		long l1 = System.currentTimeMillis();

		ContentValues values[] = getUserIDPassword(db, "新浪");
		String userID = values[0].getAsString(tswbDatabaseHelper.FIELD_USER_ID);
		String passward = values[0].getAsString(tswbDatabaseHelper.FIELD_PASSWORD);
        Log.i(TAG, "userID="+userID+", passward="+passward);

        Weibo weibo = new Weibo(userID, passward);

        String msg = sWeibo + " 我在这里" +iCount + "： " + new java.util.Date();
        iCount++;
        Log.w(TAG, "msg = " + msg);

        Status status = null;
	    try {
	    	double lat, lon;
	    	lat = Math.random()*90;
	    	//lon = Math.random()*62 + 74;
	    	lon = Math.random()*180;
	    	Log.w(TAG, "lat = "+lat+", lon = " + lon);
	    	
	    	int cc = (int)(Math.random()*10);
	    	if (cc < 5) {
	    		lat = -lat;
	    	}
	    	int cc1 = (int)(Math.random()*10);
	    	if (cc1 < 5) {
	    		lon = -lon;
	    	}
	    	//lat = 39.9741;
	    	Log.w(TAG, "lat = "+lat+", lon = " + lon + ", bytes="+bytes);
	    	msg += " lat = "+lat+",  lon = "+lon;
	    	if (bytes != null) {
	    		/*
	    		File file = new File("picTemp.jpg");
	    		try {
					file.createNewFile();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
*/
	    		/*
	    		FileOutputStream oStream = null;
				try {
					//oStream = this.openFileOutput("picTemp.jpg", Context.MODE_WORLD_WRITEABLE);
					oStream = this.openFileOutput("picTemp.jpeg", Context.MODE_APPEND);
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}
				
	    		try {
					oStream.write(bytes);
					oStream.flush();
					oStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				File file = new File("picTemp.jpeg");
				//file.
				String filePath = file.getPath();
				Log.d(TAG, "filePath = " + filePath + ", file = "+ file);
				
	    		status = weibo.uploadStatus(msg, file);*/
	    		
	    		ImageItem pic = null;
				try {
					pic = new ImageItem("pic", bytes);
				} catch (Exception e) {
					e.printStackTrace();
				}

	    		//status = weibo.uploadStatus(msg, pic);
				
				status = weibo.uploadStatus(URLEncoder.encode(msg), pic, lat, lon);
	    	}
	    	else {
	    		status = weibo.updateStatus(msg, lat, lon);
	    	}
	    } catch (JSONException e1) {
	    	Log.e(TAG, "haha1...");
	    	e1.printStackTrace();
	    }

        long l2 = System.currentTimeMillis();

        Log.w(TAG, "Successfully updated the status to [" + status.getText() + "].");
        Log.w(TAG, "Time elapsed: " + (l2 - l1));
        
        int iDBdeleteReturn = 0;
        iDBdeleteReturn = db.delete(tswbDatabaseHelper.TABLE_TIMING_SEND, 
        		tswbDatabaseHelper.FIELD_CREATE_TIME + "=" + createtime,
        		null);
        Log.d(TAG, "iDBdeleteReturn = " + iDBdeleteReturn);
        
        try {
	        	Thread.sleep(1000); // avoid flush server
	        } catch (InterruptedException e) {
	        	e.printStackTrace();
	        }
	    if (senttimeNext != 0)
	    {
	    	Calendar c = Calendar.getInstance();
	    	long nowMs = System.currentTimeMillis();
	    	if (nowMs >= senttimeNext) {
	    		c.setTimeInMillis(nowMs);
	    		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 2);
	    		senttimeNext = c.getTimeInMillis();
	    	}
	    	Log.i(TAG, "senttimeNext="+senttimeNext+", nowMs="+nowMs);
	    	mTswbService.setTimingSend(senttimeNext);
	    }
	    else //Just for test.
	    {
	    	Calendar c = Calendar.getInstance();
	    	long nowMs = System.currentTimeMillis();
	    	c.setTimeInMillis(nowMs);
    		c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 135);
    		insertOneTest(createtime, c.getTimeInMillis(), sWeibo, db);
	    	mTswbService.setTimingSend(c.getTimeInMillis());
	    	
	    }
	    Log.i(TAG, "Leave sendToSina()");
	}

	private void sendToSina(String sWeibo, Uri uri, long createtime, long senttimeNext, SQLiteDatabase db)
					throws WeiboException, IOException {
		Log.i(TAG, "Enter sendToSina()..1, uri = "+uri);
		iCount++;
		Weibo weibo;
		long l1 = System.currentTimeMillis();
		double lat = 0, lon = 0;
		String msg = null;
		ImageItem pic = null;

		ContentValues values[] = getUserIDPassword(db, "新浪");
		int sinaAccountCount = values.length;
		while (sinaAccountCount > 0) {
			sinaAccountCount--;
			Log.v(TAG, "sinaAccountCount = " + sinaAccountCount);
			String userID = values[sinaAccountCount].getAsString(tswbDatabaseHelper.FIELD_USER_ID);
			String passward = values[sinaAccountCount].getAsString(tswbDatabaseHelper.FIELD_PASSWORD);
		    Log.i(TAG, "userID="+userID+", passward="+passward);

		    weibo = new Weibo(userID, passward);

		    Status status = null;
		    try {
		    	if (sinaAccountCount+1 == values.length) {
			    	lat = Math.random()*90;
			    	//lon = Math.random()*62 + 74;
			    	lon = Math.random()*180;
			    	Log.w(TAG, "lat = "+lat+", lon = " + lon);
		
			    	int cc = (int)(Math.random()*10);
			    	if (cc < 5) {
			    		lat = -lat;
			    	}
			    	int cc1 = (int)(Math.random()*10);
			    	if (cc1 < 5) {
			    		lon = -lon;
			    	}
			    	//lat = 39.9741;
			    	Log.w(TAG, "lat = "+lat+", lon = " + lon);
			    	msg = sWeibo + " 我在这里" +iCount + "： " + new java.util.Date();
			    	msg += " lat = "+lat+",  lon = "+lon;
			    	Log.w(TAG, "msg = " + msg);
		    	}
		    	if (uri != null) {
		    		if (sinaAccountCount+1 == values.length) {
			    		ContentResolver cr = getContentResolver();
			    		Log.d(TAG, "cr = " + cr);
			    		InputStream inStream = null;
			    		int count = 0;
			    		try {
							inStream = cr.openInputStream(uri);
							Log.d(TAG, "inStream = " + inStream);
							count = inStream.available();
						} catch (FileNotFoundException e) {
							Log.w(TAG, "Oh, inStream has nothings");
							StringBuilder haha = new StringBuilder("content://media/external/images/media/3");
					    	int idx = haha.lastIndexOf("/");
					    	int len = haha.length();
					    	haha.delete(idx+1, len);
					    	haha.append((int)(Math.random()*20+1));
					    	Uri uriHa = Uri.parse(haha.toString());
					    	Log.w(TAG, "uriHa = " + uriHa);
	
					    	ContentValues values1 = new ContentValues();
					    	values1.put(tswbDatabaseHelper.FIELD_CONTENTS_PIC_URI, uriHa.toString());
					    	int iUpdate = db.update(tswbDatabaseHelper.TABLE_TIMING_SEND,
					    			  values1,
					    			  tswbDatabaseHelper.FIELD_CREATE_TIME+"=?",
					    			  new String[] {String.valueOf(createtime)});
					    	Log.w(TAG, "iUpdate = "+iUpdate);
						}
	
						Log.v(TAG, "count = "+count);
						byte[] bytes = new byte[count+5*1024];
						try {
							count = inStream.read(bytes);
							Log.d(TAG, "read count = " + count);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
	
			    		pic = null;
						try {
							pic = new ImageItem("pic", bytes);
						} catch (Exception e) {
							e.printStackTrace();
						}
		    		}
					status = weibo.uploadStatus(URLEncoder.encode(msg), pic, lat, lon);
		    	}
		    	else {
		    		status = weibo.updateStatus(msg, lat, lon);
		    	}
		    } catch (JSONException e1) {
		    	Log.e(TAG, "haha1...");
		    	e1.printStackTrace();
		    }

		    long l2 = System.currentTimeMillis();
		    Log.w(TAG, "Time elapsed: " + (l2 - l1));
		    Log.w(TAG, "Successfully updated the status to [" + status.getText() + "].");

		    if (sinaAccountCount == 0) {
			    int iDBdeleteReturn = 0;
			    iDBdeleteReturn = db.delete(tswbDatabaseHelper.TABLE_TIMING_SEND, 
			    		tswbDatabaseHelper.FIELD_CREATE_TIME + "=" + createtime,
			    		null);
			    Log.d(TAG, "iDBdeleteReturn = " + iDBdeleteReturn);
		    }
		    try {
		        	Thread.sleep(1000); // avoid flush server
		    } catch (InterruptedException e) {
		        	e.printStackTrace();
		    }

		    if (sinaAccountCount == 0) {
		       	if (senttimeNext != 0) {
			    	Calendar c = Calendar.getInstance();
			    	long nowMs = System.currentTimeMillis();
			    	if (nowMs >= senttimeNext) {
			    		c.setTimeInMillis(nowMs);
			    		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 2);
			    		senttimeNext = c.getTimeInMillis();
			    	}
			    	Log.i(TAG, "senttimeNext="+senttimeNext+", nowMs="+nowMs);
			    	mTswbService.setTimingSend(senttimeNext);
			    }
			    else //Just for test.
			    {
			    	Calendar c = Calendar.getInstance();
			    	long nowMs = System.currentTimeMillis();
			    	c.setTimeInMillis(nowMs);
					//c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 135*2+30);
			    	c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 30);
					insertOneTest(createtime, c.getTimeInMillis(), sWeibo, db);
			    	mTswbService.setTimingSend(c.getTimeInMillis());
			    }
		    }
		}
		Log.i(TAG, "Leave sendToSina()...1");
	}

	
	
	
	
	
	private void sendNow(final Bundle bundle) throws WeiboException {
		setTimingSend(5);
		new Thread() {
			public void run() {
				try {
					String sWeibo = bundle.getString("content");
			    	long l1 = System.currentTimeMillis();
			        String userID = "xianyu219@gmail.com";
			        String passward = "219615lxylr";
			        //String userID = "lixianyu2010@139.com";
			        //String passward = "219615lxylr";
			        
			    	Weibo weibo = new Weibo(userID, passward);
			//        System.out.println(weibo.getRateLimitStatus());
			/*
			        List<Status> statuses = weibo.getPublicTimeline();
			
			        for (Status status : statuses) {
			        	Log.w(TAG, status.toString());
			    	}
				*/
			        String msg = sWeibo + "我在这里" +iCount + "： " + new java.util.Date();
			        iCount++;
			        Log.w(TAG, "msg = " + msg);

			        Status status = null;
			        // normal status
			        //status = weibo.updateStatus(sWeibo + System.currentTimeMillis());
			        // status with geocode
				    try {
				    	double lat, lon;
				    	lat = Math.random()*90;
				    	//lon = Math.random()*62 + 74;
				    	lon = Math.random()*180;
				    	Log.w(TAG, "lat = "+lat+", lon = " + lon);
				    	
				    	int cc = (int)(Math.random()*10);
				    	if (cc < 5) {
				    		lat = -lat;
				    	}
				    	int cc1 = (int)(Math.random()*10);
				    	if (cc1 < 5) {
				    		lon = -lon;
				    	}
				    	//lat = 39.9741;
				    	Log.w(TAG, "lat = "+lat+", lon = " + lon);
				    	msg += " lat = "+lat+",   lon = "+lon;
				    	status = weibo.updateStatus(msg, lat, lon); 
				    	//status = weibo.updateStatus(msg, 40.7579, -73.985);
				    	//status = weibo.updateStatus(msg, 39.9841, 116.485);
				    	//status = weibo.updateStatus(msg, 39.9841, 125.027);
				    } catch (JSONException e1) {
				    	e1.printStackTrace();
				    }
			
			        long l2 = System.currentTimeMillis();
			
			        Log.w(TAG, "Successfully updated the status to [" + status.getText() + "].");
			        Log.w(TAG, "Time elapsed: " + (l2 - l1));
			
			        try {
			        	Thread.sleep(1000); // avoid flush server
			        } catch (InterruptedException e) {
			        	e.printStackTrace();
			        }
			        
			        // add a comment for the status
			        /*
			        long sid = status.getId();
			        Comment cmt = weibo.updateComment("评论1 " + new java.util.Date(), String.valueOf(sid), null);
			
			        weibo.getComments(String.valueOf(sid));
			
			        weibo.updateComment("评论2 " + new java.util.Date(),  String.valueOf(sid), null);
			
			        try {
			        	Thread.sleep(1000); // avoid flush server
			        } catch (InterruptedException e) {
			        	e.printStackTrace();
			        }
			        Comment cmt2 = weibo.destroyComment(cmt.getId());
			        Log.w(TAG, "delete " + cmt2);
			        */
					}catch (Exception e) {e.printStackTrace();}
				}
		}.start();
	}
}