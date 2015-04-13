/*
	Main.js contains the primary functionality and is also the starting point for this application.
	This is based on the Samsung Tutorial http://www.samsungdforum.com/Guide/View/Developer_Documentation/Samsung_SmartTV_Developer_Documentation_2.5/JavaScript/Play_Audio_and_Video/Tutorial_Creating_a_Video_Application_With_HAS_%28HTTP_Adaptive_Streaming%29
*/

var widgetAPI = new Common.API.Widget();
var custom = window.deviceapis.customdevice || {};
var tvKey = new Common.API.TVKeyValue();
var fileSystem = new FileSystem();

var Main =
{
    selectedVideo : 0,
    mode : 0,
    mute : 0,
    
    UP : 0,
    DOWN : 1,

    WINDOW : 0,
    FULLSCREEN : 1,
    
    NMUTE : 0,
    YMUTE : 1
}

/* called in the beginning, directly after starting the app */
Main.onLoad = function()
{
	var saDevInfo = null;
	var nDevNum = 0;
	var i = 0;
	
	//initialise all the different parts and set the player up if everything went smoothly
    if ( Player.init() && Audio.init() && Display.init() && Server.init() )
    {
        Display.setVolume( Audio.getVolume() );
        Display.setTime(0);
		
		
        Player.stopCallback = function()
        {
            /* Return to windowed mode when video is stopped
                (by choice or when it reaches the end) */
            Main.setWindowMode();
        }

        // Start retrieving data from server
        Server.dataReceivedCallback = function()
            {
                /* Use video information when it has arrived */
                Display.setVideoList( Data.getVideoNames() );
                Main.updateCurrentVideo();
            }
			
        Server.fetchVideoList(); /* Request video information from server */

        // Enable key event processing
        this.enableKeys();

        widgetAPI.sendReadyEvent();    
		// >> Register custom manager callback to receive device connect and disconnect events
	custom.registerManagerCallback(Main.onDeviceStatusChange);
	
	// >> Initializes custom device profile and gets available devices
	custom.getCustomDevices(Main.onCustomObtained);		
    }
    else
    {
        alert("Failed to initialise");
    }
}

/* called when the application is shutdown */
Main.onUnload = function()
{
    Player.deinit();
}

/* change the video that is currently played */
Main.updateCurrentVideo = function(move)
{
    Player.setVideoURL( Data.getEntryURLs(this.selectedVideo) );
    
	//changes the displayed content
    Display.setVideoListPosition(this.selectedVideo, move);
	
    Display.setDescription( Data.getEntryTitles(this.selectedVideo),0);
}

Main.enableKeys = function()
{
	//register keys to application
	var appCommon = document.getElementById('pluginObjectAppCommon');
	appCommon.RegisterKey(tvKey.KEY_VOL_UP);
	appCommon.RegisterKey(tvKey.KEY_VOL_DOWN);
	appCommon.RegisterKey(tvKey.KEY_MUTE);
	appCommon.RegisterKey(tvKey.KEY_PLAY);
    document.getElementById("anchor").focus();
}

