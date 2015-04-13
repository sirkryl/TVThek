// >> namespace assignment for convenience
var custom = window.deviceapis.customdevice || {};
var Main = {};

var gWidgetAPI;
var tvKey;
/*
var gnCustomDeviceID = -1;	//	Device id
var gsDevName = "";
var gsInputMsg = "not input yet";
*/
Main.onLoad = function()
{
	var saDevInfo = null;
	var nDevNum = 0;
	var i = 0;
    
	gWidgetAPI = new Common.API.Widget();	//	Create Common module 
	tvKey = new Common.API.TVKeyValue();
	gWidgetAPI.sendReadyEvent();			//	Send ready message to Application Manager 
    	
	// >> Register custom manager callback to receive device connect and disconnect events
	custom.registerManagerCallback(Main.onDeviceStatusChange);
	
	// >> Initializes custom device profile and gets available devices
	custom.getCustomDevices(Main.onCustomObtained);		
} 

Main.keyDown = function(){			// Key handler
    var keyCode = event.keyCode;
    alert("Main Key code : " + keyCode);
	
    switch (keyCode) {
        case tvKey.KEY_LEFT:
            alert("left");
            document.getElementById("bottom").innerHTML = "left button";
            break;
        case tvKey.KEY_RIGHT:
            alert("right");
            document.getElementById("bottom").innerHTML = "right button";
            break;
        case tvKey.KEY_UP:
            alert("up");
            document.getElementById("bottom").innerHTML = "up button";
            break;
        case tvKey.KEY_DOWN:
            alert("down");
            document.getElementById("bottom").innerHTML = "down button";
            break;
        case tvKey.KEY_ENTER:
            alert("enter");
			document.getElementById("bottom").innerHTML = "enter button";
            break;
        case tvKey.KEY_RETURN:
            break;
    }
}


