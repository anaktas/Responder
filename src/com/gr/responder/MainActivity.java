package com.gr.responder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private SQLiteDatabase responderDatabase;
	private static final String CREATE_SMS_TABLE = "CREATE TABLE IF NOT EXISTS tbl_sms ( " 
			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "message TEXT )";
	private static final String CREATE_CALL_HISTORY = "CREATE TABLE IF NOT EXISTS tbl_call_history ("
			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "number TEXT,"
			+ "calltime DATETIME )";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initialize database	
		initialDatabase();
	}
	
	private void updateDatabase(String message) {
		try {
			ContentValues values = new ContentValues();
			values.put("message", message);
			responderDatabase.update("tbl_sms", values, null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initialDatabase() {
		try {
			responderDatabase = openOrCreateDatabase("responder.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
			responderDatabase.execSQL(CREATE_SMS_TABLE);
			ContentValues values = new ContentValues();
			values.put("message", "DummyValue");
			long dummyID = responderDatabase.insert("tbl_sms", null, values);
			responderDatabase.execSQL(CREATE_CALL_HISTORY);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateButton(View view) {
		final EditText sms = (EditText) findViewById(R.id.someText);
		updateDatabase(sms.getText().toString());
		Toast.makeText(MainActivity.this, "Το μήνυμά σας καταχωρήθηκε.", Toast.LENGTH_LONG).show();
	}
	
	public void enableIncomingCallReceiver(View view) {
		ComponentName receiver = new ComponentName(this, IncomingCallReceiver.class);
		PackageManager pm = this.getPackageManager();
		pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		Toast.makeText(this, "H εφαρμογή ενεργοποιήθηκε.", Toast.LENGTH_LONG).show();
	}
	
	public void disableIncomingCallReceiver(View view) {
		ComponentName receiver = new ComponentName(this, IncomingCallReceiver.class);
		PackageManager pm = this.getPackageManager();
		pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		Toast.makeText(this, "H εφαρμογή απενεργοποιήθηκε.", Toast.LENGTH_LONG).show();
	}
}
