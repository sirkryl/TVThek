package fdts.android.dbstorage;

import java.util.ArrayList;
import java.util.List;

import fdts.android.entities.Playlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Modify, Save and Create a persistent Playlist 
 *
 */
public class PlaylistsDataSource {

	// Database fields
	String TAG = "PlaylistsDataSource";
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.PLAYLIST_ID,
			MySQLiteHelper.PLAYLIST_NAME };

	public PlaylistsDataSource(Context context) {
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
	 * creates a playlist in the database
	 * @param playlist - which should be saved
	 * @return playlist
	 */
	public Playlist createPlaylist(String playlist) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.PLAYLIST_NAME, playlist);
		long insertId = database.insert(MySQLiteHelper.TABLE_PLAYLISTS, null,
				values);
		// how to query
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLISTS,
				allColumns, MySQLiteHelper.PLAYLIST_ID + " = " + insertId,
				null, null, null, null);
		cursor.moveToFirst();
		return cursorToPlaylist(cursor);
	}

	/**
	 * deletes a playlist from the database
	 * @param playlist - which should be deleted
	 */
	public void deletePlaylist(Playlist playlist) {
		long id = playlist.getID();
		database.delete(MySQLiteHelper.TABLE_PLAYLISTS,
				MySQLiteHelper.PLAYLIST_ID + " = " + id, null);
		database.delete(MySQLiteHelper.TABLE_ENTRIES, MySQLiteHelper.FOREIGN_ID
				+ " = " + id, null);
	}

	/**
	 * renames a playlist in the database
	 * @param playlist - which should be renamed
	 * @param newName - the new playlist name
	 */
	public void renamePlaylist(Playlist playlist, String newName) {
		long id = playlist.getID();
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.PLAYLIST_NAME, newName);
		database.update(MySQLiteHelper.TABLE_PLAYLISTS, values,
				MySQLiteHelper.PLAYLIST_ID + " = " + id, null);
	}

	/**
	 * used to get all playlists available in the database
	 * @return a List of all playlists in the database
	 */
	public List<Playlist> getAllPlaylists() {
		List<Playlist> playlists = new ArrayList<Playlist>();
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLAYLISTS,
				allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Playlist playlist = cursorToPlaylist(cursor);
			playlists.add(playlist);
			cursor.moveToNext();
		}
		cursor.close();
		return playlists;
	}

	/**
	 * move cursor to a playlist
	 * @param cursor
	 * @return playlist
	 */
	private Playlist cursorToPlaylist(Cursor cursor) {
		Playlist playlist = new Playlist();
		playlist.setID(cursor.getLong(0));
		playlist.setPlaylistName(cursor.getString(1));
		return playlist;
	}
}