//########################
// Connection TV - Handy
//########################
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
				document.getElementById('bottom').innerHTML = "Connected Custom: " + param.name;
			break;
		}
		// if a device disconnet
		case custom.MGR_EVENT_DEV_DISCONNECT:
		{
			alert("#### onDeviceStatusChange - MGR_EVENT_DEV_DISCONNECT ####");
			
			if(param.deviceType == custom.DEV_SMART_DEVICE)				
				document.getElementById('bottom').innerHTML = "Disconnected Custom: " + param.name;
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

//var deviceInstance;
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
    document.getElementById('bottom').innerHTML = "Received Message from Smart Device: " + message;
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

var actPlaylist = null;
var actualClip = -1;
var clipsNo = -1;
/* Set a new playlist in the UL 'cliplist' with all URLs. */
function handleReceivedPlaylist(playlist) {
	alert("received playlist " + playlist);
	
	// reset clip variables
	actualClip = -1;
	clipsNo = 0;
	
	// get cliplist element
	cliplist = document.getElementById("cliplist");
	
	// delete actual list
	while(cliplist.hasChildNodes()) {			
		knoten = cliplist.firstChild;
		cliplist.removeChild(knoten);
	}	
	// delete src of iframe
	document.getElementById("container").removeAttribute("src");
	
	// create new playlist from received string/format
	var pli = new Playlist(playlist);
	// set actual playlist
	actPlaylist = pli;
	
	document.getElementById("pl_name").nodeValue = pli.name;
	
	// add entries to list
	for(var i=0; i<pli.entries.length; i++) {
		var newEntry = document.createElement("li");
		newEntry.setAttribute("id", pli.entries[i].url);
		var newEntryName = document.createTextNode(pli.entries[i].name);
		newEntry.appendChild(newEntryName);
		cliplist.appendChild(newEntry);
		clipsNo++;
	}
	
	// play first clip of the playlist
	// playClip(0);
	// reduces number of clips so that this fits with the LI nodes
	clipsNo = clipsNo - 1;
}

/* Play a clip in the iframe element. */
function playClip(clipnumber) {
	alert("PLAY CLIP NO. " + clipnumber);
	// set old to default
	if(actualClip != -1) {
		document.getElementsByTagName("li")[actualClip].className = "";
	}	
	// set actual clip
	document.getElementsByTagName("li")[clipnumber].className = "actual";

	// get iframe element
	iframe = document.getElementById("container");	
	
	// get url of clip
	u = document.getElementsByTagName("li")[clipnumber].getAttribute("id");	
	
	// set the src of the iframe element
	iframe.setAttribute("src", u);
	actualClip = clipnumber;
	
	alert("play this clip: " + u);
}

/* Handle key events sent from the smart device. */
function handleKeyEvent(event) {
	alert("KEY EVENT: " + event);
	
	var s = event.split("+");
	event = s[0];
	
	switch(event) {
		case "PREV":
			alert("play previous clip");
			
			var clip = -1;
			if(actualClip == 0) {
				clip = clipsNo;
			}
			else {
				clip = actualClip - 1;
			}
			
			playClip(clip);
			break;
		case "NEXT":
			alert("play next clip");
			
			var clip = -1;
			if(actualClip == clipsNo) {
				clip = 0;
			}
			else {
				clip = actualClip + 1;
			}
			
			playClip(clip);
			break;
		case "PLAY":
			alert("play clip");
			
			doPlay();
			
			//TODO
			break;
		case "PAUSE":
			alert("pause clip");
			
			doPause();
			
			//TODO
			break;			
		case "URL":
			alert("set clip to url");
			
			if(s.length == 2) {				
				playClip(parseInt(s[1]));
			}
			
			break;
		default:
			alert("Not supported key event!");
			break;	
	}
}

function handlePlaylistRequest() {
	if(actPlaylist == null) {
		// no actual playlist is available
		sendMessageToDevice("PLI NULL");
	}
	else {
		// send actual playlist to device
		sendMessageToDevice(actPlaylist.getMessageFormat());
	}
}

var lastSrc = "";
/* Plays the last iframe src after stoping. */
function doPlay() {
	if(lastSrc != "") {
		document.getElementById("container").setAttribute("src", lastSrc);
	}
	
	alert("PLAY");
}

/* Is more like a stop at the moment.
   Clears the iframe src. */
function doPause() {
	lastSrc = document.getElementById("container").getAttribute("src");
	document.getElementById("container").setAttribute("src", "");
	
	alert("PAUSED");
}

/*########################*/
/* PLAYLIST OBJECT        */
/*########################*/

/* Creates a Playlist Object from a given string/format */
/* FORMAT:
   PLI playlistname NAME entryname1 URL entryurl1 DURATION entryduration1 
   NAME entryname2 URL entryurl2....
   */
function Playlist(playlistString) {
	//alert("create new playlist.");
	this.name = "";
	this.entries = [];
	
	string = playlistString.split("+");	
	this.name = string[0];
	
	var name = false;
	var url = false;
	var duration = false;
	for(var i=1; i<string.length;i++) {
		if(string[i] == "NAME") {
			name = true;
			continue;
		}
		if(name) {
			var entry = new PlaylistEntry(string[i]);
			name = false;
			continue;
		}
		if(string[i] == "URL") {
			url = true;
			continue;
		}
		if(url) {
			url = formatURL(string[i]);
			entry.url = url;			
			url = false;
			continue;
		}
		if(string[i] == "DURATION") {
			duration = true;
			continue;
		}
		if(duration) {
			d = formatDuration(string[i]);
			entry.duration = d;
			duration = false;
			
			this.addPlaylistEntry(entry);
			continue;
		}
	}	
}

Playlist.prototype.addPlaylistEntry = function(playlistEntry) {
	this.entries.push(playlistEntry);
	//alert(playlistEntry.name);
	//alert(playlistEntry.url);
}

/* FORMAT:
   PLI playlistname ACT actclip NAME entryname1 URL entryurl1 DURATION entryduration1 
   NAME entryname2 URL entryurl2....
   */
Playlist.prototype.getMessageFormat = function() {
	var message = "PLI " + this.name;
	message += " ACT " + actualClip;
	
	for(var i=0; i<this.entries.length; i++) {
		message += " NAME " + this.entries[i].name;
		message += " URL " + this.entries[i].url;
		message += " DURATION " + this.entries[i].duration;
	}
	alert("FORMAT FOR DEVICE " + message);
	return message;
}

/*########################*/
/* PLAYLISTENTRY OBJECT   */
/*########################*/

/* Creates a PlaylistEntry object */
function PlaylistEntry(name) {
	// alert("create new playlist entry.");
	this.name = name;
	this.url = "";
	this.duration = "";
}

/*##############################################################*/
/* HELPER METHODS ----------------------------------------------*/
/*##############################################################*/
function formatURL(malURL) {
	// get mal formed url and build it new
	var s = malURL.split("%2F");
	var u = "http://tvthek.orf.at/l/programs";
	
	for(var i=0; i<s.length; i++) {
		u += "/" + s[i];
	}	
	//alert(u);
	return u;
}

function formatDuration(malDuration) {
	// get mal formed duration and build it new
	var s = malDuration.split("%3A");
	var d = s[0];
	
	for(var i = 0; i<s.length; i++) {
		d += ":" + s[i];
	}
	
	//alert(d);
	return d;
}
