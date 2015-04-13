package fdts.android.activities;

import java.util.ArrayList;

import fdts.android.appname.R;
import fdts.android.entities.Playlist;
import fdts.android.entities.PlaylistEntry;
import fdts.android.listadapters.PlaylistEntryAdapter;
import fdts.android.tvconnection.ConnectService;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for the TV Tab.
 * 
 */
public class TVActivity extends ListActivity implements OnClickListener,
		OnItemClickListener {
	private String TAG = "TVActivity";

	/** Message Codes */
	public static final int SERVICE_CONNECT = 1;

	public static final int SERVICE_CONNECT_FAIL = 0;
	public static final int SERVICE_CONNECT_SUCCESS = 1;
	public static final int TV_RECEIVE_LIST = 2;
	public static final int TV_NO_LIST = 3;
	public static final int TV_NEW = 4;
	public static final int TV_CONNECT_SUCCESS = 5;
	public static final int TV_CONNECT_FAIL = 6;

	public static final int SEND_MESSAGE_SUCCESS = 7;
	public static final int SEND_MESSAGE_FAIL = 8;

	public static final int PLAYLIST_RECEIVE = 9;
	public static final int PLAYLIST_NOT_AVAILABLE = 10;

	/** Content View Codes */
	private int actualView = 1;
	private static final int LAYOUT_EXIT = 0;
	private static final int LAYOUT_TVS = 1;
	private static final int LAYOUT_TV_CONTENT = 2;

	private ArrayAdapter<String> tvs;
	// private ArrayAdapter<PlaylistEntry> content;
	private PlaylistEntryAdapter content;
	private Playlist actualPlaylist;
	private ListView lv;
	private Button btnSearch;

	// Control Buttons
	private Button btnPlay;
	private Button btnPause;
	private Button btnPrevious;
	private Button btnNext;

	private boolean serviceConnected = false;
	private boolean tvConnected = false;

	private boolean playlistIsWaiting = false;
	private Playlist playlistToSend;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "create tv tab");

		// create adapter for list of available tvs
		tvs = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		// set view to list with all available tvs
		setTVsView();

		// register the activity to the service
		if (!serviceConnected) {
			connectToService();
		}
	}

	/**
	 * Register the activity to the service. So its possible to send message
	 * from the activity to the service and the other way around.
	 */
	private void connectToService() {
		Log.i(TAG, "Connect to service...");
		// register the activity to the service
		// send Intent to Service, so that the service can send Intents back
		Intent i = new Intent(ConnectService.REGISTER_ACTIVITY);
		PendingIntent pi = createPendingResult(TVActivity.SERVICE_CONNECT, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		i.putExtra("fdts.android.appname.Activity", pi);
		startService(i);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "start tv tab");
	}

	/**
	 * Sets the view for the search TV screen.
	 */
	private void setTVsView() {
		setContentView(R.layout.tv_view);

		btnSearch = (Button) findViewById(R.id.button_searchForTVs);
		btnSearch.setOnClickListener(this);

		setListAdapter(tvs);

		lv = getListView();
		lv.setOnItemClickListener(this);

		actualView = TVActivity.LAYOUT_TVS;
	}

	/**
	 * Sets the view for the TV screen with content.
	 */
	private void initTVContentView(Playlist playlist) {
		setContentView(R.layout.tv_connected);

		actualPlaylist = playlist;

		// control buttons
		btnPlay = (Button) findViewById(R.id.btn_play);
		btnPlay.setOnClickListener(this);

		btnPause = (Button) findViewById(R.id.btn_pause);
		btnPause.setOnClickListener(this);

		btnPrevious = (Button) findViewById(R.id.btn_previous);
		btnPrevious.setOnClickListener(this);

		btnNext = (Button) findViewById(R.id.btn_next);
		btnNext.setOnClickListener(this);

		lv = getListView();
		lv.setOnItemClickListener(this);

		// content = new ArrayAdapter<PlaylistEntry>(this,
		// android.R.layout.simple_list_item_1);
		if (playlist != null) {
			content = new PlaylistEntryAdapter(this,
					playlist.getPlaylistItems(), null);
		} else {
			content = new PlaylistEntryAdapter(this,
					new ArrayList<PlaylistEntry>(), null);
			// if no playlist as parameter --> ask tv
			// send a request to get the actual playlist of the tv, if available
			startService(new Intent(ConnectService.GET_TVPLAYLIST));
		}
		setListAdapter(content);

		actualView = TVActivity.LAYOUT_TV_CONTENT;
	}

	/**
	 * Set the entries of the playlist to the TV Playlist view.
	 * @param list
	 */
	private void setTVPlaylistEntries(Playlist list) {
		// content = new ArrayAdapter<PlaylistEntry>(this,
		// android.R.layout.simple_list_item_1);
		// setListAdapter(content);
		actualPlaylist = list;

		for (PlaylistEntry e : list.getPlaylistItems()) {
			content.add(e);
		}
	}

	/**
	 * Handle the Intents, that are received from the Service.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TVActivity.SERVICE_CONNECT) {
			Log.i(TAG, "A result from the connect service.");

			// not necessary message to know that the activity is registered at
			// the service
			if (resultCode == TVActivity.SERVICE_CONNECT_SUCCESS) {
				Log.d(TAG, "Connecting to service succeed!");

				serviceConnected = true;
			}
			// if connecting to service failed
			else if (resultCode == TVActivity.SERVICE_CONNECT_FAIL) {
				Log.d(TAG, "Connecting to service failed!");

				serviceConnected = false;
			}
			// receiving a list of all available tv devices
			else if (resultCode == TVActivity.TV_RECEIVE_LIST) {
				Log.d(TAG, "Received TV List!");

				ArrayList<String> t = data
						.getStringArrayListExtra("fdts.android.appname.TVList");
				for (int i = 0; i < t.size(); i++) {
					Log.d(TAG, "device" + i + ": " + t.get(i));
					tvs.add(t.get(i));
				}
			}
			// no tv list available
			else if (resultCode == TVActivity.TV_NO_LIST) {
				Log.d(TAG, "No TV list available!");

				Toast.makeText(this, R.string.no_tvs, Toast.LENGTH_SHORT)
						.show();
				tvs.clear();
			}
			// a new tv is found, add to the list
			else if (resultCode == TVActivity.TV_NEW) {
				Log.d(TAG, "New TV found!");

				String newTV = data
						.getStringExtra("fdts.android.appname.NEWTV");
				tvs.add(newTV);
			}
			// if connecting to tv was successful
			else if (resultCode == TVActivity.TV_CONNECT_SUCCESS) {
				Log.d(TAG, "Connecting to TV succeed!");

				tvConnected = true;

				if (playlistIsWaiting) {
					sendMessageToTV(playlistToSend.returnPlaylistString());
					initTVContentView(playlistToSend);

					playlistIsWaiting = false;
					playlistToSend = null;
				} else {
					initTVContentView(null);
				}
			}
			// if connecting to tv failed
			else if (resultCode == TVActivity.TV_CONNECT_FAIL) {
				Log.d(TAG, "Connecting to TV failed!");

				Toast.makeText(this, R.string.connect_fail, Toast.LENGTH_SHORT)
						.show();
				tvConnected = false;
			}
			// if sending message to tv succeed
			else if (resultCode == TVActivity.SEND_MESSAGE_SUCCESS) {
				Log.d(TAG, "Send message succeed!");

				// Toast.makeText(this, R.string.send_message_success,
				// Toast.LENGTH_SHORT).show();
			}
			// if sending message to tv failed
			else if (resultCode == TVActivity.SEND_MESSAGE_FAIL) {
				Log.d(TAG, "Send message failed!");

				Toast.makeText(this, R.string.send_message_fail,
						Toast.LENGTH_SHORT).show();
			}
			// if receiving a playlist from the tv
			else if (resultCode == TVActivity.PLAYLIST_RECEIVE) {
				Log.d(TAG, "Receiving playlist from tv!");

				String playlist = data
						.getStringExtra("fdts.android.appname.GET_TVPLAYLIST");
				Playlist pl = new Playlist(playlist);
				setTVPlaylistEntries(pl);

				Toast.makeText(this, R.string.loaded_playlist,
						Toast.LENGTH_SHORT).show();
			}
			// if no playlist on the tv is available
			else if (resultCode == TVActivity.PLAYLIST_NOT_AVAILABLE) {
				Log.d(TAG, "No playlist on the tv available!");

				// TODO ? nur als info
			}
			// if result code is false
			else {
				Log.d(TAG, "Unknown resultCode");
			}
		}
	}

	/**
	 * Send a string of the playlist to the connected TV.
	 */
	public void sendPlaylistToTV(Playlist playlist) {
		Log.i(TAG, "Send playlist to tv...");
		Log.i(TAG, playlist.returnPlaylistString());

		if (!serviceConnected) {
			connectToService();
		}
		if (!tvConnected) {
			Log.i(TAG, "Wait till a tv has been choosen...");
			Toast.makeText(this, R.string.choose_tv, Toast.LENGTH_LONG).show();
			playlistIsWaiting = true;
			playlistToSend = playlist;

			return;
		}
		// send playlist as message to tv
		sendMessageToTV(playlist.returnPlaylistString());
		initTVContentView(playlist);
	}

	/**
	 * Sends a message to the connected TV.
	 * @param message
	 */
	private void sendMessageToTV(String message) {
		if (serviceConnected && tvConnected) {
			Intent i = new Intent(ConnectService.SEND_MESSAGE);
			i.putExtra("fdts.android.appname.SEND_MESSAGE", message);
			startService(i);
		}
	}

	/**
	 * Clicking on button.
	 * @param click
	 */
	public void onClick(View click) {
		if (click.equals(btnSearch)) {
			Log.i(TAG, "clicked button");
			// ganze tv liste anfordern
			tvs.clear();
			// TODO Ladeanzeige?!
			startService(new Intent(ConnectService.GET_TVLIST));
		}
		else if (click.equals(btnPlay)) {
			Log.i(TAG, "clicked play");

			startService(new Intent(ConnectService.ACTION_PLAY));
		} else if (click.equals(btnPause)) {
			Log.i(TAG, "clicked pause");

			startService(new Intent(ConnectService.ACTION_PAUSE));
		} else if (click.equals(btnPrevious)) {
			Log.i(TAG, "clicked previous");

			startService(new Intent(ConnectService.ACTION_PREVIOUS));
			
			if(actualPlaylist.getActualNumber() == 0) {
				setActualPlaylistEntry(actualPlaylist.getPlaylistItems().size()-1);
			}
			else {
				setActualPlaylistEntry(actualPlaylist.getActualNumber()-1);
			}
			
		} else if (click.equals(btnNext)) {
			Log.i(TAG, "clicked next");

			startService(new Intent(ConnectService.ACTION_NEXT));
			
			if(actualPlaylist.getActualNumber() == actualPlaylist.getPlaylistItems().size()-1) {
				setActualPlaylistEntry(0);
			}
			else {
				setActualPlaylistEntry(actualPlaylist.getActualNumber()+1);
			}
		}
	}

	/**
	 * Clicking on item in list.
	 * @param arg0
	 * @param view
	 * @param position
	 * @param id
	 */
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
		if (actualView == TVActivity.LAYOUT_TVS) {
			// get name of clicked tv
			String tv = (String) ((TextView) view).getText();
			Log.i(TAG, "Clicked on item " + tv);

			// start service with data from chosen tv device
			Intent i = new Intent(ConnectService.CHOOSE_TV);
			i.putExtra("fdts.android.appname.CHOOSE_TV", tv);
			startService(i);
		} else if (actualView == TVActivity.LAYOUT_TV_CONTENT) {
			Log.i(TAG, "Item at position " + position + ".");		
			
			setActualPlaylistEntry(position);
			
			// send actual clip info to TV			
			Intent i = new Intent(ConnectService.ACTION_URL);
			i.putExtra("fdts.android.appname.ACTION_URL", position);
			startService(i);
		}
	}
	
	/**
	 * Sets the actual Playlist Entry and highlight it.
	 * Old one gets default values.
	 * @param position
	 */
	private void setActualPlaylistEntry(int position){
		if(actualPlaylist.getActualNumber() != -1) {
			// set old to default
			
//			content.getItem(actualPlaylist.getActualNumber()).setActual(false);
		}
		actualPlaylist.changeActualEntry(position);
		// set new to actual and change color
//		actualPlaylist.setActualNumber(position);
//		content.getItem(position).setActual(true);
		getListView().invalidateViews();
	}

	@Override
	public void onBackPressed() {
		actualView = actualView - 1;
		if (actualView == LAYOUT_EXIT) {
			super.onBackPressed();
		} else if (actualView == LAYOUT_TVS) {
			setTVsView();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "stop tvActivity");
		// TODO disconnect Connection
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "destroy tvActivity");
		// TODO disconnect Connection
	}
}
