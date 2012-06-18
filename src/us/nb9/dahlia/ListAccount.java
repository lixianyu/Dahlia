package us.nb9.dahlia;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ListAccount extends ListActivity implements OnItemClickListener {
	static private final String TAG = "ListAccount_lxy";
	
	public static final int MENU_ADD_ID = Menu.FIRST;
	
	static final int REQUEST_USERID_PASSWORD = 0;
	
	SQLiteDatabase db;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tswbDatabaseHelper mDBHelper = new tswbDatabaseHelper(this);
    	db = mDBHelper.getWritableDatabase();
    	String col[] = { "_id",
    			tswbDatabaseHelper.FIELD_WEIBO_ID,
    			tswbDatabaseHelper.FIELD_USER_ID,
    			tswbDatabaseHelper.FIELD_PASSWORD,
    			tswbDatabaseHelper.FIELD_SELECTED};
    	
    	Cursor cur = db.query(tswbDatabaseHelper.TABLE_ACCOUNT, col, null, null, null, null, null);
		Integer n = cur.getCount();
		Log.i(TAG, "There are " + n + " rows......");
    
        startManagingCursor(cur);
        
        // Map Cursor columns to views defined in simple_list_item_2.xml
        ListAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, cur, 
                        new String[] { tswbDatabaseHelper.FIELD_WEIBO_ID,
        							   tswbDatabaseHelper.FIELD_USER_ID }, 
                        new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(adapter);
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
		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, MENU_ADD_ID, 0, R.string.menu_add_account);
    	//menu.add(0, TIMING_SEND_QUEUE_ID, 1, R.string.menu_timing_queue);
    	//menu.add(0, RESET_ID, 2, R.string.menu_reset);
    	//menu.add(0, ABOUT_ID, 3, R.string.menu_about);
    	return true; 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case MENU_ADD_ID:
	    		Intent in = new Intent(ListAccount.this, getUserIDPassword.class);
	    		Bundle bundle = new Bundle();
	    		bundle.putInt("curStatus", 0);//0, –¬¿À’ ∫≈
	    		in.putExtras(bundle);
		    	Log.i(TAG, "Before startActivityForResult");
		    	startActivityForResult(in, REQUEST_USERID_PASSWORD);
		    	Log.i(TAG, "After startActivityForResult");
	    		break;
	    		/*
	    	case TIMING_SEND_QUEUE_ID:
	    		draw_list_view_timing_send_queue();
	    		break;
	    	case RESET_ID:
	    		break;
	    	case ABOUT_ID:
	    		break;
	    	case EXIT_ID: finish(); break;*/
    	}
    	return super.onOptionsItemSelected(item);
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
			    TimingSendWeiBo.insertNewAccount(userID, password, 0, this);
		    }
	    }
    }
}
