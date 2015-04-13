package fdts.android.dbstorage;

import java.util.ArrayList;
import java.util.List;

import fdts.android.entities.PlaylistEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Modify, Save and Create a persistent Playlist Entry
 *
 */
public class PlaylistEntryDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.ENTRY_ID,
			MySQLiteHelper.ENTRY_NAME, MySQLiteHelper.ENTRY_DURATION,
			MySQLiteHelper.ENTRY_URL, MySQLiteHelper.ENTRY_POSITION,
			MySQLiteHelper.FOREIGN_ID };

	public PlaylistEntryDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
		database.close();
	}

	/**
	 * create a new entry for a playlist in the database
	 * @param entry
	 * @param duration
	 * @param url
	 * @param position
	 * @param foreignID
	 * @return playlist entry
	 */
	public PlaylistEntry createPlaylistEntry(String entry, String duration,
			String url, long position, long foreignID) {
		ContentValues values = new ContentValues();

		values.put(MySQLiteHelper.ENTRY_NAME, entry);
		values.put(MySQLiteHelper.ENTRY_DURATION, duration);
		values.put(MySQLiteHelper.ENTRY_URL, url);
		values.put(MySQLiteHelper.ENTRY_POSITION, position);
		values.put(MySQLiteHelper.FOREIGN_ID, foreignID);
		long insertId = database.insert(MySQLiteHelper.TABLE_ENTRIES, null,
				values);
		// how to query
		Cursor cursor = database.query(MySQLiteHelper.TABLE_ENTRIES,
				allColumns, MySQLiteHelper.ENTRY_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		return cursorToPlaylistEntry(cursor);
	}

	/**
	 * delete a playlist entry from the database
	 * @param entry - which should be deleted
	 */
	public void deletePlaylistEntry(PlaylistEntry entry) {
		long id = entry.getID();
		database.delete(MySQLiteHelper.TABLE_ENTRIES, MySQLiteHelper.ENTRY_ID
				+ " = " + id, null);
	}

	/**
	 * changes the current list position of an entry in the database
	 * @param entry - which should be repositioned
	 * @param newPosition - position index
	 */
	public void changePlaylistEntryPosition(PlaylistEntry entry,
			long newPosition) {
		long id = entry.getID();
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.ENTRY_POSITION, newPosition);
		database.update(MySQLiteHelper.TABLE_ENTRIES, values,
				MySQLiteHelper.ENTRY_ID + " = " + id, null);
	}

	/**
	 * used to get all entries of a specific playlist
	 * @param playlistID - of the playlist which contains the entry
	 * @return list of all entries in a specific playlist
	 */
	public List<PlaylistEntry> getAllPlaylistEntries(long playlistID) {
		List<PlaylistEntry> entries = new ArrayList<PlaylistEntry>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_ENTRIES,
				allColumns, "playlist_id = " + playlistID, null, null, null,
				"position");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			PlaylistEntry entry = cursorToPlaylistEntry(cursor);
			entries.add(entry);
			cursor.moveToNext();
		}
		cursor.close();
		return entries;
	}

	/**
	 * move cursor to a certain entry
	 * @param cursor
	 * @return playlist entry
	 */
	private PlaylistEntry cursorToPlaylistEntry(Cursor cursor) {
		PlaylistEntry entry = new PlaylistEntry();
		entry.setID(cursor.getLong(0));
		entry.setName(cursor.getString(1));
		entry.setDuration(cursor.getString(2));
		entry.setUrl(cursor.getString(3));
		entry.setPosition(cursor.getLong(4));
		entry.setForeignID(cursor.getLong(5));
		return entry;
	}
}
