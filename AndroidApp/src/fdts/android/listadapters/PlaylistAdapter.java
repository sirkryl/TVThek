package fdts.android.listadapters;

import java.util.List;

import fdts.android.entities.Playlist;
import fdts.android.activities.PlaylistsActivity;
import fdts.android.appname.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Custom Adapter to manage the playlists
 *
 */
public class PlaylistAdapter extends ArrayAdapter<Playlist> {

	// List of all Playlists
	private List<Playlist> items;
	private Context context;
	private TextView textView;

	public PlaylistAdapter(Context context, List<Playlist> items) {
		super(context, R.layout.playlists_singleentry, items);

		this.items = items;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Playlist playlist = items.get(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.playlists_singleentry, parent,
				false);
		textView = (TextView) rowView.findViewById(R.id.textView_playlistName);
		
		// check if there are any playlists
		if (playlist != null && textView != null) {
			textView.setText(playlist.getPlaylistName());

			if (((PlaylistsActivity) context).currentPlaylist != null
					&& playlist.getID() == ((PlaylistsActivity) context).currentPlaylist
					.getID()) {
				rowView.setBackgroundColor(Color.RED);
			}
		} else {
			textView.setText("No Playlists.");
		}

		return rowView;
	}

	/**
	 * The number of items in the list
	 * @see android.widget.ListAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return items.size();
	}

	/**
	 * get item at a certain position
	 */
	@Override
	public Playlist getItem(int position) {
		return items.get(position);
	}

}