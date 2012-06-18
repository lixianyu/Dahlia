package us.nb9.dahlia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ListSendQueue extends ListActivity implements OnItemClickListener {
	static private final String TAG = "ListSendQueue_lxy";
	
	private static final int DIALOG_LIST = 0;
	SQLiteDatabase db;
	ListView lv;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.list);
        lv = getListView();

        tswbDatabaseHelper mDBHelper = new tswbDatabaseHelper(this);
    	db = mDBHelper.getWritableDatabase();
    	String col[] = { "_id",
    			tswbDatabaseHelper.FIELD_SEND_TIME,
    			tswbDatabaseHelper.FIELD_WEIBO_CONTENT,
    			tswbDatabaseHelper.FIELD_CONTENTS_PIC_URI,
    			tswbDatabaseHelper.FIELD_SEND_TO_SINA};

    	Cursor cur = db.query(tswbDatabaseHelper.TABLE_TIMING_SEND, col, null, null, null, null, null);
		Integer n = cur.getCount();
		Log.i(TAG, "There are " + n + " rows......");
    
        startManagingCursor(cur);
        
        //lv = (ListView)findViewById(R.id.list);
        //lv.setOnItemClickListener(this);
        // Map Cursor columns to views defined in simple_list_item_2.xml
        ListAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, cur, 
                        new String[] { tswbDatabaseHelper.FIELD_SEND_TIME,
        							   tswbDatabaseHelper.FIELD_WEIBO_CONTENT }, 
                        new int[] { android.R.id.text1, android.R.id.text2 });
        lv.setOnItemClickListener(this);
        setListAdapter(adapter);
    }

	@Override
    protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LIST:
            return new AlertDialog.Builder(ListSendQueue.this)
                .setTitle(R.string.menu_timing_queue)
                .setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        /* User clicked so do some stuff */
                        String[] items = getResources().getStringArray(R.array.select_dialog_items);
                        new AlertDialog.Builder(ListSendQueue.this)
                                .setMessage("You selected: " + which + " , " + items[which])
                                .show();
                    }
                })
                .create();
		}

		return null;
	}
	
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
	}
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Enter onDestroy()");
		db.close();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.v(TAG, "Enter onItemClick()");
		Log.v(TAG, "arg0 = "+arg0);
		Log.v(TAG, "arg1 = "+arg1);
		Log.v(TAG, "arg2 = "+arg2);
		Log.v(TAG, "arg3 = "+arg3);
		showDialog(DIALOG_LIST);
	}
}