Main.keyDown = function()
{
    var keyCode = event.keyCode;
    
    switch(keyCode)
    {
        case tvKey.KEY_RETURN:
        case tvKey.KEY_PANEL_RETURN:
            alert("RETURN");
            Player.stopVideo();
            widgetAPI.sendReturnEvent(); 
            break;    
            break;
    
        case tvKey.KEY_PLAY:
            alert("PLAY");
            this.handlePlayKey();
            var playerplugin =  document.getElementById("pluginPlayer");   
            break;
            
        case tvKey.KEY_STOP:
            alert("STOP");
            Player.stopVideo();
            break;
            
        case tvKey.KEY_PAUSE:
            alert("PAUSE");
            this.handlePauseKey();
            break;
            
        case tvKey.KEY_FF:
            alert("FF");
            if(Player.getState() != Player.PAUSED)
                Player.skipForwardVideo();
            break;
        
        case tvKey.KEY_RW:
            alert("RW");
            if(Player.getState() != Player.PAUSED)
                Player.skipBackwardVideo();
            break;

        case tvKey.KEY_VOL_UP:
        case tvKey.KEY_PANEL_VOL_UP:
            alert("VOL_UP");
            if(this.mute != 0)
                this.noMuteMode();
			Audio.setRelativeVolume(0);
            break;
            
        case tvKey.KEY_VOL_DOWN:
        case tvKey.KEY_PANEL_VOL_DOWN:
            alert("VOL_DOWN");
            if(this.mute != 0)
                this.noMuteMode();
			Audio.setRelativeVolume(1);
            break;      
        case tvKey.KEY_DOWN:
            alert("DOWN");
            this.selectNextVideo(this.DOWN);
            break;
            
        case tvKey.KEY_UP:
            alert("UP");
            this.selectPreviousVideo(this.UP);
            break;            

		case tvKey.KEY_RIGHT:
			alert("RIGHT");
			Player.playNextClip();
			break;
		case tvKey.KEY_LEFT:
			alert("LEFT");
			Player.playPreviousClip();
			break;
        case tvKey.KEY_ENTER:
        case tvKey.KEY_PANEL_ENTER:
            alert("ENTER");
            this.toggleMode();
            break;
        
        case tvKey.KEY_MUTE:
            alert("MUTE");
            this.muteMode();
            break;
            
        default:
            alert("Unhandled key");
            break;
    }
}

Main.handlePlayKey = function()
{
    switch ( Player.getState() )
    {
        case Player.STOPPED:
            Player.playVideo();
            break;
            
        case Player.PAUSED:
            Player.resumeVideo();
            break;
            
        default:
            alert("Ignoring play key, not in correct state");
            break;
    }
}

Main.handlePauseKey = function()
{
    switch ( Player.getState() )
    {
        case Player.PLAYING:
            Player.pauseVideo();
            break;
        
        default:
            alert("Ignoring pause key, not in correct state");
            break;
    }
}

/* select the next clip */
Main.selectNextVideo = function(down)
{
    Player.stopVideo();
    this.selectedVideo = (this.selectedVideo + 1) % Data.getVideoCount();

    this.updateCurrentVideo(down);
}

/* select the previous clip */
Main.selectPreviousVideo = function(up)
{
    Player.stopVideo();
    if (--this.selectedVideo < 0)
    {
        this.selectedVideo += Data.getVideoCount();
    }

    this.updateCurrentVideo(up);
}

Main.setFullScreenMode = function()
{
    if (this.mode != this.FULLSCREEN)
    {
        Display.hide();
        
        Player.setFullscreen();
        
        this.mode = this.FULLSCREEN;
    }
}

Main.setWindowMode = function()
{
    if (this.mode != this.WINDOW)
    {
        Display.show();
        
        Player.setWindow();
        
        this.mode = this.WINDOW;
    }
}

/* change to fullscreen mode or back to windowed mode if we're already there */
Main.toggleMode = function()
{
    switch (this.mode)
    {
        case this.WINDOW:
            this.setFullScreenMode();
            break;
            
        case this.FULLSCREEN:
            this.setWindowMode();
            break;
            
        default:
            alert("ERROR: unexpected mode in toggleMode");
            break;
    }
}

/* mute is handled IN the application, not by the TV itself */
Main.setMuteMode = function()
{
    if (this.mute != this.YMUTE)
    {
        var volumeElement = document.getElementById("volumeInfo");
        Audio.plugin.SetSystemMute(true);
		
		//change appearance of volumebar to reflect that it's in mute mode
        document.getElementById("volumeBar").style.backgroundImage = 'url("Images/videoBox/muteBar.png")';
        document.getElementById("volumeIcon").style.backgroundImage = 'url("Images/videoBox/mute.png")';
        widgetAPI.putInnerHTML(volumeElement, "MUTE");
		
        this.mute = this.YMUTE;
    }
}

/* switches back from mute mode to the previously set volume */
Main.noMuteMode = function()
{
    if (this.mute != this.NMUTE)
    {
        Audio.plugin.SetSystemMute(false); 
		
		//change appearance of volumebar back to normal
        document.getElementById("volumeBar").style.backgroundImage = 'url("Images/videoBox/volumeBar.png")';
        document.getElementById("volumeIcon").style.backgroundImage = 'url("Images/videoBox/volume.png")';
        Display.setVolume( Audio.getVolume() );
        this.mute = this.NMUTE;
    }
}

