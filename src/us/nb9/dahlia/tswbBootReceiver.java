package us.nb9.dahlia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class tswbBootReceiver extends BroadcastReceiver {
	static private final String TAG = "tswbBootReceiver_lxy";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Enter onReceive(), context = " + context + ", intent = " + intent);

		tswbDatabaseHelper mDBHelper = new tswbDatabaseHelper(context);
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String col[] = {tswbDatabaseHelper.FIELD_SEND_TIME };
		Cursor cur = db.query(tswbDatabaseHelper.TABLE_TIMING_SEND, col, null, null, null, null, 
				tswbDatabaseHelper.FIELD_SEND_TIME);
		Integer n = cur.getCount();
		Log.i(TAG, "n = " + n);
		long senttime = 0;
		if (n >= 1) {
			if (cur.moveToNext()) {
				senttime = cur.getLong(0);
			}
		}
		cur.close();
		db.close();
		Log.i(TAG, "senttime = " + senttime);

		if (senttime > 0) {
			Intent inStartService = new Intent(context, tswbService.class);
			Bundle bundle = new Bundle();
	    	bundle.putInt("now", 3);
	    	bundle.putLong("sendTime", senttime);
	    	inStartService.putExtras(bundle);
	    	ComponentName cn = context.startService(inStartService);
	    	Log.i(TAG, "ComponentName = " + cn);
		}

		Log.i(TAG, "Leave onReceive()");
	}
}
