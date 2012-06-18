package us.nb9.dahlia;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.text.TextUtils;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import weibo4andriod.Weibo;
import weibo4andriod.Status;
import weibo4andriod.Comment;
import weibo4andriod.WeiboException;
import weibo4andriod.org.json.JSONException;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

//import java.util.List;



public class TimingSendWeiBo extends Activity implements OnClickListener,OnCheckedChangeListener
{
	static private final String TAG = "TimingSendWeiBo_lxy";
	public static final int ACCOUNT_ID = Menu.FIRST;
	public static final int TIMING_SEND_QUEUE_ID = Menu.FIRST + 1;
	public static final int ABOUT_ID = Menu.FIRST + 2;
	public static final int RESET_ID = Menu.FIRST + 3;
	public static final int EXIT_ID = Menu.FIRST + 4;

	static final int REQUEST_USERID_PASSWORD = 0;
	static final int REQUEST_USERID_PIC = 1;
	static final int REQUEST_USERID_VIDEO = 2;
	static final int REQUEST_USERID_TAKE_PHOTO = 3;
	static final int REQUEST_USERID_TAKE_VIDEO = 4;
	
	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	static final int SINA_USERID_PASSWORD_DIALOG_ID = 2;
	private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
	String sFIC;
	TextView tv;
	Button btnSendWeibo, btnTimingSendWeibo, btnInsertPic, btnInsertVideo, btnTakePhoto, btnTakeVideo;
	Button btnDelPic;
	EditText TextEditWeibo;
	Uri photo_uri;
	
	private boolean[] status = {true, false, false, false};
	public int curStatus;
	static final int mWeiboCount = 1; //目前支持4个微博
	private int mFlag;
	
	private tswbDatabaseHelper mDBHelper;
	ByteArrayOutputStream mBaos;
	Uri mUri;
	String mUriString;
	
	public void onStart() {
		super.onStart();
		Log.i(TAG, "Enter onStart()");
		
	}
	public void onRestart() {
		super.onRestart();
		Log.i(TAG, "Enter onRestart()");
	}
	public void onResume() {
		super.onResume();
		Log.i(TAG, "Enter onResume()");
	}
	public void onPause() {
		super.onPause();
		Log.i(TAG, "Enter onPause()");
	}
	public void onStop() {
		super.onStop();
		Log.i(TAG, "Enter onStop()");
		mDBHelper = new tswbDatabaseHelper(this);
    	SQLiteDatabase db = mDBHelper.getWritableDatabase();
    	db.close();
	}
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Enter onDestroy()");
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Enter onCreate(), savedInstanceState = " + savedInstanceState);
        LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout)inflate.inflate(R.layout.main, null);
        layout.setBackgroundResource(R.drawable.back);
        setContentView(layout);
        
        CheckBox sinaCheckBox = (CheckBox) findViewById(R.id.checkbox_sina);
        sinaCheckBox.setChecked(true);
        sinaCheckBox.setOnCheckedChangeListener(this);
        
        CheckBox tengxunCheckBox = (CheckBox) findViewById(R.id.checkbox_tengxun);
        tengxunCheckBox.setEnabled(false);
        tengxunCheckBox.setChecked(false);
        tengxunCheckBox.setOnCheckedChangeListener(this);
        
        CheckBox sohuCheckBox = (CheckBox) findViewById(R.id.checkbox_sohu);
        sohuCheckBox.setEnabled(false);
        sohuCheckBox.setChecked(false);
        sohuCheckBox.setOnCheckedChangeListener(this);
        
        CheckBox wangyiCheckBox = (CheckBox) findViewById(R.id.checkbox_wangyi);
        wangyiCheckBox.setEnabled(false);
        wangyiCheckBox.setChecked(false);
        wangyiCheckBox.setOnCheckedChangeListener(this);
        
        btnSendWeibo = (Button)findViewById(R.id.send_weibo);
        btnSendWeibo.setOnClickListener(this);
        btnTimingSendWeibo = (Button)findViewById(R.id.send_weibo_timing);
        btnTimingSendWeibo.setOnClickListener(this);
        
        btnInsertPic = (Button)findViewById(R.id.insert_pic);
        btnInsertPic.setOnClickListener(this);