Main.muteMode = function()
{
    switch (this.mute)
    {
        case this.NMUTE:
            this.setMuteMode();
            break;
            
        case this.YMUTE:
            this.noMuteMode();
            break;
            
        default:
            alert("ERROR: unexpected mode in muteMode");
            break;
    }
}

Main.setEntryNumber = function(number)
{
	Display.setDescription( Data.getEntryTitles(this.selectedVideo),number);
}

//########################
// Connection TV - Handy
//########################
// This part is adopted and ajusted from Samsungs Tutorial: Creating a Convergence Application
// http://www.samsungdforum.com/Guide/View/Developer_Documentation/Samsung_SmartTV_Developer_Documentation_3.5/JavaScript/Convergence_App/Tutorial_Creating_a_Convergence_Application

/* Callback function that get called when a device changes its status. */
Main.onDeviceStatusChange = function(param)
{
//	alert("#### onDeviceStatusChange - Device status change received ####");
//	alert("#### onDeviceStatusChange - event type is " + param.eventType + " ####");
	alert("#### onDeviceStatusChange - event device name is " + param.name + " ####");
//	alert("#### onDeviceStatusChange - event device type is " + param.deviceType + " ####");

	switch( Number(param.eventType) )
	{
		// if a device connect
		case custom.MGR_EVENT_DEV_CONNECT:
		{
			alert("#### onDeviceStatusChange - MGR_EVENT_DEV_CONNECT ####");
			
			if(param.deviceType == custom.DEV_SMART_DEVICE)
				//document.getElementById('status').innerHTML = "Connected Custom: " + param.name;
			break;
		}
		// if a device disconnet
		case custom.MGR_EVENT_DEV_DISCONNECT:
		{
			alert("#### onDeviceStatusChange - MGR_EVENT_DEV_DISCONNECT ####");
			
			if(param.deviceType == custom.DEV_SMART_DEVICE)				
				//document.getElementById('status').innerHTML = "Disconnected Custom: " + param.name;
			break;
		}
		default:
		{
			alert("#### onDeviceStatusChange - Unknown event ####");
			break
		}
	}
	custom.getCustomDevices(Main.onCustomObtained);
}

/* Main.onCustomObtained Called by getCustomDevices in Main.onDeviceStatusChange callback function 
when a device is connected or disconnected */ 
/* To support multi device, refer to tutorial app code. */
Main.onCustomObtained = function(customs)
{
	if(customs.length > 0)
	{
		alert("#### onCustomObtained - found " + customs.length + " custom device(s) ####");
		if(customs[0]!=null && customs[0].getType() == custom.DEV_SMART_DEVICE)
		{
			alert("#### onCustomObtained - get device instance ####");
			deviceInstance = customs[0];
			deviceInstance.registerDeviceCallback(Main.onDeviceEvent);
		}
	}
	else
	{
		alert("#### onCustomObtained - no custom device found ####");
	}
} 

/* Main.onDeviceEvent is called when device instance event arrive. 
Example displays event type on console and on message arrival performs other functions 
provided by CustomDevice interface */
Main.onDeviceEvent = function(sParam)
{
	alert("#### - First smart device - ####");
	switch(Number(sParam.infoType))
	{
		case custom.DEV_EVENT_MESSAGE_RECEIVED:
			alert("#### onDeviceEvent -1- DEV_EVENT_MESSAGE_RECEIVED:" + sParam.data.message1);
                	// sParam.sEventData.sMessage1 -> message body; sParam.sEventData.sMessage2 -> context
                	Main.onMessageReceived(sParam.data.message1, sParam.data.message2);
			break;
		case custom.DEV_EVENT_JOINED_GROUP:
			alert("#### onDeviceEvent -1- DEV_EVENT_JOINED_GROUP ####");
			// no interest
			break;
		case custom.DEV_EVENT_LEFT_GROUP:
			alert("#### onDeviceEvent -1- DEV_EVENT_LEFT_GROUP ####");
			// no interest
			break;
		default:
			alert("#### onDeviceEvent -1- Unknown event ####");
			break;
	}
}

