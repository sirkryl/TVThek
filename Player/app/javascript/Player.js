/*
	Player.js is responsible for all the video-related functions but audio.
	This is based on the Samsung Tutorial http://www.samsungdforum.com/Guide/View/Developer_Documentation/Samsung_SmartTV_Developer_Documentation_2.5/JavaScript/Play_Audio_and_Video/Tutorial_Creating_a_Video_Application_With_HAS_%28HTTP_Adaptive_Streaming%29
*/

var Player =
{
    plugin : null,
    state : -1,
    skipState : -1,
    stopCallback : null,    /* Callback function to be set by client */
    originalSource : null,
    
    STOPPED : 0,
    PLAYING : 1,
    PAUSED : 2,  
    FORWARD : 3,
    REWIND : 4
}

Player.init = function()
{
    var success = true;
    
    this.state = this.STOPPED;
    
    this.plugin = document.getElementById("pluginPlayer");
    
    if (!this.plugin)
    {
         success = false;
    }
    else
    {
        var mwPlugin = document.getElementById("pluginTVMW");
        
        if (!mwPlugin)
        {
            success = false;
        }
        else
        {
            /* Save current TV Source */
            this.originalSource = mwPlugin.GetSource();
            
            /* Set TV source to media player plugin */
            mwPlugin.SetMediaSource();
        }
    }
    
    this.setWindow();
    
	//assign methods to events
    this.plugin.OnCurrentPlayTime = 'Player.setCurTime';
    this.plugin.OnStreamInfoReady = 'Player.setTotalTime';
    this.plugin.OnBufferingStart = 'Player.onBufferingStart';
    this.plugin.OnBufferingProgress = 'Player.onBufferingProgress';
    this.plugin.OnBufferingComplete = 'Player.onBufferingComplete';           
            
    return success;
}

Player.deinit = function()
{
        var mwPlugin = document.getElementById("pluginTVMW");
        
        if (mwPlugin && (this.originalSource != null) )
        {
            /* Restore original TV source before closing the widget */
            mwPlugin.SetSource(this.originalSource);
            alert("Restore source to " + this.originalSource);
        }
}

Player.setWindow = function()
{
    this.plugin.SetDisplayArea(391, 60, 510, 287);
}

Player.setFullscreen = function()
{
    this.plugin.SetDisplayArea(0, 0, 960, 540);
}

//changes the current video url(s)
Player.setVideoURL = function(url)
{
	//the first entry-url is stored as an entrypoint, that's where the player is supposed to start playback
    this.url = url[0];
	
	//set entryIndex back to 0, because we're back at the start
	this.entryIndex = 0;
	
	//save the rest of the entry urls that this show contains
	this.urlList = url;
}

//play video and change playback-control elements to reflect the current state - also sets a buffersize.
Player.playVideo = function()
{
    if (this.url == null)
    {
        alert("No videos to play");
    }
    else
    {
        this.state = this.PLAYING;
        document.getElementById("play").style.opacity = '0.2';
        document.getElementById("stop").style.opacity = '1.0';
        document.getElementById("pause").style.opacity = '1.0';
        document.getElementById("forward").style.opacity = '1.0';
        document.getElementById("rewind").style.opacity = '1.0';
        Display.status("Play");
        this.setWindow();
        
        this.plugin.SetInitialBuffer(640*1024);
        this.plugin.SetPendingBuffer(640*1024); 
       
        this.plugin.Play( this.url );
        Audio.plugin.SetSystemMute(false);
    }
}

/* pause video and change playback-control elements to reflect the current state. */
Player.pauseVideo = function()
{
    this.state = this.PAUSED;
    document.getElementById("play").style.opacity = '1.0';
    document.getElementById("stop").style.opacity = '1.0';
    document.getElementById("pause").style.opacity = '0.2';
    document.getElementById("forward").style.opacity = '0.2';
    document.getElementById("rewind").style.opacity = '0.2';
    Display.status("Pause");
    this.plugin.Pause();
}

