package fdts.android.dbstorage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Used to save Entries and Playlists
 *
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

	// ---------------------- Playlists Table ---------------------------------
	public static final String TABLE_PLAYLISTS = "playlist";
	public static final String PLAYLIST_ID = "_id";
	public static final String PLAYLIST_NAME = "name";

	// ---------------------- PlaylistEntries Table ---------------------------
	public static final String TABLE_ENTRIES = "entries";
	public static final String ENTRY_ID = "_id";
	public static final String ENTRY_NAME = "entry";
	public static final String ENTRY_DURATION = "duration";
	public static final String ENTRY_URL = "url";
	public static final String ENTRY_POSITION = "position";
	public static final String FOREIGN_ID = "playlist_id";

	// ---------------------- Database ----------------------------------------
	private static final String DATABASE_NAME = "playlists.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String PLAYLIST_CREATE = "create table "
			+ TABLE_PLAYLISTS + "( " + PLAYLIST_ID
			+ " integer primary key autoincrement, " + PLAYLIST_NAME
			+ " text not null);";

	private static final String PLAYLISTENTRIES_CREATE = "create table "
			+ TABLE_ENTRIES + "( " + ENTRY_ID
			+ " integer primary key autoincrement, " + ENTRY_NAME
			+ " text not null, " + ENTRY_DURATION + " text, "
			+ ENTRY_URL + " text not null, " + ENTRY_POSITION + " integer, "
			+ FOREIGN_ID + " integer, foreign key (" + FOREIGN_ID
			+ ") references " + TABLE_PLAYLISTS + "(" + PLAYLIST_ID
			+ ") ON DELETE CASCADE" + ");";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(PLAYLIST_CREATE);
		database.execSQL(PLAYLISTENTRIES_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
		onCreate(db);
	}

}