//        btnInsertVideo = (Button)findViewById(R.id.insert_video);
//        btnInsertVideo.setOnClickListener(this);
        btnTakePhoto = (Button)findViewById(R.id.insert_take_photo);
        btnTakePhoto.setOnClickListener(this);
//        btnTakeVideo = (Button)findViewById(R.id.insert_take_video);
//        btnTakeVideo.setOnClickListener(this);
        btnDelPic = (Button)findViewById(R.id.del_pic);
        btnDelPic.setOnClickListener(this);
        btnDelPic.setEnabled(false);

        TextEditWeibo = (EditText)findViewById(R.id.edit_weibo);
        
        tv = (TextView)findViewById(R.id.textviewmy);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(20);
        //setContentView(R.layout.main);
        
        Resources resourceString = this.getResources();
        sFIC = resourceString.getString(R.string.firstInputContent);
        
        mUri = null;
        mUriString = null;
    }

    public void onClick(View arg0) {
    	switch (arg0.getId()) {
    		case R.id.send_weibo_timing:
	    		Log.v(TAG, "send_weibo_timing");
	    		if (TextUtils.isEmpty(TextEditWeibo.getText())) {
	                Toast.makeText(this, sFIC, Toast.LENGTH_SHORT).show();
	                Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
	                TextEditWeibo.startAnimation(shake);
//	                tswbCrypto crypto = new tswbCrypto(this);
//	        		crypto.myCryptoTest();
	                break;
	            }
	    		else if (isAllCheckboxFalse()) {
	    			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
	    			findViewById(R.id.table_layout_checkbox).startAnimation(shake);
	    			Toast.makeText(this, "请至少先选择一个微博", Toast.LENGTH_SHORT).show();
	    			break;
	    		}
	    		Date today = new Date();
	    		Log.d(TAG, "times=" + today.getTime()+ ", date="+today.getDate()+ ", year="+today.getYear()+", month="+today.getMonth()+ ", day="+today.getDay()+", hour="+today.getHours()+", minute="+today.getMinutes()+"...");
	    		//获得当前时间
	            Calendar c = Calendar.getInstance();
	            mYear = c.get(Calendar.YEAR);
	            mMonth = c.get(Calendar.MONTH);
	            mDay = c.get(Calendar.DAY_OF_MONTH);
	            mHour = c.get(Calendar.HOUR_OF_DAY);
	            mMinute = c.get(Calendar.MINUTE);
	            updateDisplay();
	            //showDialog(TIME_DIALOG_ID);
	            //SystemClock.sleep(3000);
	            showDialog(DATE_DIALOG_ID);
	    		break;

	    	case R.id.send_weibo:
	    		Log.v(TAG, "R.id.send_weibo");
	    		if (TextUtils.isEmpty(TextEditWeibo.getText())) {
	    			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
	                TextEditWeibo.startAnimation(shake);
	                Toast.makeText(this, sFIC, Toast.LENGTH_SHORT).show();
	                break;
	            }
	    		else if (isAllCheckboxFalse()) {
	    			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
	    			findViewById(R.id.table_layout_checkbox).startAnimation(shake);
	    			Toast.makeText(this, "请至少先选择一个微博", Toast.LENGTH_SHORT).show();
	    			break;
	    		}
	    		curStatus = 0;
	    		mFlag = 0;
	    		beforeSendWeibo(mFlag);
	    		/*
	    		asdf;
	    		if (checkUserIDPassword()) {
	    			sendWeiBoLaterOrNow(TextEditWeibo.getText().toString(), 0);
	    		}
	    		else {
	    			int i = 0;
	    			while (status[i] == false) {
	    				i++;
	    			}
	    			showDialog(SINA_USERID_PASSWORD_DIALOG_ID);
	    		}
	    		*/
	    		break;
	    		
	    	case R.id.insert_pic:
	    		/*
	    		Intent intent = new Intent();
	    		intent.setClassName("com.android.camera", "android.camera.GalleryPicker");
	    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	    		startActivityForResult(intent, REQUEST_USERID_PIC);
	    		*/
	    		Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
	    		innerIntent.setType("image/*");
	    		Intent wrapperIntent = Intent.createChooser(innerIntent, null);
	    		startActivityForResult(wrapperIntent, REQUEST_USERID_PIC);
	    		break;
/*
	    	case R.id.insert_video:
	    		innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
	    		innerIntent.setType("video/*");
	    		wrapperIntent = Intent.createChooser(innerIntent, null);
	    		startActivityForResult(wrapperIntent, REQUEST_USERID_VIDEO);
	    		break;
*/
	    	case R.id.insert_take_photo:
	    		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //"android.media.action.IMAGE_CAPTURE";
	    		//intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("content://mms/scrapSpace")); // output,Uri.parse("content://mms/scrapSpace");
	    		File out = new File(Environment.getExternalStorageDirectory(), "TimingSendCamera.jpg");
	    		photo_uri = Uri.fromFile(out);
	    		Log.i(TAG, "photo_uri = " + photo_uri);
	    		intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_uri);
	    		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

	    		startActivityForResult(intent, REQUEST_USERID_TAKE_PHOTO);
	    		break;
/*
	    	case R.id.insert_take_video:
	    		//int durationLimit = getVideoCaptureDurationLimit(); //SystemProperties.getInt("ro.media.enc.lprof.duration", 60);
	    		intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	    		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
	    		intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 100*1024);
	    		//intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit);
	    		startActivityForResult(intent, REQUEST_USERID_TAKE_VIDEO);
	    		break;
*/
	    	case R.id.del_pic:
	    		mUri = null;
	    		mUriString = null;
	    		btnDelPic.setEnabled(false);
	    		break;
    	}
    	/*
    	if (arg0 == btnSendWeibo)
    	{
    		if (TextUtils.isEmpty(TextEditWeibo.getText())) {
                Toast.makeText(this, "Please enter a message body.",
                        Toast.LENGTH_SHORT).show();
                //return;
            }
    	}
    	else if (arg0 == btnTimingSendWeibo)
    	{
    		if (TextUtils.isEmpty(TextEditWeibo.getText())) {
                Toast.makeText(this, "Please enter a message body.",
                        Toast.LENGTH_SHORT).show();
                //return;
            }
    	}
    	*/
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	Log.v(TAG, "isChecked="+isChecked);
    	switch (buttonView.getId()) {
	    	case R.id.checkbox_sina:
	    		Log.v(TAG, "status[0]="+status[0]);
	    		status[0]=isChecked;
	    		break;
	    	case R.id.checkbox_tengxun:
	    		Log.v(TAG, "status[1]="+status[1]);
	    		status[1]=isChecked;
	    		break;
	    	case R.id.checkbox_sohu:
	    		Log.v(TAG, "status[2]="+status[2]);
	    		status[2]=isChecked;
	    		break;
	    	case R.id.checkbox_wangyi:
	    		Log.v(TAG, "status[3]="+status[3]);
	    		status[3]=isChecked;
	    		break;
    	}
    }

    boolean isAllCheckboxFalse() {
    	for(int i=0; i< status.length ; i++) {
    		if (status[i] == true) {
    			return false;
    		}
    	}
    	return true;
    }

    private void updateDisplay() {
        tv.setText(
            new StringBuilder()
            		.append(mYear).append("-")
                    // Month is 0 based so add 1
                    .append(mMonth + 1).append("-")
                    .append(mDay).append(" ")
                    .append(mHour).append(":")
                    .append(mMinute).append(" ")
                    );
    }

    public int getStringID(int iWeiboID) {
    	Log.i(TAG, "Enter getStringID(), iWeiboID = " + iWeiboID);
    	int iStringID = 0;
    	switch (iWeiboID) {
    	case 0:
    		iStringID = R.string.alert_dialog_text_entry_sina;
    		break;
    	case 1:
    		iStringID = R.string.alert_dialog_text_entry_tengxun;
    		break;
    	case 2:
    		iStringID = R.string.alert_dialog_text_entry_sohu;
    		break;
    	case 3:
    		iStringID = R.string.alert_dialog_text_entry_wangyi;
    		break;
    	}
    	return iStringID;
    }

    @Override
    protected Dialog onCreateDialog(int id) {   
        switch (id) {   
        case DATE_DIALOG_ID:   
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        case TIME_DIALOG_ID:
        	return new TimePickerDialog(this,
        			mTimeSetListener,
        			mHour, mMinute, true);
/*
        case SINA_USERID_PASSWORD_DIALOG_ID:
        	// This example shows how to add a custom layout to an AlertDialog
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
            int iStringID = getStringID(curStatus);
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(iStringID)
                .setView(textEntryView)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.i(TAG, "User clicked OK so do some stuff, whichButton = "+whichButton);
                        //LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        //LinearLayout layout = (LinearLayout)inflate.inflate(R.layout.alert_dialog_text_entry, null);
                        EditText username_edit = (EditText)textEntryView.findViewById(R.id.username_edit);
                        EditText password_view = (EditText)textEntryView.findViewById(R.id.password_edit);
                        String username = username_edit.getText().toString();
                        String password = password_view.getText().toString();
                        insertNewAccount(username, password);
                        
                        curStatus++;
                        beforeSendWeibo(mFlag);
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.i(TAG, "User clicked cancel so do some stuff. whichButton = "+whichButton);
                    }
                })
                .create();
                */
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year,
                     int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                updateDisplay();
                showDialog(TIME_DIALOG_ID);
            }
    };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hour, int minute) {
                mHour = hour;
                mMinute = minute;
                updateDisplay();
                
                sendWeiBoLaterOrNow(TextEditWeibo.getText().toString(), 1);
            }
    };

    // flag = 1, 延迟发送； flag = 0, 立即发送
    private void sendWeiBoLaterOrNow(String sWeibo, int flag) {
    	Log.d(TAG, "Enter sendWeiBoLaterOrNow(), sWeibo = "+sWeibo+", flag = " + flag);
    	
    	mDBHelper = new tswbDatabaseHelper(this);
    	SQLiteDatabase db = mDBHelper.getWritableDatabase();
    	
    	long createMs = System.currentTimeMillis();
    	Log.d(TAG, "createMs = " + createMs);
    	Calendar c = Calendar.getInstance();
    	if (flag == 0)
    	{
    		c.setTimeInMillis(createMs);
    		c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 3); 
    	}
    	else
    	{
    		c.set(mYear, mMonth, mDay, mHour, mMinute, 0);
    	}
    	long sendMs = c.getTimeInMillis();
    	Log.d(TAG, "sendMs =   " + sendMs);
    	
    	ContentValues values = new ContentValues();
    	values.put(tswbDatabaseHelper.FIELD_CREATE_TIME, createMs);
    	values.put(tswbDatabaseHelper.FIELD_SEND_TIME, sendMs);
    	values.put(tswbDatabaseHelper.FIELD_WEIBO_CONTENT, sWeibo);
    	values.put(tswbDatabaseHelper.FIELD_SEND_TO_SINA, 1);//0, 不发送该帐号；1, 准备发送；2, 发送失败了
    	values.put(tswbDatabaseHelper.FIELD_SEND_TO_TENGXUN, 0);
    	values.put(tswbDatabaseHelper.FIELD_SEND_TO_SOHU, 0);
    	values.put(tswbDatabaseHelper.FIELD_SEND_TO_WANGYI, 0);
    	/*
    	if (mBaos != null)
    		values.put(tswbDatabaseHelper.FIELD_CONTENTS_PIC, mBaos.toByteArray());
    		*/
    	if (mUriString != null) {
    		values.put(tswbDatabaseHelper.FIELD_CONTENTS_PIC_URI, mUriString);
    	}
    	
    	long lInsertReturn = db.insert(tswbDatabaseHelper.TABLE_TIMING_SEND, null, values);
    	if (lInsertReturn != -1)
    	{
    		Log.i(TAG, "db.insert sucess!!");
    	}
    	else
    	{
    		Log.e(TAG, "db.insert error!!");
    	}
    	//db.close();
    	
    	Intent intent = new Intent(this, tswbService.class);
    	Bundle bundle = new Bundle();
    	bundle.putInt("now", flag);
    	bundle.putInt("year", mYear);
    	bundle.putInt("month", mMonth);
    	bundle.putInt("day", mDay);
    	bundle.putInt("hour", mHour);
    	bundle.putInt("minute", mMinute);
    	bundle.putLong("sendMs", sendMs);
    	bundle.putString("content", sWeibo);
    	if (mUri != null)
    	{
    		bundle.putString("uri", mUri.toString());
    	}
    	intent.putExtras(bundle);
    	startService(intent);
    	
    	db.close();
