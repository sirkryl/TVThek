package fdts.android.listadapters;

import java.util.List;

import fdts.android.appname.R;
import fdts.android.dbstorage.PlaylistEntryDataSource;
import fdts.android.dragndrop.DropListener;
import fdts.android.dragndrop.RemoveListener;
import fdts.android.entities.PlaylistEntry;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Custom Adapter to manage the playlistentries
 * 
 */
public class PlaylistEntryAdapter extends ArrayAdapter<PlaylistEntry> implements
		RemoveListener, DropListener {

	// List of all Playlistentries
	private List<PlaylistEntry> items;
	
	private Context context;
	private TextView textViewName;
	private TextView textViewUrl;
	private TextView textViewDuration;
	private ImageView thumbnailImageView;
	private PlaylistEntryDataSource dataSource;
	private LinearLayout layout;

	public PlaylistEntryAdapter(Context context, List<PlaylistEntry> items,
			PlaylistEntryDataSource ds) {
		super(context, R.layout.clips_singleentry, items);
		this.dataSource = ds;
		this.items = items;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PlaylistEntry entry = items.get(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.clips_singleentry, parent,
				false);

		thumbnailImageView = (ImageView) rowView.findViewById(R.id.thumbnailImageView);
		textViewName = (TextView) rowView.findViewById(R.id.textView_clipName);
		textViewDuration = (TextView) rowView
				.findViewById(R.id.textView_clipDuration);
		textViewUrl = (TextView) rowView.findViewById(R.id.textView_clipUrl);
		layout = (LinearLayout) rowView.findViewById(R.id.entry_layout);

		// check if there even is an entry to work with
		if (entry != null && textViewUrl != null) {
			textViewName.setText(entry.getName());
			textViewDuration.setText(entry.getDuration());
			textViewUrl.setText(entry.getUrl());
			thumbnailImageView.setImageResource(R.drawable.tvthek_logo);
			
			if(entry.isActual()) {
				layout.setBackgroundColor(Color.RED);
			}
		} else {
			textViewName.setText("No Clips.");
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
	 * get an item at a certain position
	 */
	@Override
	public PlaylistEntry getItem(int position) {
		return items.get(position);
	}

	// invoked after an item has been dragged and dropped by a user, saves item
	// at a new position and change others if necessary
	public void onDrop(int from, int to) {
		PlaylistEntry temp_entry = items.get(from);

		dataSource.open();

		// consider two possible cases: drag and drop an entry to a position
		// above or below
		if (from < to) {

			for (int i = from; i < to; i++) {
				items.get(i + 1).setPosition(i);
				dataSource.changePlaylistEntryPosition(items.get(i + 1), i);
			}
			items.remove(from);
			items.add(to, temp_entry);
			items.get(to).setPosition(to);
			dataSource.changePlaylistEntryPosition(temp_entry, to);
		} else if (from > to) {

			for (int i = to; i < from; i++) {
				items.get(i).setPosition(i + 1);
				dataSource.changePlaylistEntryPosition(items.get(i), i + 1);
			}
			items.remove(from);
			items.add(to, temp_entry);
			items.get(to).setPosition(to);
			dataSource.changePlaylistEntryPosition(temp_entry, to);
		}
		// Log.d("WTF","temp-foreignid "+temp_entry.getForeignID());
		items = dataSource.getAllPlaylistEntries(temp_entry.getForeignID());
		dataSource.close();

	}

	// invoked after an item has been removed
	public void onRemove(int which) {
		if (which < 0 || which > items.size())
			return;
		items.remove(which);

	}
}