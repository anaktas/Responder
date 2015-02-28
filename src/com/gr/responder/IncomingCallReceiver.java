package com.gr.responder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.*;
import android.util.Log;

public class IncomingCallReceiver extends BroadcastReceiver {
	
	private static boolean isRinging = false;
	private static String incomingNumber;
	private int count2 = 0;
	
	@Override
	public void onReceive(final Context arg0, Intent arg1) {
		// TODO Auto-generated method stud
		count2++;
		Log.d("DEBUG", "call onReceive() " + count2);
		String state = arg1.getStringExtra(TelephonyManager.EXTRA_STATE);
		if(state == null)
			return;
		if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			isRinging = true;
			Bundle bundle = arg1.getExtras();
			incomingNumber = bundle.getString("incoming_number");
			Log.d("DEBUG", "state is ringing");
		}
		if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
			isRinging = false;
			Log.d("DEBUG", "state is of hook");
		}
		if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			if(isRinging) {
				Intent intent = new Intent(arg0, RespondService.class);
				intent.putExtra(RespondService.INCOMING_NUMBER, incomingNumber);
				arg0.startService(intent);
				Log.d("DEBUG", "state is idle");
			}
		}
		Log.d("DEBUG", "isRinging = " + isRinging + ", state = " + state);
	}
	
}
