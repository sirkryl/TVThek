package fdts.android.entities;

/**
 * Represents a Playlist Entry with some attributes.
 *
 */
public class PlaylistEntry {

	private String name = "";
	private String url = "";
	private String duration;
	private long id;
	private long foreignID;
	private long position;
	private boolean actual;	
	
	public PlaylistEntry() {
		this.actual = false;
	}

	public String getName() {
		if(this.name.split("<new>").length != 0)
		{
			return this.name.split("<new>")[0];
		}
		else return this.name;
	}

	public String getNames() {
		return this.name;
	}
	
	public String getUrls() {
		return this.url;
	}
	public void setName(String name) {
		
		if (this.name == "") {
			this.name = name;
		}
		
	}

	public String getUrl() {
		if(this.url.split("<new>").length != 0)
		{
			return this.url.split("<new>")[0];
		}
		else return this.url;
	}

	public void setUrl(String url) {
		if(this.url == "") {
			this.url = url;
		}
	}

	public String getDuration() {
		return this.duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void addTitle(String title) {
		if(!this.name.isEmpty()) {
			this.name += "<new>"+title;
		}
		else this.name = title;
	}

	public void addUrl(String url) {
		if(!this.url.isEmpty()) {
			this.url += "<new>"+url;
		}
		else this.url = url;
	}

	
	public long getID() {
		return this.id;
	}

	public void setID(long id) {
		this.id = id;
	}

	public long getForeignID() {
		return this.foreignID;
	}

	public void setForeignID(long foreignID) {
		this.foreignID = foreignID;
	}

	public long getPosition() {
		return this.position;
	}

	public void setPosition(long position) {
		this.position = position;
	}
	
	public boolean isActual() {
		return actual;
	}

	public void setActual(boolean actual) {
		this.actual = actual;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}