/* stop video and change playback-control elements to reflect the current state. */
Player.stopVideo = function()
{
    if (this.state != this.STOPPED)
    {
        this.state = this.STOPPED;
        document.getElementById("play").style.opacity = '1.0';
        document.getElementById("stop").style.opacity = '0.2';
        document.getElementById("pause").style.opacity = '0.2';
        document.getElementById("forward").style.opacity = '0.2';
        document.getElementById("rewind").style.opacity = '0.2';
        Display.status("Stop");
        this.plugin.Stop();
        Display.setTime(0);
        
        if (this.stopCallback)
        {
            this.stopCallback();
        }
    }
    else
    {
        alert("Ignoring stop request, not in correct state");
    }
}

/* if a video is paused and the pause-button is clicked, this function is called */
Player.resumeVideo = function()
{
    this.state = this.PLAYING;
    document.getElementById("play").style.opacity = '0.2';
    document.getElementById("stop").style.opacity = '1.0';
    document.getElementById("pause").style.opacity = '1.0';
    document.getElementById("forward").style.opacity = '1.0';
    document.getElementById("rewind").style.opacity = '1.0';
    Display.status("Play");
    this.plugin.Resume();
}

/* skip ahead a certain amount of time */
Player.skipForwardVideo = function()
{
    this.skipState = this.FORWARD;
    this.plugin.JumpForward(5);    
}

/* rewind a certain amount of time */
Player.skipBackwardVideo = function()
{
    this.skipState = this.REWIND;
    this.plugin.JumpBackward(5);
}

Player.getState = function()
{
    return this.state;
}

/* Global functions called directly by the player */

Player.onBufferingStart = function()
{
    Display.status("Buffering...");
    switch(this.skipState)
    {
        case this.FORWARD:
            document.getElementById("forward").style.opacity = '0.2';
            break;
        
        case this.REWIND:
            document.getElementById("rewind").style.opacity = '0.2';
            break;
    }
}

Player.onBufferingProgress = function(percent)
{
    Display.status("Buffering...:" + percent + "%");
}

Player.onBufferingComplete = function()
{
    Display.status("Playing...");
    switch(this.skipState)
    {
        case this.FORWARD:
            document.getElementById("forward").style.opacity = '1.0';
            break;
        
        case this.REWIND:
            document.getElementById("rewind").style.opacity = '1.0';
            break;
    }
}

/* set the current time whenever it changes */
Player.setCurTime = function(time)
{
    Display.setTime(time);

	//if a video is finished and there are more entries left, play the next one, otherwise go back to the first entry and stop
	if(time == Player.plugin.GetDuration())
	{
		if(this.entryIndex >= this.urlList.length-1)
		{
			this.entryIndex = 0;
			this.url = this.urlList[0];
			Main.setEntryNumber(0);
		}
		else Player.playNextClip();
	}
}

/* skip to the next entry clip if there is one, otherwise go back to the first one */
Player.playNextClip = function()
{
	if(this.urlList.length == 1) return;
	
	if(this.entryIndex >= this.urlList.length-1)
	{
		this.entryIndex = 0;
	}
	else
	{
		this.entryIndex = this.entryIndex+1;
	}
	Player.stopVideo();
	alert(this.entryIndex);
	this.url = this.urlList[this.entryIndex];
	
	Main.setEntryNumber(this.entryIndex);
	Player.playVideo();
}

/* go back to the last entry clip if there is one, otherwise skip ahead to the last one */
Player.playPreviousClip = function()
{	
	if(this.urlList.length == 1) return;
	if(this.entryIndex <= 0) this.entryIndex = this.urlList.length-1;
	else this.entryIndex = this.entryIndex-1;
	Player.stopVideo();
	alert(this.entryIndex);
	this.url = this.urlList[this.entryIndex];

	Main.setEntryNumber(this.entryIndex);
	Player.playVideo();
}

Player.setTotalTime = function()
{
    Display.setTotalTime(Player.plugin.GetDuration());
}

onServerError = function()
{
    Display.status("Server Error!");
}

OnNetworkDisconnected = function()
{
    Display.status("Network Error!");
}

stopPlayer = function()
{
    Player.stopVideo();
}

getBandwidth = function(bandwidth) { alert("getBandwidth " + bandwidth); }

onDecoderReady = function() { alert("onDecoderReady"); }

onRenderError = function() { alert("onRenderError"); }



setTotalBuffer = function(buffer) { alert("setTotalBuffer " + buffer); }

setCurBuffer = function(buffer) { alert("setCurBuffer " + buffer); }
