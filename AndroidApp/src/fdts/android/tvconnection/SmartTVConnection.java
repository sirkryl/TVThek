package fdts.android.tvconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.ParserException;
import org.cybergarage.xml.parser.JaxpParser;

import fdts.android.entities.DeviceBasicInfo;

import android.util.Log;

/**
 * Set up a connection to a SmartTV device.
 * After the connection is established http requests are handled.
 * 
 * Whether a emulator is used the port has to be changed!
 *
 */
public class SmartTVConnection implements TVConnection {
	String TAG = "SmartTVConnection";

	/** Connected service */
	private ConnectService service;

	/** SmartTV App name */
	private static final String smartTVAppname = "Player";
	/** URI for the http requests */
	private static final String uri = "/ws/app/" + smartTVAppname + "/";

	/** actual chosen tv */
	private HttpHost host;
	/** port, emulator 8008, else 80 */
	private int port = 80;
	/** device id */
	private String deviceid = "12345";
	/** actual tv string */
	private String tvname;

	/** device list */
	private HashMap<String, DeviceBasicInfo> devSearchHashMap = new HashMap<String, DeviceBasicInfo>();

	/** control point */
	private ControlPoint controlPoint;
	/** service type filter */
	private final String SERVICE_TYPE_FILTER = "urn:samsung.com:service:MultiScreenService:1";

	/**
	 * Creates a SmartTVConnection.
	 * @param s
	 */
	public SmartTVConnection(ConnectService s) {
		this.service = s;
	}

	/**
	 * Starts to establish a connection.
	 */
	public void start() {
		upnpStart();
	}

