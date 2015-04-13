package fdts.android.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Playlist which has a name and contains playlist items.
 *
 */
public class Playlist {

	private String playlistName;
	private List<PlaylistEntry> playlistItems = new ArrayList<PlaylistEntry>();
	private int actualNumber;
	private boolean active = false;
	
	public Playlist() {
		this.actualNumber = -1;
	}
	
	/**
	 * Parses the given String/Format and set the playlist items.
	 * @param playlistEntries
	 */
	public Playlist (String playlistEntries) {
		System.out.println("parse String from Playlist");
		String[] tmp = playlistEntries.split(" ");
		
		if(tmp[0].equals("PLI")) {			
			// parse playlistentries
			PlaylistEntry entry = new PlaylistEntry();
			
			if(tmp[1].equals("NULL")) {
				// no playlist available
				return;
			}
			
			int posAct = playlistEntries.indexOf(" ACT ");
			this.playlistName = playlistEntries.substring(("PLI ").length(), posAct);
			
			int posClip = playlistEntries.indexOf(" CLIP ");
			actualNumber = Integer.valueOf(playlistEntries.substring(posAct + (" ACT ").length(), posClip));
			
			
			System.out.println(this.playlistName);
			System.out.println(this.actualNumber);
			
			String[] clips = playlistEntries.split(" CLIP ");
			System.out.println(clips.length);
			for(int i = 1; i < clips.length; i++) {
				entry = new PlaylistEntry();
				
				int posEntry = clips[i].indexOf(" ENTRY ");
				entry.setName(clips[i].substring(0, posEntry));
				System.out.println(clips[i].substring(0, posEntry));
				
				String[] entries = clips[i].split(" ENTRY ");
				System.out.println(entries.length);
				for(int j = 1; j < entries.length; j++) {
					int posUrl = entries[j].indexOf(" URL ");
					entry.addTitle(entries[j].substring((" URL ").length(), posUrl));
					System.out.println(entries[j].substring((" URL ").length(), posUrl));
					
					entry.addUrl(entries[j].substring(posUrl + (" URL ").length()));
					System.out.println(entries[j].substring(posUrl + (" URL ").length()));					
				}
				
				this.addPlaylistItem(entry);
			}			
			this.changeActualEntry(actualNumber);
		}
	}
	
	private long id;

	public long getID() {
		return this.id;
	}

	public void setID(long id) {
		this.id = id;
	}

	public String getPlaylistName() {
		return this.playlistName;
	}

	public void setActive(Boolean active)
	{
		this.active = active;
	}
	
	public boolean active()
	{
		return this.active;
	}
	public void setPlaylistName(String playlistName) {
		this.playlistName = playlistName;
	}

	public List<PlaylistEntry> getPlaylistItems() {
		return this.playlistItems;
	}

	public void addPlaylistItem(PlaylistEntry entry) {
		this.playlistItems.add(entry);
	}

	public void setPlaylistItems(List<PlaylistEntry> playlistItems) {
		this.playlistItems = playlistItems;
	}
	
	/**
	 * @return the actualNumber
	 */
	public int getActualNumber() {
		return actualNumber;
	}

	/**
	 * @param actualNumber the actualNumber to set
	 */
	public void setActualNumber(int actualNumber) {
		this.actualNumber = actualNumber;
	}
	
	public void changeActualEntry(int new_position) {	
		if(actualNumber != -1) {
			this.playlistItems.get(actualNumber).setActual(false);
		}	
		
		this.actualNumber = new_position;
		this.playlistItems.get(new_position).setActual(true);
	}
		

	/**
	 * Prepares the playlist in a format to send it to the tv.
	 * FORMAT:  PLI playlistname CLIP  NAME entryname1 DURATION entryduration1 ENTRY  TITLE subentry1name URL subentry1url ENTRY  ....
	 * @return
	 */
	public String returnPlaylistString() {
		
		String playlist = "PLI " + this.playlistName;
		for(PlaylistEntry e : playlistItems) {
			String[] splitTitle = e.getNames().split("<new>");
			String[] splitUrl = e.getUrls().split("<new>");
			playlist += " CLIP ";
			playlist += splitTitle[0];
			//playlist += " NAME " + splitTitle[0];
			playlist += " DURATION " + e.getDuration();
			for(int j = 1; j < splitTitle.length; j++) {
				playlist += " ENTRY ";
				playlist += " TITLE " + splitTitle[j];
				playlist += " URL " + splitUrl[j];
			}
		}
		
		System.out.println("STRING: "+playlist);

		return playlist;
	}

}