/* Sending a message to a device */
function sendMessageToDevice(message)
{
	 alert(message);
	 
     // customdeviceInstance is the instance related with one device.
     deviceInstance.sendMessage(message);
     return;
}

/* Get called by the onDeviceEvent method, when a message received from a smart device. 
A message sent by a smart device can be a plain string, an XML string or a JSON string. */
/*
PLI - received a playlist with clips defined as URL
KEY - received a KEY event
RLI - received a playlist request
*/
Main.onMessageReceived = function(message, context)
{
    // message -> message body
    // context -> message context (headers and etc)
    alert("#### onMessageReceived:" + message);
    //document.getElementById('description_top').innerHTML = "Received Message from Smart Device: " + message;
	var tmp = message.substring(5);
	
	var w = tmp.substr(0,3);
	alert(w);
	switch(w) {
		case "PLI":
			alert("RECEIVED A PLAYLIST");
			
			handleReceivedPlaylist(tmp.substring(4));
			break;	
		case "KEY":
			alert("RECEIVED KEY EVENT");
			
			handleKeyEvent(tmp.substring(4));
			break;
		case "RLI":
			alert("RECEIVED PLAYLIST REQUEST");
			
			handlePlaylistRequest();
			break;
		default:
			alert("OH NO!");
			break;
	}
}

//############################
// END Connection TV - Handy
//############################


//################################
// Handling requests and events.
//################################
// This part contains some methods to handle different requests and events.
var actPlaylist = null;
var actualClip = -1;
var clipsNo = -1;

/* Set a new playlist in the UL 'cliplist' with all URLs. */
function handleReceivedPlaylist(playlist) {
	alert("received playlist " + playlist);

	// create new playlist from received string/format
	var pli = new Playlist(playlist);
	// save to memory
	pli.saveToXML();
	
	// set actual playlist
	actPlaylist = pli;
	Server.fetchVideoList();
}

/* Handle key events sent from the smart device. */
function handleKeyEvent(event) {
	alert("KEY EVENT: " + event);
	
	var s = event.split("+");
	event = s[0];
	
	switch(event) {
		case "PREV":
			alert("play previous clip");
			Main.selectPreviousVideo(this.UP);
			break;
		case "NEXT":
			alert("play next clip");
			
			Main.selectNextVideo(this.DOWN);
			break;
		case "PLAY":
			alert("play clip");
			
			Main.handlePlayKey();
            var playerplugin =  document.getElementById("pluginPlayer");   
			break;
		case "PAUSE":
			alert("pause clip");
			
			Main.handlePauseKey();
			break;			
		/*
		case "URL":
			alert("set clip to url");
			
			if(s.length == 2) {				
				playClip(parseInt(s[1]));
			}
			
			break;
		*/
		default:
			alert("Not supported key event!");
			break;	
	}
}

/* Handling a playlist request from a smart device. 
The actual playlist get formated and sent back to the smart device. */
function handlePlaylistRequest() {
	// if the actual playlist is null, there is no playlist sent during this session but it still can be one in the memory
	if(actPlaylist == null) {
		// is there a list in the memory?
		list = createPlaylistFromMemory();
		
		if(list == null) {
			// no actual playlist is available
			alert("no actual playlist available");
			sendMessageToDevice("PLI NULL");
		}
		else {
			alert("created playlist from memory");
			actPlaylist = list;
			
			// send actual playlist to device
			sendMessageToDevice(actPlaylist.getMessageFormat());
		}
	}
	else {
		// send actual playlist to device
		sendMessageToDevice(actPlaylist.getMessageFormat());
	}
}

/*########################*/
/* PLAYLIST OBJECT        */
/*########################*/
// This parts represents a Playlist object and some methods.

/* Creates a Playlist Object from a given string/format */
/* FORMAT:   
   PLI playlistname CLIP NAME haupttitel1 DURATION hauptdauer1 ENTRY TITLE subtitel1 URL url
   CLIP NAME haupttitel2 DURATION hauptdauer2 ENTRY TITLE subtitel URL url
   */
