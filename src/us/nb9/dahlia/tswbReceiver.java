package us.nb9.dahlia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;
import android.os.Bundle;
import android.os.SystemClock;

public class tswbReceiver extends BroadcastReceiver {
	static private final String TAG = "tswbReceiver_lxy";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w(TAG, "Enter onReceive(), intent="+intent);
		Toast.makeText(context, "ʱ�䵽", Toast.LENGTH_SHORT).show();
		//SystemClock.sleep(3000);
		//tswbService.getService().onDestroy();
		//Bundle bundleTemp = intent.getExtras();
		//long senttimes = bundleTemp.getLong("sendTime");

		Intent inStartService = new Intent(context, tswbService.class);
		Bundle bundle = new Bundle();
    	bundle.putInt("now", 2);
    	
    	//Log.w(TAG, "senttimes = " + senttimes);
    	//bundle.putLong("sendTime", senttimes);
    	
		//tswbService.getService().stopService(stopService);
		//context.bindService(stopService, arg1, null);
    	inStartService.putExtras(bundle);
    	context.startService(inStartService);
    	Log.w(TAG, "Leave onReceive(), inStartService="+inStartService);
	}
}
