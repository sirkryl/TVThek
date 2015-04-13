package fdts.android.activities;

import fdts.android.entities.Playlist;
import fdts.android.entities.PlaylistEntry;
import fdts.android.appname.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	private static String TAG = "AndroidAppActivity";

	public static int TAB_PLAYLIST = 0;
	public static int TAB_TV = 1;
	public static int TAB_TVTHEK = 2;

	public static TabHost tabHost;
	private Playlist activePlaylist;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Resources res = getResources(); // Resource object to get Drawables
		tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// TABS
		// 1. Playlist
		// 2. TV
		// 3. TVThek

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, PlaylistsActivity.class);
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("playlists")
				.setIndicator("Playlists",
						res.getDrawable(R.drawable.ic_tab_playlists))
						.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, TVActivity.class);
		spec = tabHost.newTabSpec("tv")
				.setIndicator("TV", res.getDrawable(R.drawable.ic_tab_tv))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, TVThekActivity.class);
		spec = tabHost
				.newTabSpec("tvthek")
				.setIndicator("TV Thek",
						res.getDrawable(R.drawable.ic_tab_tvthek))
						.setContent(intent);
		tabHost.addTab(spec);

		// Default Tab = Playlist Tab
		tabHost.setCurrentTab(MainActivity.TAB_PLAYLIST);
	}

	// to stay within tabs and tell an activity that is is being invoked by
	// another activity (and is
	// most likely expected to return something)
	public void changeTabAndTell(int from, int to) {
		tabHost.setCurrentTab(to);

		switch (to) {
		case 0:
			return;
			// ((PlaylistsActivity)this.getCurrentActivity()).inform();
		case 1:
			return;
			// ((TVActivity)this.getCurrentActivity()).inform();
		case 2:
			//((TVThekActivity) this.getCurrentActivity()).setInvoked();
			return;
		}
	}

	// same as above, but with the possibility to send an object to another
	// activity
	public void changeTabAndTell(int from, int to, Object delivery) {
		tabHost.setCurrentTab(to);
		if(delivery instanceof Playlist) {
			activePlaylist = (Playlist) delivery;
		}
		switch (to) {
		case 0:
			((PlaylistsActivity) this.getCurrentActivity())
			.addNewClip((PlaylistEntry) delivery);
			return;
		case 1:
			Log.i(TAG, "Change Tab to TVActivity with playlist as package!");
			((TVActivity) this.getCurrentActivity())
					.sendPlaylistToTV((Playlist) delivery);
			return;			
		case 2:
			((TVThekActivity)this.getCurrentActivity()).setInvoked((Playlist)delivery);
			return;
		}
	}

	/**
	 * Change currently active playlist
	 * @param activePlaylist
	 */
	public void setActivePlaylist(Playlist activePlaylist) {
		this.activePlaylist = activePlaylist;
	}
	
	/**
	 * Get currently active playlist
	 * @return activePlaylist
	 */
	public Playlist getActivePlaylist()
	{
		return this.activePlaylist;
	}
	
	/**
	 * Change the actual tab.
	 * @param currentTab
	 */
	public void changeTab(int currentTab) {
		Log.i(TAG, "Tab changed to " + currentTab);
		tabHost.setCurrentTab(currentTab);
	}

	/** Called when the menu button is pressed. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/** Called when a button in the menu is pressed. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.options:
			// TODO something
			// start options activity?
			return true;
		case R.id.help:
			// TODO something
			// start help activity?
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}