function Playlist(playlistString) {
	//alert("create new playlist.");
	this.name = "";
	this.clips = [];	
	
	//document.getElementById('description_top').innerHTML = playlistString;
	
	//tja+mich+CLIP+ZIB+20+DURATION+00:06:38.942
	//+ENTRY++TITLE+1:+Signation+URL+2012-09-26_200_tl01_ZIB-20_Signation__47674401__o
	//+ENTRY++TITLE+2:+Zeugenschwund+beim+U-Ausschuss+URL+2012-09-26_2000
	//+CLIP+ZIB+20+DURATION+00:06:38.942
	//+ENTRY++TITLE+1:+Signation+URL+2012-09-26_200_tl01_ZIB-20_Signation__47674401__o
	//+ENTRY++TITLE+2:+Zeugenschwund+beim+U-Ausschuss+URL+2012-09-26_2000
	
	// decode string
	playlistString = playlistString.replace(/[+]/g, " ");
	playlistString = decodeURIComponent(playlistString);	
	
	// playlist name
	posName = playlistString.indexOf(" CLIP ");
	n = playlistString.substring(0, posName);
	this.name = n;
	
	// split clips
	string = playlistString.split(" CLIP ");
	for(var i=1; i<string.length; i++) {		
		// get title
		posDuration = string[i].indexOf(" DURATION ");
		title = string[i].substring(0,posDuration);
		
		var clip = new PlaylistClip(title);		
		
		// get duration
		posEntry = string[i].indexOf(" ENTRY ");
		clip.duration = string[i].substring(posDuration + (" DURATION ").length, posEntry); 
				
		// get entries
		var entries = [];
		var e = string[i].substring(posEntry).split(" ENTRY ");
		
		// go through all entries
		for(var j=1; j<e.length; j++) {			
			// get title
			posTitle = e[j].indexOf(" TITLE ");
			posUrl = e[j].indexOf(" URL ");
			
			title = e[j].substring(posTitle + (" TITLE ").length, posUrl);
			var entry = new PlaylistEntry(title);
			
			// get url
			entry.url = e[j].substring(posUrl + (" URL ").length);
			entries.push(entry);			
		}		
		clip.entries = entries;
		
		// add this clip to the playlist
		this.addPlaylistClip(clip);
	}	
}

/* Adds a given clip to the playlist. */
Playlist.prototype.addPlaylistClip = function(playlistClip) {
	this.clips.push(playlistClip);
}

/* Create a xml document from the playlist. */
Playlist.prototype.saveToXML = function () {
	alert("Save playlist to a xml file.");		
	
	var xmlDoc = createXMLDoc();
	
	var channel = xmlDoc.createElement ("channel");
	
	// append items
	for(var i = 0; i < this.clips.length; i++) {
		var item = xmlDoc.createElement("item");
		var clip = this.clips[i];
		
		title = xmlDoc.createElement("title");
		title.appendChild(xmlDoc.createTextNode(clip.name));
		duration = xmlDoc.createElement("duration");
		duration.appendChild(xmlDoc.createTextNode(clip.duration));
		
		item.appendChild(title);
		item.appendChild(duration);
		
		alert(clip.entries.length);
		// append entries
		for(var j = 0; j < clip.entries.length; j++) {
			var entry = xmlDoc.createElement("entry");
			
			var e = clip.entries[j];
			title = xmlDoc.createElement("title");
			title.appendChild(xmlDoc.createTextNode(e.title));
			link = xmlDoc.createElement("link");
			// create the needed url format
			link.appendChild(xmlDoc.createTextNode("http://apasfw.apa.at/cms-worldwide/smil:" + e.url + ".smil/playlist.m3u8|COMPONENT=HLS"));
			
			entry.appendChild(title);
			entry.appendChild(link);
			
			item.appendChild(entry);
		}		
		channel.appendChild(item);
	}
	
    xmlDoc.documentElement.appendChild (channel);
	
	// create a string from the xml document to save it to the memory
	var string = (new XMLSerializer()).serializeToString(xmlDoc);
	alert(string);
	
	saveStringToFile(string, "videoList.xml");
}