	/**
	 * Creates a control point and starts it. A Response Listener handle new tv
	 * devices.
	 * 
	 * This method is adopted from the Samsung SmartTV Tutorial: Convergence Application
	 * http://www.samsungdforum.com/Guide/View/Developer_Documentation/Samsung_SmartTV_Developer_Documentation_3.5/JavaScript/Convergence_App/Tutorial_Creating_a_Convergence_Application
	 */
	private void upnpStart() {
		Log.i(TAG, "started upnp...");

		controlPoint = new ControlPoint();
		controlPoint.addSearchResponseListener(new SearchResponseListener() {
			public void deviceSearchResponseReceived(SSDPPacket ssdpPacket) {
				Log.d(TAG, "deviceSearchResponseReceived...");

				String location = ssdpPacket.getLocation(); // Location
				Log.d(TAG, "remote address = " + ssdpPacket.getRemoteAddress());
				String ip = ssdpPacket.getRemoteAddress(); // IP Address

				Log.d(TAG, "location = " + location);
				Log.d(TAG, "ip = " + ip);

				host = new HttpHost(ip, port);

				// Get xml content by location
				HttpGet httpget = new HttpGet(location);
				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse resp = null;

				try {
					resp = client.execute(httpget); // Http GET for Location.

				} catch (IOException e) {
					e.printStackTrace();
				}

				// Try to find friendly name in XML
				if (resp != null && resp.getStatusLine().getStatusCode() == 200) {
					JaxpParser parser = null;
					try {
						parser = new JaxpParser();
						InputStream is = resp.getEntity().getContent();
						// Log.i(TAG, parser.parse(is).toString());
						Node deviceNode = parser.parse(is).getNode("device");
						if (deviceNode != null) {
							String friendly = deviceNode
									.getNodeValue("friendlyName"); // Friendly
							// Name
							Log.d(TAG, "friendly : " + friendly);

							DeviceBasicInfo dev = new DeviceBasicInfo();
							dev.setIp(ip);
							dev.setLocation(location);
							dev.setFriendlyName(friendly);
							devSearchHashMap.put(ip, dev);

							// tell service that a new tv is available
							service.newTV(friendly);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ParserException e) {
						e.printStackTrace();
					}

				}

			}
		});

		controlPoint.start(SERVICE_TYPE_FILTER); // Start Control Point using
		// Search Target
	}

	public void search() {
		controlPoint.search(SERVICE_TYPE_FILTER);
	}

	
	// #####################################################
	// ANDROID APP -> TV

	/**
	 * Set the default headers for a http request to a tv device. 
	 * @param request - HttpRequest for setting the default headers
	 */
	private void setDefaultHeaders(HttpRequest request) {
		request.setHeader("sldeviceid", deviceid);
		request.setHeader("VendorID", "VenderMe");
		request.setHeader("DeviceName", "AndroidApp");
		request.setHeader("ProductID", "SMARTDev");
	}

	/**
	 * Sends a http post request to connect with the tv.
	 */
	public boolean connectToTV(String tvname) {
		Log.i(TAG, "identify tv and set host...");
		this.tvname = tvname;

		// search for tvname in hashmap and set host
		for (DeviceBasicInfo i : devSearchHashMap.values()) {
			if (i.getFriendlyName().equals(tvname)) {
				Log.d(TAG, "tv found...set host");

				host = new HttpHost(i.getIp(), port);
				Log.d(TAG, "ip: " + i.getIp() + ", port: " + port);
			}
		}        

		Log.i(TAG, "connect to tv...");
		HttpPost httppost = new HttpPost(uri + "connect");

		// set headers
		setDefaultHeaders(httppost);

		HttpClient client = new DefaultHttpClient();
		HttpResponse resp = null;

		// execute request
		try {
			resp = client.execute(host, httppost);

		} catch (IOException e) {
			Log.e(TAG, "Can't execute http request.");
			e.printStackTrace();
		}

		// handle response
		if (resp != null && resp.getStatusLine().getStatusCode() == 200) {
			Log.d(TAG, "Connection succeed.");
			return true;
		} else {
			if (resp != null) {
				int code = resp.getStatusLine().getStatusCode();

				if (code == 400) {
					Log.e(TAG, "Connection failed. POST " + code
							+ " tv application not running.");
				} else if (code == 404) {
					Log.e(TAG, "Connection failed. POST " + code
							+ " requested application does not exist.");
				} else if (code == 409) {
					Log.e(TAG, "Connection failed. POST " + code
							+ " conflict on device ID.");
				} else if (code == 500) {
					Log.e(TAG, "Connection failed. POST " + code
							+ " server internal error 500");
				} else if (code == 503) {
					Log.e(TAG, "Connection failed. POST " + code
							+ " server may reach maximum connections.");
				} else {
					Log.e(TAG, "Connection failed. POST " + code);
				}
			}
		}

		// if connecting to tv failed
		return false;
	}

	/**
	 * Sends a http post request to disconnect from the tv.
	 */
	public boolean disconnectFromTV() {
		Log.i(TAG, "disconnect from tv...");

		HttpPost httppost = new HttpPost(uri + "disconnect");
		setDefaultHeaders(httppost);

		HttpParams httpParameters = new BasicHttpParams();
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		HttpResponse resp = null;

		// execute request
		try {
			resp = client.execute(host, httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "Can't dissconnect from tv.");
			e.printStackTrace();
		}

		// handle response
		if (resp != null && resp.getStatusLine().getStatusCode() == 200) {
			Log.d(TAG, "Disconnect from tv succeed.");
			return true;
		} else {
			Log.d(TAG, "Disconnect from tv failed. POST "
					+ resp.getStatusLine().getStatusCode() + "");
		}

		// if disconnecting from tv failed
		return false;
	}

	/**
	 * Sends a http post request with data to the connected tv. String format
	 * possible. First connects to the tv and then sends the message.
	 * @param data
	 */
	public boolean sendMessageToTV(String data) {
		this.connectToTV(tvname);
		Log.i(TAG, "send message to tv..." + data);

		HttpPost httppost = new HttpPost(uri + "queue");

		HttpParams httpParameters = new BasicHttpParams();
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		HttpResponse resp = null;

		setDefaultHeaders(httppost);

		// add data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("data", data.toString()));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			Log.e(TAG, "Can't set data for http post request.");
			e1.printStackTrace();
		}

		// execute request
		try {
			resp = client.execute(host, httppost);

		} catch (IOException e) {
			Log.e(TAG, "Can't send message to tv.");
			e.printStackTrace();
		}

		// handle response
		if (resp != null && resp.getStatusLine().getStatusCode() == 200) {
			Log.d(TAG, "Send message to tv succeed.");
			return true;
		} else {
			Log.d(TAG, "Send message to tv failed. POST "
					+ resp.getStatusLine().getStatusCode() + "");
		}

		// if sending message to tv failed
		return false;
	}

	/**
	 * Sends a http get request to the tv to receive a message from the queue.
	 * First connect to the tv and then get the message.
	 */
	public String getMessageFromTV() {
		this.connectToTV(tvname);
		Log.i(TAG, "get message from tv...");

		HttpGet httpget = new HttpGet(uri + "queue/device/" + deviceid);
		setDefaultHeaders(httpget);

		HttpParams httpParameters = new BasicHttpParams();
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		HttpResponse resp = null;

		// execute request
		try {
			resp = client.execute(host, httpget);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "Can't get message from tv.");
			e.printStackTrace();
		}

		// handle response
		if (resp != null && resp.getStatusLine().getStatusCode() == 200) {
			Log.d(TAG, "Get message from tv succeed.");

			// read message
			HttpEntity resEntityGet = resp.getEntity();
			if (resEntityGet != null) {
				try {
					String message = EntityUtils.toString(resEntityGet);
					Log.i(TAG, message);
					return message;
				} catch (ParseException e) {
					Log.e(TAG, "Can't parse response from tv.");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(TAG, "Can't read message from tv.");
					e.printStackTrace();
				}
			}

		} else {
			Log.d(TAG, "Get message from tv failed. GET "
					+ resp.getStatusLine().getStatusCode() + "");
		}

		// if reading or receiving message failed
		return null;
	}
}
