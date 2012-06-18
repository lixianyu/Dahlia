package us.nb9.dahlia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class getUserIDPassword extends Activity {
	static private final String TAG = "getUserIDPassword_lxy";
	static final int USERID_PASSWORD_DIALOG_ID = 0;
	
	int curStatus;
	
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		Log.i(TAG, "Enter onKeyDown(), keyCode="+keyCode+", keyevent="+keyevent);
		super.onKeyDown(keyCode, keyevent);
		return false;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent keyevent) {
		Log.i(TAG, "Enter onKeyUp(), keyCode="+keyCode+", keyevent="+keyevent);
		super.onKeyUp(keyCode, keyevent);
		return false;
	}
	
	public void onStart() {
		super.onStart();
		Log.i(TAG, "Enter onStart()");
		showDialog(USERID_PASSWORD_DIALOG_ID);
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
	}
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Enter onDestroy()");
	}

	@Override
	public void onCreate(Bundle icicle) {
		Log.i(TAG, "Enter onCreate(), icicle = "+icicle);
		super.onCreate(icicle);
		setContentView(R.layout.empty);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		curStatus = bundle.getInt("curStatus");
		Log.i(TAG, "curStatus = " + curStatus);
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
        case USERID_PASSWORD_DIALOG_ID:
        	// This example shows how to add a custom layout to an AlertDialog
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
            int iStringID = getStringID(curStatus);
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(iStringID)
                .setView(textEntryView)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {

					@Override
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						Log.i(TAG, "keyCode="+keyCode+", event="+event);
						if (keyCode == 4) {
							finish();
							return true;
						}
						return false;
					}
                	
                })
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.i(TAG, "User clicked OK so do some stuff, whichButton = "+whichButton);
                        //LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        //LinearLayout layout = (LinearLayout)inflate.inflate(R.layout.alert_dialog_text_entry, null);
                        EditText username_edit = (EditText)textEntryView.findViewById(R.id.username_edit);
                        EditText password_view = (EditText)textEntryView.findViewById(R.id.password_edit);
                        String username = username_edit.getText().toString();
                        String password = password_view.getText().toString();
                        
                        Bundle bundle = new Bundle();
                		bundle.putString("userID", username);
                		bundle.putString("password", password);
                		Intent mIntent = new Intent();
                		mIntent.putExtras(bundle);
                		setResult(RESULT_OK, mIntent);
                		finish();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.i(TAG, "User clicked cancel so do some stuff. whichButton = "+whichButton);
                        Intent mIntent = new Intent();
                		setResult(RESULT_CANCELED, mIntent);
                		finish();
                    }
                })
                .create();
        }
        return null;
	}
}