/* Creates the needed format from the playlist to send it back to the smart device.
FORMAT:
PLI playlistname ACT actclip NAME entryname1 URL entryurl1 DURATION entryduration1 
NAME entryname2 URL entryurl2....   */
Playlist.prototype.getMessageFormat = function() {
	var message = "PLI " + this.name;
	
	// TODO actual clip information is not used temporarly
	actualClip = 0;
	message += " ACT " + actualClip;
	
	for(var i=0; i<this.clips.length; i++) {
		message += " CLIP " + this.clips[i].name;
		
		for(var j=0; j<this.clips[i].entries.length; j++) {
			message += " ENTRY " + this.clips[i].entries[j].title;
			message += " URL " + this.clips[i].entries[j].url;
		}
	}
	alert("FORMAT FOR DEVICE " + message);
	return message;
}


/*########################*/
/* PLAYLISTCLIP OBJECT    */
/*########################*/
// This part represents a PlaylistClip Object. A PlaylistClip is a part of the Playlist.

/* The PlaylistClip object */
function PlaylistClip(name) {
	this.name = name;
	this.duration = "";
	this.entries = [];
}

/*########################*/
/* PLAYLISTENTRY OBJECT   */
/*########################*/
// This part represents a PlaylistEntry Object. A PlaylistEntry is a part of the PlaylistClip.

/* Creates a PlaylistEntry object */
function PlaylistEntry(title) {
	// alert("create new playlist entry.");
	this.title = title;
	this.url = "";
}

/*##############################################################*/
/* HELPER METHODS ----------------------------------------------*/
/*##############################################################*/
/* Creates a xml document for the playlist. */
function createXMLDoc() {
	var xmlDoc = document.implementation.createDocument("", "rss", "");
	
	return xmlDoc;
}

/* Saves a string to a file in the database. */
function saveStringToFile(string, filename) {
	var fileObj = fileSystem.openCommonFile(curWidget.id + filename, 'w');
	if(!fileObj){
		var bValid = fileSystem.isValidCommonPath(curWidget.id);
		if (!bValid) {
			alert('does not exist');
			fileSystem.createCommonDir(curWidget.id);  
		}
	}

	fileObj = fileSystem.openCommonFile(curWidget.id + filename, 'w');
	fileObj.writeLine(string);
	fileSystem.closeCommonFile(fileObj);		
}

/* Reads the playlist in the memory and creates a playlist variable from that. */
function createPlaylistFromMemory() {
	list = openFileAndParseXML("videoList.xml");
	alert(list);

	var items = list.getElementsByTagName("item");
	
	if(items == null) {
		return null;
	}
	
	//tja+mich+CLIP+ZIB+20+DURATION+00:06:38.942
	//+ENTRY++TITLE+1:+Signation+URL+2012-09-26_200_tl01_ZIB-20_Signation__47674401__o
	//+ENTRY++TITLE+2:+Zeugenschwund+beim+U-Ausschuss+URL+2012-09-26_2000
	//+CLIP+ZIB+20+DURATION+00:06:38.942
	//+ENTRY++TITLE+1:+Signation+URL+2012-09-26_200_tl01_ZIB-20_Signation__47674401__o
	//+ENTRY++TITLE+2:+Zeugenschwund+beim+U-Ausschuss+URL+2012-09-26_2000
	var playlistString = "playlistFromMemory";
		
    var videoNames = [ ];
    var videoURLs = [ ];
    var videoDurations = [ ];
    var entryTitles = [[],[]];
	var entryURLs = [[],[]];
    for (var index = 0; index < items.length; index++)
    {
        var titleElement = items[index].getElementsByTagName("title")[0].firstChild.data;
        var durationElement = items[index].getElementsByTagName("duration")[0].firstChild.data;
		var entries = items[index].getElementsByTagName('entry');
		
		playlistString += " CLIP " + titleElement + " DURATION " + durationElement;
				
		var entryTitleElement = [ ];
		var entryURLElement = [ ]
		for (var j = 0; j < entries.length; j++)
		{
			entryTitleElement[j] = entries[j].getElementsByTagName("title")[0].firstChild.data;
			if(j == 0)	var linkElement = entries[j].getElementsByTagName("link")[0];
				entryURLElement[j] = entries[j].getElementsByTagName("link")[0].firstChild.data;	
				
			playlistString += " ENTRY " + entryTitleElement[j] + " URL " + entryURLElement[j];
		}
	 }
	 
	 alert(playlistString);
	 
	 tmp_pli = new Playlist(playlistString);
	 return tmp_pli;
}
