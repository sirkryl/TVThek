package fdts.android.activities;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import fdts.android.appname.R;
import fdts.android.entities.Playlist;
import fdts.android.entities.PlaylistEntry;

/**
 * Activity for the TV-Thek Tab.
 *
 */
public class TVThekActivity extends Activity {

	private String TAG = "TVThekActivity";
	private String savedUrl;
	private String activeUrl;
	private String asxurl;
	private boolean invoked = false;
	private String[] splitString;
	private Button addButton;
	private Button infoButton;
	private WebView engine;
	private Playlist currentPlaylist;
	private ColorStateList tmp_colorList;
	private String savedButtonText = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tvthek_view);
		activeUrl = new String();
		// init webview, enable javascript
		engine = (WebView) findViewById(R.id.web_engine);
		engine.getSettings().setJavaScriptEnabled(true);
		engine.loadUrl("http://tvthek.orf.at/l/");
		engine.setWebViewClient(new InternWebViewClient());

		// listen to buttonclicks
		addButton = (Button) findViewById(R.id.button_tvthekAddSpot);
		addButton.setEnabled(false);
		addButton.getBackground().setAlpha(128);
		tmp_colorList = addButton.getTextColors();
		if (savedButtonText.isEmpty()) {
			savedButtonText = addButton.getText().toString();
		}
		infoButton = (Button) findViewById(R.id.button_tvthekChoosePlaylist);

		// tmpInfoButtonHeight = infoButton.getHeight();
		currentPlaylist = ((MainActivity) getParent()).getActivePlaylist();
		// Log.d(TAG, "currentPlaylist: "+tmpInfoButtonHeight);

		if (currentPlaylist == null) {
			addButton.setTextColor(addButton.getTextColors().withAlpha(50));
			addButton
			.setText(Html
					.fromHtml(savedButtonText
							+ "<br/><small><font color='#2e2e2d'>(Keine Playlist ausgewählt)</font></small>"));
			infoButton.setVisibility(View.VISIBLE);
			// infoButton.setLayoutParams(new
			// LayoutParams(LayoutParams.MATCH_PARENT,
			// LayoutParams.WRAP_CONTENT));
		} else {
			invoked = true;
			infoButton.setVisibility(View.GONE);
			if (!activeUrl.isEmpty()) {
				addButton.setEnabled(true);
				addButton.getBackground().setAlpha(255);
				addButton.setTextColor(tmp_colorList);
				addButton.setText(Html.fromHtml(savedButtonText
						+ "<br/><small><font color='#a2a29d'>(Playlist: "
						+ currentPlaylist.getPlaylistName()
						+ ")</font></small>"));
			} else {
				addButton.setEnabled(false);
				addButton.getBackground().setAlpha(128);
				addButton.setTextColor(addButton.getTextColors().withAlpha(50));
				addButton.setText(Html.fromHtml(savedButtonText
						+ "<br/><small><font color='#2e2e2d'>(Playlist: "
						+ currentPlaylist.getPlaylistName()
						+ ")</font></small>"));
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		currentPlaylist = ((MainActivity) getParent()).getActivePlaylist();
		if (currentPlaylist == null) {
			invoked = false;
			addButton.setTextColor(addButton.getTextColors().withAlpha(50));
			addButton
			.setText(Html
					.fromHtml(savedButtonText
							+ "<br/><small><font color='#2e2e2d'>(Keine Playlist ausgewählt)</font></small>"));
			infoButton.setVisibility(View.VISIBLE);
			// infoButton.setLayoutParams(new
			// LayoutParams(LayoutParams.MATCH_PARENT,
			// LayoutParams.WRAP_CONTENT));
		} else {
			Log.d(TAG, "resume");
			invoked = true;
			infoButton.setVisibility(View.GONE);
			if (!activeUrl.isEmpty()) {
				addButton.setEnabled(true);
				addButton.getBackground().setAlpha(255);
				addButton.setTextColor(tmp_colorList);
				addButton.setText(Html.fromHtml(savedButtonText
						+ "<br/><small><font color='#a2a29d'>(Playlist: "
						+ currentPlaylist.getPlaylistName()
						+ ")</font></small>"));
			} else {
				addButton.setEnabled(false);
				addButton.getBackground().setAlpha(128);
				addButton.setTextColor(addButton.getTextColors().withAlpha(50));
				addButton.setText(Html.fromHtml(savedButtonText
						+ "<br/><small><font color='#2e2e2d'>(Playlist: "
						+ currentPlaylist.getPlaylistName()
						+ ")</font></small>"));
			}
		}

	}

	// reloads current page when reopening tab
	@Override
	public void onRestart() {
		super.onRestart();
		Log.d(TAG, "restart");
		engine.loadUrl("javascript:window.location.reload( true )");
	}

	/** Called when the 'Hinzufuegen'-Button is clicked */
	public void onButtonClick_button_addClip(View click) {
		if (click.equals(addButton)) {

			if (!activeUrl.isEmpty()) {
				savedUrl = activeUrl;
			}
		}
		if (invoked || currentPlaylist != null) {
			PlaylistEntry newEntry = new PlaylistEntry();
			String[] splitString = savedUrl.split("/");
			String name = splitString[splitString.length - 1];
			String duration = "00:" + (int) Math.abs(Math.random() * 60 + 1)
					+ ":" + (int) Math.abs(Math.random() * 60 + 1);

			newEntry = this.readAsx(asxurl);
			invoked = false;
			((MainActivity) getParent()).changeTabAndTell(2, 0, newEntry);
		}

	}

	/**
	 * Reads the .asx from the given URL and saves attributes to the entry
	 * @param senturl
	 * @return the Playlist Entry with attributes
	 */
	private PlaylistEntry readAsx(String senturl) {
		long start = System.currentTimeMillis();
		
		PlaylistEntry entry = new PlaylistEntry();
		TagNode node;
		String realUrl = senturl.replace("/l/", "/");
		String finalAsxUrl = "";

		String expression = "//a[@id='open_playlist']/@href";

		HtmlCleaner cleaner = new HtmlCleaner();
		CleanerProperties props = cleaner.getProperties();
		// props.setAllowHtmlInsideAttributes(true);
		// props.setAllowMultiWordAttributes(true);
		props.setNamespacesAware(false);
		props.setOmitComments(true);
		props.setOmitUnknownTags(true);
		props.setOmitDeprecatedTags(true);

		try {
			URL url = new URL(realUrl);
			URLConnection conn = url.openConnection();
			node = cleaner.clean(new InputStreamReader(conn.getInputStream()));
			Object[] nodes = node.evaluateXPath(expression);

			if (nodes.length >= 1) {
				finalAsxUrl = "http://tvthek.orf.at" + nodes[0].toString();
				Log.d(TAG, "ASX-URL: " + finalAsxUrl);
			}

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			URL url = new URL(finalAsxUrl);
			URLConnection conn = url.openConnection();

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			docBuilderFactory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			Document doc = docBuilder.parse(conn.getInputStream());

			// normalize text representation
			doc.getDocumentElement().normalize();

			// first title and duration are stored as main-tags for the new
			// entry
			NodeList mainTitleList = doc.getElementsByTagName("title");
			String mainTitle = mainTitleList.item(0).getChildNodes().item(0)
					.getNodeValue();
			entry.setName(mainTitle);

			NodeList mainDurationList = doc.getElementsByTagName("duration");

			Element valueElement = (Element) mainDurationList.item(0);

			String mainDuration = valueElement.getAttribute("value");
			entry.setDuration(mainDuration);

			// now each subentry is processed and stored accordingly
			NodeList entryList = doc.getElementsByTagName("entry");

			for (int i = 0; i < entryList.getLength(); i++) {
				Node entryNode = entryList.item(i);

				if (entryNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eTitle = (Element) entryNode;
					NodeList nodeList = eTitle.getElementsByTagName("title");

					// System.out.println("Title #"+(i+1)+": "
					// + nodeList.item(0).getChildNodes().item(0)
					// .getNodeValue());

					entry.addTitle(nodeList.item(0).getChildNodes().item(0)
							.getNodeValue());

					nodeList = eTitle.getElementsByTagName("ref");
					Node refNode = nodeList.item(0);
					Element eUrl = (Element) refNode;

					// System.out.println("URL #"+(i+1)+": "
					// + eUrl.getAttribute("href"));

					String tmpName = eUrl.getAttribute("href");
					tmpName = tmpName.split("cms-worldwide/")[1];
					tmpName = tmpName.split(".wmv")[0];

					// just the filename is sent - build full url on TV!!
					// tmpName =
					// "http://apasfw.apa.at/cms-worldwide/smil:"+tmpName+".smil/playlist.m3u8";

					// first url is used as "main url", as it is the starting
					// point
					if (i == 0) {
						entry.setUrl(tmpName);
					}

					entry.addUrl(tmpName);

				}

			}

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		return entry;
	}

	/** Called when the 'Info'-Button is clicked */
	public void onButtonClick_button_choosePlaylist(View click) {
		if (click.equals(infoButton)) {
			((MainActivity) getParent()).changeTabAndTell(2, 0);
		}
	}

	// called by MainActivity if this activity is being invoked by another
	// activity (i.e. PlaylistsActivity) that is expecting a value in return
	// (i.e. a clip)
	public void setInvoked(Playlist playlist) {
		invoked = true;
		currentPlaylist = ((MainActivity) getParent()).getActivePlaylist();
		infoButton.setVisibility(View.GONE);
		if (addButton.isEnabled()) {
			addButton.setText(Html.fromHtml(savedButtonText
					+ "<br/><small><font color='#a2a29d'>(Playlist: "
					+ currentPlaylist.getPlaylistName() + ")</font></small>"));
		} else
			addButton.setText(Html.fromHtml(savedButtonText
					+ "<br/><small><font color='#2e2e2d'>(Playlist: "
					+ currentPlaylist.getPlaylistName() + ")</font></small>"));
	}

	// allows to go back in browsers history
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_BACK) && engine.canGoBack()) {
			engine.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// Class to overwrite loadUrl-function to avoid opening the standard browser
	// when loading a new url
	private class InternWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO: Schoenere Ueberpruefung, ob auf TVThek oder nicht
			// only load url if it's inside tvthek
			if (url.contains("tvthek.orf.at/l/")) {
				view.loadUrl(url);
				asxurl = url;

				/**
				 * cut string to shorten the final concatenated url-string, for
				 * /programs and /topics at the same time enables or disables
				 * the add button, depending on whether the current site
				 * contains a valid clip or not.
				 */
				splitString = url.split("/programs/");

				if (splitString.length >= 2) {
					activeUrl = splitString[1];
					if (invoked) {

						Log.d(TAG, "Url http://tvthek.orf.at/l/programs/" + url
								+ " added.");
						addButton.setEnabled(true);
						addButton.getBackground().setAlpha(255);
						addButton.setTextColor(tmp_colorList);
						addButton
						.setText(Html
								.fromHtml(savedButtonText
										+ "<br/><small><font color='#a2a29d'>(Playlist: "
										+ currentPlaylist
										.getPlaylistName()
										+ ")</font></small>"));
					}
				} else {

					splitString = url.split("/topics/");
					if (splitString.length >= 2) {
						activeUrl = splitString[1];
						if (invoked) {
							Log.d(TAG, "Url http://tvthek.orf.at/l/topics/"
									+ url + " added.");
							addButton.setEnabled(true);
							addButton.getBackground().setAlpha(255);
							addButton.setTextColor(tmp_colorList);
							addButton
							.setText(Html
									.fromHtml(savedButtonText
											+ "<br/><small><font color='#a2a29d'>(Playlist: "
											+ currentPlaylist
											.getPlaylistName()
											+ ")</font></small>"));
						}
					} else {
						addButton.setEnabled(false);
						addButton.getBackground().setAlpha(128);
						addButton.setTextColor(addButton.getTextColors()
								.withAlpha(50));
						if (currentPlaylist != null)
							addButton
							.setText(Html
									.fromHtml(savedButtonText
											+ "<br/><small><font color='#2e2e2d'>(Playlist: "
											+ currentPlaylist
											.getPlaylistName()
											+ ")</font></small>"));
						else
							addButton
							.setText(Html
									.fromHtml(savedButtonText
											+ "<br/><small><font color='#2e2e2d'>(Keine Playlist ausgewählt)</font></small>"));
						activeUrl = new String();
					}
				}
				return true;
			} else
				return false;

		}
	}

}
