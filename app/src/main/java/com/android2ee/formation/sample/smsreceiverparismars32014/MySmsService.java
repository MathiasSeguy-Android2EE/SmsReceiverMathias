package com.android2ee.formation.sample.smsreceiverparismars32014;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import androidx.core.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class MySmsService extends Service {
	public MySmsService() {
		Log.e("MySmsService", "Constructor called");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("MySmsService", "A new SMS has been received by the service");
		
		pdIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		if (intent!=null&&intent.getAction()!=null&& intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			extractSmsInformation(intent);
		}else if(intent==null){
			Toast.makeText(this, "SMS Listener Paris redeliver null intent sigh :"+intent, Toast.LENGTH_LONG).show();
			Log.e("MySmsService","SMS Listener Paris redeliver null intent sigh :"+intent);
		}else {
			//I excpect a intent redelivered by the system
			Toast.makeText(this, "SMS Listener Paris Strange beahvior received intent with action "+intent.getAction(), Toast.LENGTH_LONG).show();
			Toast.makeText(this, "SMS Listener Paris redeliver with flag"+intent.getFlags(), Toast.LENGTH_LONG).show();

			Log.e("MySmsService","SMS Listener Paris Strange beahvior received intent with action "+intent.getAction());
			Log.e("MySmsService","SMS Listener Paris redeliver with flag"+intent.getFlags());
		}
		//You should call stopSelf();
		//but if you don't what happens ?o? you receive null intent that wake you up!!!
		
		//When the system kills your service, it tries to launch it again if your used the following flag:
		//return Service.START_REDELIVER_INTENT the last Intent will be sent again to wake up the service
		//return Service.START_NOT_STICKY the system won't try to launch it again
		//return Service.START_STICKY the systme wake up the service with a null intent
		
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Extract
	 * 
	 * @param intent
	 */
	private void extractSmsInformation(Intent intent) {
		// Retrieve the bundle that handles the Messages
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			// Retrieve the data store in the SMS
			Object[] pdus = (Object[]) bundle.get("pdus");
			// Declare the associated SMS Messages
			SmsMessage[] smsMessages = new SmsMessage[pdus.length];
			// Rebuild your SMS Messages
			for (int i = 0; i < pdus.length; i++) {
				smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}
			// Parse your SMS Message
			SmsMessage currentMessage;
			String body="", from="Nobody";
			long time=0;
			for (int i = 0; i < smsMessages.length; i++) {
				currentMessage = smsMessages[i];
				body = body + currentMessage.getDisplayMessageBody();
				from = currentMessage.getDisplayOriginatingAddress();
				time = currentMessage.getTimestampMillis();
				Log.e("MySmsService", "A new SMS [from: " + from + "] [body: " + body + "]");
			}

			displayNotif(body, from, time);
		}
	}

	/**
	 * The pendingIntent sent by the system when the notification is clicked
	 */
	private PendingIntent pdIntent;
	/**
	 * The id of the notification
	 */
	int UniqueNotificationId = 4112008;

	/**
	 * Display the notification using the parameters below
	 * 
	 * @param body
	 * @param from
	 * @param time
	 */
	private void displayNotif(String body, String from, long time) {
		String dude = getName(from);
		//adding a sound
		Uri soundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);			
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);		
		builder.setAutoCancel(true)
				.setContentIntent(pdIntent)
				.setContentText(body)
				.setContentTitle(dude)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_smiley))
				.setLights(0x99FF0000, 0, 1000)// don't work
				.setNumber(41108)
				.setSound(soundUri)//set the sound
				.addAction(R.drawable.ic_notif_action, "Love", pdIntent)//add an action
				.addAction(R.drawable.ic_notif_smiley, "You", pdIntent)//add an action
				.setOngoing(false)
				.setPriority(Integer.MIN_VALUE)
				.setProgress(100, 0, true) // don't work
				.setSmallIcon(R.drawable.ic_small_smiley)
				.setSubText("tel : " + from)
				.setTicker("You received a new SMS from " + from)
				.setVibrate(new long[] { 100, 200, 100, 200, 100 }) // don't work
				.setWhen(System.currentTimeMillis())
				.setStyle(new NotificationCompat.BigTextStyle()
					.bigText(body)
					.setSummaryText("*tel : " + from)
					.setBigContentTitle(":)"+dude)
				);

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(UniqueNotificationId, builder.build());
	}

	public String getName(String from) {
		String contact = "unknow";
		// Android 2.0 and later
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(from));
		// Query the filter URI
		String[] projection = new String[] { PhoneLookup.DISPLAY_NAME };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		int nameFieldColumnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
		while (cursor.moveToNext()) {
			contact = cursor.getString(nameFieldColumnIndex);
		}
		cursor.close();
		return contact;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
