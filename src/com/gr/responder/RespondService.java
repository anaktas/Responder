package com.gr.responder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
		String sms = "";
		String incomingNumber = arg0.getStringExtra(INCOMING_NUMBER);
		SQLiteDatabase responderDatabase;
		responderDatabase = openOrCreateDatabase("responder.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
		Cursor cursor = responderDatabase.rawQuery("SELECT message FROM tbl_sms", null);
		if(cursor.moveToFirst())
			sms = cursor.getString(0);
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(incomingNumber, null, "Auto response: " + sms, null, null);
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
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Αποτυχία αποστολής μηνύματος.", Toast.LENGTH_LONG).show();
			cursor.close();
		}
	}

}
