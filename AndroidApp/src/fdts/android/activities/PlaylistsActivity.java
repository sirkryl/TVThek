package fdts.android.activities;

import java.util.List;

import fdts.android.listadapters.PlaylistAdapter;
import fdts.android.listadapters.PlaylistEntryAdapter;
import fdts.android.appname.R;
import fdts.android.dbstorage.PlaylistEntryDataSource;
import fdts.android.dbstorage.PlaylistsDataSource;
import fdts.android.dragndrop.DragListener;
import fdts.android.dragndrop.DragNDropListView;
import fdts.android.dragndrop.DropListener;
import fdts.android.dragndrop.RemoveListener;
import fdts.android.entities.Playlist;
import fdts.android.entities.PlaylistEntry;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;


/**
 * Activity for the Playlist Tab
 * 
 */
public class PlaylistsActivity extends ListActivity {

	String TAG = "PlaylistsActivity";
	private List<Playlist> listItems;
	private PlaylistAdapter adapter;
	private PlaylistEntryAdapter entryAdapter;
	private ListView listView;
	private PlaylistsDataSource dataSource;
	private PlaylistEntryDataSource entryDataSource;
	public Playlist currentPlaylist;
	private Button sendToTVButton;
	private ColorStateList tmp_colorList;

	/** Content View Codes */
	private int currentView = 1;
	private static final int LAYOUT_EXIT = 0;
	private static final int LAYOUT_PLAYLISTS = 1;
	private static final int LAYOUT_PLAYLIST_CONTENT = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setPlaylistView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dataSource != null) {
			dataSource.close();
		}
		if (entryDataSource != null) {
			entryDataSource.close();
		}
	}

	/**
	 * Sets the current view to show all currently available playlists
	 */
	private void setPlaylistView() {
		setContentView(R.layout.playlists_view);

		dataSource = new PlaylistsDataSource(this);
		dataSource.open();

		listItems = dataSource.getAllPlaylists();
		dataSource.close();

		listView = getListView();

		this.adapter = new PlaylistAdapter(this, listItems);
		setListAdapter(this.adapter);

		// refreshes the Adapter
		this.adapter.notifyDataSetChanged();

		// registers listview for a context menu
		registerForContextMenu(listView);
		
		if (currentPlaylist != null)
			Log.d(TAG, "id: " + currentPlaylist.getID());
		
		currentView = LAYOUT_PLAYLISTS;
	}

	/**
	 * Sets the current view to show the clips contained within a chosen
	 * playlist
	 */
	private void setPlaylistContentView(Playlist item) {
		setContentView(R.layout.clips_view);

		((MainActivity) getParent()).setActivePlaylist(item);
		currentPlaylist = item;
		entryDataSource = new PlaylistEntryDataSource(this);
		entryDataSource.open();

		long foreignID = item.getID();

		// loads all clips corresponding to the chosen playlist
		this.entryAdapter = new PlaylistEntryAdapter(this,
				entryDataSource.getAllPlaylistEntries(foreignID),
				entryDataSource);

		currentPlaylist.setPlaylistItems(entryDataSource
				.getAllPlaylistEntries(foreignID));
		((MainActivity) getParent()).setActivePlaylist(currentPlaylist);
		setListAdapter(this.entryAdapter);
		
		if(sendToTVButton == null) {
			sendToTVButton = (Button) findViewById(R.id.button_sendToTV);
		    tmp_colorList = sendToTVButton.getTextColors();
		}
		if (currentPlaylist.getPlaylistItems().size() == 0) {
			sendToTVButton.setEnabled(false);
			sendToTVButton.getBackground().setAlpha(128);
			sendToTVButton.setTextColor(sendToTVButton.getTextColors().withAlpha(50));
		} else {
			sendToTVButton.setEnabled(true);
			sendToTVButton.getBackground().setAlpha(255);
			sendToTVButton.setTextColor(tmp_colorList);
		}
		
		listView = getListView();

		// register DragNDropListeners
		if (listView instanceof DragNDropListView) {
			((DragNDropListView) listView).setDropListener(mDropListener);
			((DragNDropListView) listView).setRemoveListener(mRemoveListener);
			((DragNDropListView) listView).setDragListener(mDragListener);
		}
		Log.d(TAG, "id2: " + currentPlaylist.getID());
		
		// register ListView for a context menu
		registerForContextMenu(listView);

		entryDataSource.close();

		// changes current view
		currentView = LAYOUT_PLAYLIST_CONTENT;
	}

	/**
	 * if the "Playlist hinzufuegen"-Button is clicked
	 * @param view
	 */
	public void onButtonClick_button_createPlaylist(View view) {
		this.createPlaylist("Bitte geben Sie einen Namen ein:");
	}

	/**
	 * if the "Beitrag hinzufuegen"-Button is clicked
	 * @param view
	 */
	public void onButtonClick_button_addClip(View view) {

		// change tab to tvthek and get the new clip
		((MainActivity) getParent()).changeTabAndTell(0, 2, currentPlaylist);
	}

	public void onButtonClick_button_sendToTV(View view) {
		Playlist playlist = currentPlaylist;
		entryDataSource = new PlaylistEntryDataSource(this);
		entryDataSource.open();
		playlist.setPlaylistItems(entryDataSource
				.getAllPlaylistEntries(playlist.getID()));
		((MainActivity) getParent()).changeTabAndTell(0, 1, playlist);
		entryDataSource.close();
	}

	// add a new clip to the currently selected playlist
	// is invoked indirectly by TVThekActivity
	public void addNewClip(PlaylistEntry entry) {
		if (currentView != LAYOUT_PLAYLIST_CONTENT)
			setPlaylistContentView(currentPlaylist);
		String name = entry.getNames();
		String url = entry.getUrls();
		String duration = entry.getDuration();
		entryDataSource = new PlaylistEntryDataSource(this);
		entryDataSource.open();

		// Log.d(TAG, "currentPlaylistforeignID: "+currentPlaylist.getID());
		// create new playlistentry
		long foreignID = currentPlaylist.getID();

		long position = currentPlaylist.getPlaylistItems().size() - 1;
		entry = entryDataSource.createPlaylistEntry(name, duration, url,
				position, foreignID);
		// entry.setForeignID(foreignID);
		// Log.d(TAG, "foreignId: "+foreignID);
		// add new entry to currently selected Playlist
		currentPlaylist.getPlaylistItems().add(entry);
		if (!sendToTVButton.isEnabled()) {
			sendToTVButton.setEnabled(true);
			sendToTVButton.getBackground().setAlpha(255);
			sendToTVButton.setTextColor(tmp_colorList);
		}
		
		currentPlaylist.returnPlaylistString();
		
		// reload listitems to show new entry
		this.entryAdapter = new PlaylistEntryAdapter(this,
				entryDataSource.getAllPlaylistEntries(foreignID),
				entryDataSource);
		setListAdapter(this.entryAdapter);
		
		entryDataSource.close();
	}

	/**
	 * Used to create a new playlist
	 * @param message - Message of the dialog
	 */
	private void createPlaylist(String message) {
		final EditText input = new EditText(this);

		new AlertDialog.Builder(this)
		.setTitle("Playlist erstellen")
		.setMessage(message)
		.setView(input)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				Playlist current;
				if (!name.trim().isEmpty()) {
					current = new Playlist();
					for (Playlist item : listItems) {
						if (item.getPlaylistName().equals(name)) {
							createPlaylist("Diese Playliste existiert bereits:");
							return;
						}
					}
					current.setPlaylistName(name);
					adapter.notifyDataSetChanged();
					addItems(current);
				} else {
					createPlaylist("Der Name ist leer:");
				}
			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				// Do nothing.
			}
		}).show();
	}

	/**
	 * Called by clicking on a Playlist
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		if (currentView == LAYOUT_PLAYLISTS) {
			// get currently selected playlist
			Playlist item = listItems.get(position);
			// Log.d(TAG, "listItems - id: "+item.getID());
			Log.d(TAG, "" + listView.getChildCount());
			// TextView entryView = (TextView)(listView.getChildAt(position+1));
			// entryView.setBackgroundColor(Color.BLUE);
			this.setPlaylistContentView(item);

			listItems.get((int) id);
		}
	}

	/**
	 * Adds Playlists to the List
	 * @param item - new Playlist
	 */
	public void addItems(Playlist item) {
		dataSource.open();
		item = dataSource.createPlaylist(item.getPlaylistName());
		dataSource.close();
		listItems.add(item);
		this.adapter.notifyDataSetChanged();
	}

	/**
	 * Creates the Context Menu
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		// iff the current listview is showing a playlist's contents
		if (listView instanceof DragNDropListView) {
			inflater.inflate(R.menu.context_menu_clips, menu);
			return;
		}
		inflater.inflate(R.menu.context_menu, menu);
	}

	/**
	 * Controls the actions for the Context Menu
	 * @return true - if one of the items in the Context Menu is clicked 
	 * 		   false - otherwise
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo context = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.tv:
			// send playlist to TVActivity
			final Playlist playlist = listItems.get((int) context.id);
			entryDataSource = new PlaylistEntryDataSource(this);
			entryDataSource.open();
			playlist.setPlaylistItems(entryDataSource
					.getAllPlaylistEntries(playlist.getID()));
			((MainActivity) getParent()).changeTabAndTell(0, 1, playlist);
			entryDataSource.close();
			return true;
		case R.id.delete:
			// delete entry
			if (listView instanceof DragNDropListView) {
				entryDataSource.open();
				Log.d("TEST", "" + (int) context.id);
				entryDataSource.deletePlaylistEntry(currentPlaylist
						.getPlaylistItems().get((((int) context.id))));
				currentPlaylist.getPlaylistItems().remove((int) context.id);
				if(currentPlaylist.getPlaylistItems().size() == 0) {
					sendToTVButton.setEnabled(false);
					sendToTVButton.getBackground().setAlpha(128);
					sendToTVButton.setTextColor(sendToTVButton.getTextColors().withAlpha(50));
				}
				// reload listitems to show new entry
				this.entryAdapter = new PlaylistEntryAdapter(this,
						entryDataSource.getAllPlaylistEntries(currentPlaylist
								.getID()), entryDataSource);
				setListAdapter(this.entryAdapter);
				entryDataSource.close();
				// entryAdapter.notifyDataSetChanged();
				return true;
			}
			// delete playlist
			dataSource.open();
			dataSource.deletePlaylist(listItems.get((int) context.id));
			dataSource.close();
			if (listItems.get((int) context.id).getID() == currentPlaylist
					.getID()) {
				currentPlaylist = null;
			}
			listItems.remove((int) context.id);

			adapter.notifyDataSetChanged();
			((MainActivity) getParent()).setActivePlaylist(null);

			return true;
		case R.id.edit:
			// edit playlist
			final Playlist current = listItems.get((int) context.id);
			this.renamePlaylist(current);
			return true;
		default:
			return false;
		}
	}

	/**
	 * Dialog shows up if one wants to name two playlists equal
	 * @param current - Playlist which should be renamed
	 */
	private void playlistAlreadyExists(final Playlist current, String title,
			String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				renamePlaylist(current);
			}
		}).show();
	}

	/**
	 * RenameDialog
	 * @param current - Playlist which should be renamed
	 */
	private void renamePlaylist(final Playlist current) {
		final EditText input = new EditText(this);

		new AlertDialog.Builder(this)
		.setTitle("Umbenennen der Playlist")
		.setView(input)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				if (!name.trim().isEmpty()) {
					for (Playlist item : listItems) {
						if (item.getPlaylistName().equals(name)) {
							playlistAlreadyExists(
									current,
									"Diese Playliste existiert bereits:",
									"Bitte wählen Sie einen anderen Namen.");
							return;
						}
					}
					current.setPlaylistName(name);
					adapter.notifyDataSetChanged();
					dataSource.open();
					dataSource.renamePlaylist(current, name);
					dataSource.close();
					((MainActivity) getParent())
					.setActivePlaylist(current);
				} else {
					playlistAlreadyExists(current,
							"Der Name ist leer:",
							"Bitte geben Sie einen Namen ein.");
				}
			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				// Do nothing.
			}
		}).show();
	}

	// invoked if user presses "BACK"-Button
	@Override
	public void onBackPressed() {
		currentView = currentView - 1;
		if (currentView == LAYOUT_EXIT) {
			super.onBackPressed();
		} else if (currentView == LAYOUT_PLAYLISTS) {
			setPlaylistView();
		}
	}

	/**
	 * used for drag'n'drop
	 */
	private DropListener mDropListener = new DropListener() {
		public void onDrop(int from, int to) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof PlaylistEntryAdapter) {
				((PlaylistEntryAdapter) adapter).onDrop(from, to);
				getListView().invalidateViews();
			}
		}
	};

	/**
	 * used for drag'n'drop
	 */
	private RemoveListener mRemoveListener = new RemoveListener() {
		public void onRemove(int which) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof PlaylistEntryAdapter) {
				((PlaylistEntryAdapter) adapter).onRemove(which);
				getListView().invalidateViews();
			}
		}
	};

	/**
	 * used for drag'n'drop
	 */
	private DragListener mDragListener = new DragListener() {

		// TODO: Change color and change behavior of element beneath currently
		// dragged item to better show the current position
		int backgroundColor = Color.BLUE;
		int defaultBackgroundColor;

		// while an item is being dragged
		public void onDrag(int x, int y, ListView listView) {
			// TODO Auto-generated method stub
		}

		// user starts to drag an item
		public void onStartDrag(View itemView) {
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
			ImageView iv = (ImageView) itemView
					.findViewById(R.id.thumbnailImageView);
			if (iv != null)
				iv.setVisibility(View.INVISIBLE);
		}

		// user stops to drag an item
		public void onStopDrag(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
			ImageView iv = (ImageView) itemView
					.findViewById(R.id.thumbnailImageView);
			if (iv != null)
				iv.setVisibility(View.VISIBLE);
		}

	};
}