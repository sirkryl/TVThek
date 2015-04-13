package fdts.android.tvconnection;

import java.util.ArrayList;

import fdts.android.activities.TVActivity;

import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Service which handle the connection between Android Device and Smart TV and
 * send responses back to the related Activity.
 * 
 */
public class ConnectService extends Service {
	private String TAG = "ConnectAndroidTVService";

	/** action commands */
	public static final String REGISTER_ACTIVITY = "fdts.android.appname.action.REGISTER_ACTIVITY";
	public static final String GET_TVLIST = "fdts.android.appname.action.GET_TVLIST";
	public static final String CHOOSE_TV = "fdts.android.appname.action.CHOOSE_TV";
	public static final String SEND_MESSAGE = "fdts.android.appname.action.SEND_MESSAGE";

	public static final String GET_TVPLAYLIST = "fdts.android.appname.action.GET_TVPLAYLIST";

	public static final String ACTION_PLAY = "fdts.android.appname.action.ACTION_PLAY";
	public static final String ACTION_PAUSE = "fdts.android.appname.action.ACTION_PAUSE";
	
	public static final String ACTION_PREVIOUS = "fdts.android.appname.action.ACTION_PREVIOUS";
	public static final String ACTION_NEXT = "fdts.android.appname.action.ACTION_NEXT";
	public static final String ACTION_STOP = "fdts.android.appname.action.ACTION_VOLUME";
	public static final String ACTION_URL = "fdts.android.appname.action.ACTION_URL";

	public static final String DISCONNECT = "fdts.android.appname.action.DISCONNECT";

	/** Used to return messages to registered activity. */
	private PendingIntent activity;
	private boolean isRegistered = false;
	private boolean isTV = false;

	/** the SmartTVConnection */
	private TVConnection con;

	/** Device List */
	private ArrayList<String> devices;

	/**
	 * The system calls this method when the service is first created, to
	 * perform one-time setup procedures (before it calls either
	 * onStartCommand() or onBind()).
	 * 
	 * Starts control point and searching for available tv devices.
	 */
	@Override
	public void onCreate() {
		Log.d(TAG, "create connect service");

		con = new SmartTVConnection(this);
		devices = new ArrayList<String>();
		// start control point
		con.start();
	}