//    	mUri = null;
//    	mUriString = null;
    }

    private void sendWeiBoNow(String sWeibo) {
		Intent intent = new Intent(TimingSendWeiBo.this, tswbService.class);
		Bundle bundle = new Bundle();
		bundle.putInt("now", 0); //立即发送
		bundle.putInt("year", mYear);
		bundle.putInt("month", mMonth);
		bundle.putInt("day", mDay);
		bundle.putInt("hour", mHour);
		bundle.putInt("minute", mMinute);
		bundle.putString("content", sWeibo);
		intent.putExtras(bundle);
		startService(intent);
    }
    
    private boolean checkUserIDPassword(int iWeiboID) {
    	Log.i(TAG, "Enter checkUserIDPassword(), iWeiboID = " + iWeiboID);
    	mDBHelper = new tswbDatabaseHelper(this);
    	SQLiteDatabase db = mDBHelper.getWritableDatabase();
    	String col[] = { tswbDatabaseHelper.FIELD_WEIBO_ID };
    	String selection = "WeiboID=?";
    	String selectionArgs[] = null;
    	if (iWeiboID == 0) { //新浪微博
    		selectionArgs = new String[] {"新浪"};
    	}
    	else if (iWeiboID == 1) { // 腾讯微博
    		selectionArgs = new String[] {"腾讯"};
    	}
    	else if (iWeiboID == 2) { // 搜狐微博
    		selectionArgs = new String[] {"搜狐"};
    	}
    	else if (iWeiboID == 3) { // 网易微博
    		selectionArgs = new String[] {"网易"};
    	}
    	
		Cursor cur = db.query(tswbDatabaseHelper.TABLE_ACCOUNT, col, selection, selectionArgs, null, null, null);
		Integer n = cur.getCount();
		Log.i(TAG, "n = " + n);
		boolean tag = false;
		if (n > 0) {
			tag = true;
		}
		cur.close();
		db.close();
		return tag;
    }
    
    private void beforeSendWeibo(int flag) {
    	Log.i(TAG, "Enter beforeSendWeibo(), flag = " + flag+ ", curStatus="+curStatus);
    	if (curStatus >= mWeiboCount) {
    		sendWeiBoLaterOrNow(TextEditWeibo.getText().toString(), flag);
    		return;
    	}
    	while (status[curStatus] == false) {
    		curStatus++;
    		if (curStatus >= mWeiboCount) break;
    	}
    	if (curStatus >= mWeiboCount) {
    		sendWeiBoLaterOrNow(TextEditWeibo.getText().toString(), flag);
    		return;
    	}
    	if (false == checkUserIDPassword(curStatus)) {
    		//showDialog(SINA_USERID_PASSWORD_DIALOG_ID);
    		Intent in = new Intent(TimingSendWeiBo.this, getUserIDPassword.class);
    		Bundle bundle = new Bundle();
    		bundle.putInt("curStatus", curStatus);
    		in.putExtras(bundle);
	    	Log.i(TAG, "Before startActivityForResult");
	    	startActivityForResult(in, REQUEST_USERID_PASSWORD);
	    	Log.i(TAG, "After startActivityForResult");
    	}
    	else {
    		curStatus++;
    		beforeSendWeibo(flag);
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.i(TAG, "Enter onActivityResult(), requestCode="+requestCode+",resultCode="+resultCode);
    	Log.v(TAG, "Intent data = " + data);
	    if (requestCode == REQUEST_USERID_PASSWORD) {
		    if (resultCode == RESULT_CANCELED) {
		    	//setTitle("Canceled...");
		    }
		    else if(resultCode == RESULT_OK) {
			    Bundle bundle = data.getExtras();
			    String userID = bundle.getString("userID");
			    String password = bundle.getString("password");
			    insertNewAccount(userID, password, curStatus, this);
			    
			    curStatus++;
			    beforeSendWeibo(mFlag);
		    }
	    }
	    else if (requestCode == REQUEST_USERID_PIC) {
	    	if(resultCode == RESULT_OK) {
		    	//Bitmap photo = data.getParcelableExtra("data");
	    		mUri = data.getData();
	    		mUriString = mUri.toString();
	    		Log.d(TAG, "mUri = " + mUri);
/*
	    		ContentResolver cr = getContentResolver();
	    		Log.d(TAG, "cr = " + cr);
	    		InputStream inStream = null;
	    		try {
					inStream = cr.openInputStream(mUri);
					Log.d(TAG, "inStream = " + inStream);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				Bitmap photo = BitmapFactory.decodeStream(inStream);

				mBaos = new ByteArrayOutputStream();
		    	photo.compress(CompressFormat.JPEG, 99, mBaos);
		    	Log.d(TAG, "photo = " + photo);
*/
		    	/*
		    	try {
					mBaos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				*/
		    	TextEditWeibo.setBackgroundColor(Color.GRAY);
		    	btnDelPic.setEnabled(true);
		    	//TextEditWeibo.
		    	/*
		    	Drawable d = null;
		    	d = Drawable.createFromStream(inStream, "temp.jpeg");
		    	Log.i(TAG, "d = " + d);
		    	TextEditWeibo.setBackgroundDrawable(d);
		    	*/
	    	}
	    	else {
	    	}
	    }
	    else if (requestCode == REQUEST_USERID_TAKE_PHOTO) {
	    	if(resultCode == RESULT_OK) {
	    		btnDelPic.setEnabled(true);
	    		mUri = photo_uri;
	    		//data.get
	    		mUriString = mUri.toString();
	    		Log.d(TAG, "mUri = " + mUri);
	    		
	    		TextEditWeibo.setBackgroundColor(Color.DKGRAY);
	    	}
	    }
	    else if (requestCode == REQUEST_USERID_VIDEO) {
	    	if(resultCode == RESULT_OK) {
	    		mUri = data.getData();
	    		mUriString = mUri.toString();
	    		Log.d(TAG, "mUri = " + mUri);
	    		
	    		TextEditWeibo.setBackgroundColor(Color.GREEN);
	    	}
	    }
	    else if (requestCode == REQUEST_USERID_TAKE_VIDEO) {
	    	if(resultCode == RESULT_OK) {
	    		
	    	}
	    }
	}
    
    public static void insertNewAccount(String sUserID, String sPassword, int status, Context context) {
    	Log.i(TAG, "Enter insertNewAccount(), sUserID = "+sUserID+", sPassword="+sPassword);
    	tswbDatabaseHelper mDBHelper = new tswbDatabaseHelper(context);
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		switch (status) {
		case 0:
			values.put(tswbDatabaseHelper.FIELD_WEIBO_ID, "新浪");
			break;
		case 1:
			values.put(tswbDatabaseHelper.FIELD_WEIBO_ID, "腾讯");
			break;
		case 2:
			values.put(tswbDatabaseHelper.FIELD_WEIBO_ID, "搜狐");
			break;
		case 3:
			values.put(tswbDatabaseHelper.FIELD_WEIBO_ID, "网易");
			break;
		}
    	values.put(tswbDatabaseHelper.FIELD_USER_ID, sUserID);
    	values.put(tswbDatabaseHelper.FIELD_PASSWORD, sPassword);
    	values.put(tswbDatabaseHelper.FIELD_SELECTED, 1);//0, 不发到这个帐号
    	
    	long lInsertReturn = db.insert(tswbDatabaseHelper.TABLE_ACCOUNT, null, values);
    	if (lInsertReturn != -1)
    	{
    		Log.i(TAG, "db.insert account sucess!!");
    	}
    	else
    	{
    		Log.e(TAG, "db.insert account error!!");
    	}
    	db.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, ACCOUNT_ID, 0, R.string.menu_account);
    	menu.add(0, TIMING_SEND_QUEUE_ID, 1, R.string.menu_timing_queue);
    	menu.add(0, RESET_ID, 2, R.string.menu_reset);
    	menu.add(0, ABOUT_ID, 3, R.string.menu_about);
    	return true; 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case ACCOUNT_ID:
	    		draw_list_view_account();
	    		break;
	    	case TIMING_SEND_QUEUE_ID:
	    		draw_list_view_timing_send_queue();
	    		break;
	    	case RESET_ID:
	    		break;
	    	case ABOUT_ID:
	    		break;
	    	case EXIT_ID: finish(); break;
    	}
    	return super.onOptionsItemSelected(item);
    }

    void draw_list_view_account() {
    	Intent in = new Intent(TimingSendWeiBo.this, ListAccount.class);
    	startActivity(in);
    }

    void draw_list_view_timing_send_queue() {
    	Intent in = new Intent(TimingSendWeiBo.this, ListSendQueue.class);
    	startActivity(in);
    }
}