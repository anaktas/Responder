package com.gr.responder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class RespondService extends IntentService {

	public static final String INCOMING_NUMBER = "";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager notifier;
	private Notification notify;	
	private int count = 0;
	
	public RespondService() {
		super("RespondService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		count++;
		Log.d("DEBUG", "call onHandleIntent() " + count);
		String smsText = "";
		smsText = getSMSText();
		String incomingNumber = arg0.getStringExtra(INCOMING_NUMBER);
		try {
			sendSMSText(incomingNumber, smsText);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Αποτυχία αποστολής μηνύματος.", Toast.LENGTH_LONG).show();
		}
	}
	
	public String getSMSText() {
		Log.d("DEBUG", "I am inside of geSMSText");
		String smsText = "";
		Cursor cursor = null;
		
		try {
			SQLiteDatabase responderDatabase;
			responderDatabase = openOrCreateDatabase("responder.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
			cursor = responderDatabase.rawQuery("SELECT message FROM tbl_sms", null);
			if(cursor.moveToFirst())
				smsText = cursor.getString(0);
			cursor.close();
			Log.d("DEBUG", "smsText = " + smsText);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			cursor.close();
		}
		
		return smsText;
	}
	
	public void sendSMSText(String incomingNumber, String smsText) throws ParseException {
		Log.d("DEBUG", "I am inside of sendSMSText");
		String callTime = "";
		String currentTime = getDateTime();
		Log.d("DEBUG", "currentTime = " + currentTime);
		Cursor cursor = null;
		SQLiteDatabase responderDatabase;
		responderDatabase = openOrCreateDatabase("responder.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
		cursor = responderDatabase.rawQuery("SELECT calltime FROM tbl_call_history WHERE number = ? ORDER BY calltime DESC LIMIT 1", new String[] {incomingNumber});
		Log.d("DEBUG", "count the results: " + cursor.getCount());
		if(cursor != null && cursor.moveToFirst()) {
			callTime = cursor.getString(0);
			Log.d("DEBUG", "callTime = " + callTime);
			boolean sendFlag = okToSend(callTime, currentTime);
			if(sendFlag) {
				ContentValues values = new ContentValues();
				values.put("number", incomingNumber);
				values.put("calltime", getDateTime());
				long id = responderDatabase.insert("tbl_call_history", null, values);
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(incomingNumber, null, "Auto response: " + smsText, null, null);
				showNotification(incomingNumber);
			}
		} else {
			ContentValues values = new ContentValues();
			values.put("number", incomingNumber);
			values.put("calltime", getDateTime());
			long id = responderDatabase.insert("tbl_call_history", null, values);
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(incomingNumber, null, "Auto response: " + smsText, null, null);
			showNotification(incomingNumber);
		}
		cursor.close();
	}
	
	public String getDateTime() {
		Log.d("DEBUG", "I am inside of getDateTime");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		Date date = new Date();
        
		return dateFormat.format(date).toString();
	}
	
	public void showNotification(String incomingNumber) {
		notifier = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notify = new Notification(R.drawable.ic_launcher, "Response!", System.currentTimeMillis());
		Context context = getApplicationContext();
		String notificationTitle = "Responder:";
		String notificationText = "Απάντησα στον " + incomingNumber;
		Intent myIntent = new Intent(Intent.ACTION_VIEW, null);
		PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, myIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		notify.defaults |= Notification.DEFAULT_SOUND;
		notify.flags |= Notification.FLAG_AUTO_CANCEL;
		notify.setLatestEventInfo(context, notificationTitle, notificationText, pendingIntent);
		notifier.notify(NOTIFICATION_ID, notify);
	}
	
	public boolean okToSend(String callTime, String currentTime) {
		Log.d("DEBUG", "I am inside of okToSend");
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date then = null;
		Date now = null;
		boolean sendFlag = false;
	
		try {
			then = format.parse(callTime);
			now = format.parse(currentTime);
			long diff = now.getTime() - then.getTime();
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (24 * 60 * 60 * 1000);
			if(diffDays == 0 && diffHours == 0) {
				if(diffMinutes >= 5) {
					sendFlag = true;
				} else
					sendFlag = false;
			} else
				sendFlag = true;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.d("DEBUG", "sendFlag = " + sendFlag);
		return sendFlag;
	}

}