	/**
	 * Binding to the service is not possible.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// Log.d(TAG, "onBind");
		return null;
	}

	/**
	 * Starts the service with the corresponding action.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");

		String action = intent.getAction();
		// register the starting activity to this service
		if (action.equals(ConnectService.REGISTER_ACTIVITY)) {
			Log.d(TAG, "REGISTER ACTIVITY");

			activity = intent
					.getParcelableExtra("fdts.android.appname.Activity");

			// send result message back, not necessary
			sendResult(TVActivity.SERVICE_CONNECT);
			isRegistered = true;
		}
		// send tv list to the registered activity
		else if (action.equals(ConnectService.GET_TVLIST)) {
			Log.d(TAG, "GET TVLIST");

			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				if (devices.size() != 0) {
					Intent i = new Intent();
					i.putStringArrayListExtra("fdts.android.appname.TVList",
							devices);

					con.search();

					sendResultWithData(TVActivity.TV_RECEIVE_LIST, i);
				}
				// no devices available
				else {
					con.search();

					sendResult(TVActivity.TV_NO_LIST);
				}
			}
		}
		// activity chose a tv from the available list
		else if (action.equals(ConnectService.CHOOSE_TV)) {
			Log.d(TAG, "CHOOSE_TV");

			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				String tv = intent
						.getStringExtra("fdts.android.appname.CHOOSE_TV");
				Log.d(TAG, "CHOSEN TV DEVICE: " + tv);

				isTV = con.connectToTV(tv);
				if (isTV) {
					Log.i(TAG, "Connecting succeed!");

					sendResult(TVActivity.TV_CONNECT_SUCCESS);
				} else {
					Log.i(TAG, "Connecting failed!");

					sendResult(TVActivity.TV_CONNECT_FAIL);
				}
			}
		}
		// send a message to tv
		else if (action.equals(ConnectService.SEND_MESSAGE)) {
			Log.d(TAG, "SEND_MESSAGE");

			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				String message = intent
						.getStringExtra("fdts.android.appname.SEND_MESSAGE");
				boolean ok = con.sendMessageToTV(message);

				if (ok) {
					Log.i(TAG, "Sending succeed!");

					sendResult(TVActivity.SEND_MESSAGE_SUCCESS);
				} else {
					Log.i(TAG, "Sending failed!");

					sendResult(TVActivity.SEND_MESSAGE_FAIL);
				}
			}
		} else if (action.equals(ConnectService.GET_TVPLAYLIST)) {
			Log.d(TAG, "GET_TVPLAYLIST");

			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				boolean ok = con.sendMessageToTV("RLI");

				if (ok) {
					Log.i(TAG, "Send playlist request to tv succeed!");

					// now send get message sent from the tv
					String playlist = con.getMessageFromTV();
					
					// is a playlist available?		
					if (playlist != null) {
						Log.i(TAG, "Get playlist message from tv succeed!");
						Log.d(TAG, playlist);
						
						String[] tmp = playlist.split(" ");
						if(tmp[1].equals("NULL")) {
							Log.i(TAG, "No playlist from the tv available!");

							sendResult(TVActivity.PLAYLIST_NOT_AVAILABLE);
						} else {
							Intent i = new Intent();
							i.putExtra("fdts.android.appname.GET_TVPLAYLIST",
									playlist);
							sendResultWithData(TVActivity.PLAYLIST_RECEIVE, i);
						}						
					} else {
						Log.i(TAG, "No playlist from the tv available!");

						sendResult(TVActivity.PLAYLIST_NOT_AVAILABLE);
					}
				} else {
					Log.i(TAG, "Get playlist from tv failed!");

					// TODO eigenes result als fehlermeldung?!
					sendResult(TVActivity.PLAYLIST_NOT_AVAILABLE);
				}
			}
		}
		// if play button pressed
		else if (action.equals(ConnectService.ACTION_PLAY)) {
			Log.d(TAG, "ACTION_PLAY");

			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				boolean ok = con.sendMessageToTV("KEY PLAY");

				if (ok) {
					Log.i(TAG, "Sending succeed!");

					sendResult(TVActivity.SEND_MESSAGE_SUCCESS);
				} else {
					Log.i(TAG, "Sending failed!");

					sendResult(TVActivity.SEND_MESSAGE_FAIL);
				}
			}
		}
		// if pause button pressed
		else if (action.equals(ConnectService.ACTION_PAUSE)) {
			Log.d(TAG, "ACTION_PAUSE");

			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				boolean ok = con.sendMessageToTV("KEY PAUSE");

				if (ok) {
					Log.i(TAG, "Sending succeed!");

					sendResult(TVActivity.SEND_MESSAGE_SUCCESS);
				} else {
					Log.i(TAG, "Sending failed!");

					sendResult(TVActivity.SEND_MESSAGE_FAIL);
				}
			}
		}
		// if previous button pressed
		else if (action.equals(ConnectService.ACTION_PREVIOUS)) {
			Log.d(TAG, "ACTION_PREVIOUS");
			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				boolean ok = con.sendMessageToTV("KEY PREV");

				if (ok) {
					Log.i(TAG, "Sending succeed!");

					sendResult(TVActivity.SEND_MESSAGE_SUCCESS);
				} else {
					Log.i(TAG, "Sending failed!");

					sendResult(TVActivity.SEND_MESSAGE_FAIL);
				}
			}
		}
		// if next button pressed
		else if (action.equals(ConnectService.ACTION_NEXT)) {
			Log.d(TAG, "ACTION_NEXT");

			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				boolean ok = con.sendMessageToTV("KEY NEXT");

				if (ok) {
					Log.i(TAG, "Sending succeed!");

					sendResult(TVActivity.SEND_MESSAGE_SUCCESS);
				} else {
					Log.i(TAG, "Sending failed!");

					sendResult(TVActivity.SEND_MESSAGE_FAIL);
				}
			}
		}
		// if specific entry is choosen
		else if (action.equals(ConnectService.ACTION_URL)) {
			Log.d(TAG, "ACTION_URL");

			// if no activity is registered to this service
			if (!isRegistered) {
				Log.e(TAG, "No activity is registered to this service.");

				// TODO send a response back?
			} else {
				int position = intent.getIntExtra("fdts.android.appname.ACTION_URL", -1);
				if(position != -1) {
					boolean ok = con.sendMessageToTV("KEY URL " + position);
					
					if (ok) {
						Log.i(TAG, "Sending succeed!");

						sendResult(TVActivity.SEND_MESSAGE_SUCCESS);
					} else {
						Log.i(TAG, "Sending failed!");

						sendResult(TVActivity.SEND_MESSAGE_FAIL);
					}
				}				
			}
		}
		// disconnect service and connection between AndroidApp and tv
		else if (action.equals(ConnectService.DISCONNECT)) {
			Log.d(TAG, "DISCONNECT");
		}

		if (isTV) {
			con.disconnectFromTV();
		}

		return START_NOT_STICKY; // Means we started the service, but don't want
		// it to restart in case it's killed.
	}

	/**
	 * Sends an Intent with a resultCode back to the activity. 
	 * @param resultCode - result code for the activity
	 */
	private void sendResult(int resultCode) {
		Log.i(TAG, "send result to activity...");
		try {
			activity.send(resultCode);
		} catch (CanceledException e) {
			Log.e(TAG, "Can't send result to activity!");
			e.printStackTrace();
		}
	}

	/**
	 * Send an Intent with a resultCode and an Intent with data back to the
	 * activity.
	 * @param resultCode - result code for the activity
	 * @param i - Intent with extra data
	 */
	private void sendResultWithData(int resultCode, Intent i) {
		Log.i(TAG, "send result with data to activity...");
		try {
			activity.send(getApplicationContext(), resultCode, i);
		} catch (CanceledException e) {
			Log.e(TAG, "Can't send result with data to activity!");
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	}

	// ##############################################
	// Stuff with TV Connection

	/**
	 * Gets called from TVConnection, when a new TV is found.
	 * @param tvname
	 */
	public void newTV(String tvname) {
		if (!devices.contains(tvname)) {
			devices.add(tvname);

			// tell registered activity from new tv
			Intent i = new Intent();
			i.putExtra("fdts.android.appname.NEWTV", tvname);

			sendResultWithData(TVActivity.TV_NEW, i);
		}
	}
}
