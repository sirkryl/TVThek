package fdts.android.entities;

public class DeviceBasicInfo {
	private String ip;
	private String location;
	private String friendlyName;

	public DeviceBasicInfo() {

	}

	public String getIp() {
		return ip;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	@Override
	public String toString() {
		return friendlyName;
	}
}
