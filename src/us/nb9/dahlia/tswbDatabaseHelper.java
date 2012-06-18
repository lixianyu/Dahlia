package us.nb9.dahlia;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

public class tswbDatabaseHelper extends SQLiteOpenHelper {
	static private final String TAG = "tswbDatabaseHelper_lxy";
	private static final String DATABASE_NAME = "tswb.db";
    private static final int DATABASE_VERSION = 33;
    //预发队列表格
    static final String TABLE_TIMING_SEND = "timing_send_table";
    //static final String FIELD_ID = "_ID";
    static final String FIELD_CREATE_TIME = "create_ms";
    static final String FIELD_SEND_TIME = "send_ms";
    static final String FIELD_WEIBO_CONTENT = "contents";
    static final String FIELD_LAT = "lat";
    static final String FIELD_LON = "lon";
    static final String FIELD_CONTENTS_PIC = "contents_pic";
    static final String FIELD_CONTENTS_PIC_URI = "contents_pic_uri";
    static final String FIELD_CONTENTS_VIDEO = "contents_video";
    static final String FIELD_CONTENTS_VIDEO_URI = "contents_video_uri";
    static final String FIELD_SEND_TO_SINA = "sendToSina";
    static final String FIELD_SEND_TO_TENGXUN = "sendToTengxun";
    static final String FIELD_SEND_TO_SOHU = "sendToSohu";
    static final String FIELD_SEND_TO_WANGYI = "sendToWangyi";
    
    //帐号表格
    static final String TABLE_ACCOUNT = "account_table";
    static final String FIELD_WEIBO_ID = "WeiboID";
    static final String FIELD_USER_ID = "UserID";
    static final String FIELD_PASSWORD = "Password";
    static final String FIELD_SELECTED = "selected";
    
	private Context mContext;
	
	public tswbDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
	
	//"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Enter onCreate(), db = " + db);
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TIMING_SEND + " (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				FIELD_CREATE_TIME + " LONG," +
				FIELD_SEND_TIME + " LONG," +
				FIELD_WEIBO_CONTENT + " TEXT," +
				FIELD_LAT + " DOUBLE DEFAULT 91.0," +
				FIELD_LON + " DOUBLE DEFAULT 181.0," +
				FIELD_CONTENTS_PIC + " BLOB DEFAULT NULL," +
				FIELD_CONTENTS_PIC_URI + " TEXT DEFAULT NULL," +
				FIELD_CONTENTS_VIDEO + " BLOB DEFAULT NULL," +
				FIELD_CONTENTS_VIDEO_URI + " TEXT DEFAULT NULL," +
				FIELD_SEND_TO_SINA + " INTEGER," +
				FIELD_SEND_TO_TENGXUN + " INTEGER," +
				FIELD_SEND_TO_SOHU + " INTEGER," +
				FIELD_SEND_TO_WANGYI + " INTEGER" +          
	            ");");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACCOUNT + " (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				FIELD_WEIBO_ID + " TEXT," +
				FIELD_USER_ID + " TEXT," +
				FIELD_PASSWORD + " TEXT," +
				FIELD_SELECTED + " INTEGER " +
	            ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Enter onUpgrade(), db = " + db + "oldVersion="+oldVersion+"newVersion="+newVersion);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_TIMING_SEND);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_ACCOUNT);
        onCreate(db);
	}

}
