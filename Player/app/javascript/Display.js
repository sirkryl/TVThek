/*
	Display.js essentially decides what is displayed on the screen.
	This is based on the Samsung Tutorial http://www.samsungdforum.com/Guide/View/Developer_Documentation/Samsung_SmartTV_Developer_Documentation_2.5/JavaScript/Play_Audio_and_Video/Tutorial_Creating_a_Video_Application_With_HAS_%28HTTP_Adaptive_Streaming%29

*/

var Display =
{
    statusDiv : null,
    FIRSTIDX : 0,
    LASTIDX : 4,
    currentWindow : 0,

    SELECTOR : 0,
    LIST : 1,
    
    videoList : new Array()
}

Display.init = function()
{
    var success = true;
    
    this.statusDiv = document.getElementById("status");

    if (!this.statusDiv)
    {
        success = false;
    }
    
    return success;
}

/* set the total duration of a clip */
Display.setTotalTime = function(total)
{
    this.totalTime = total;
}

/* change time to format "hh:mm:ss" and set the progressbar accordingly */
Display.setTime = function(time)
{
    var timePercent = (100 * time) / this.totalTime;
    var timeElement = document.getElementById("timeInfo");
    var timeHTML = "";
    var timeHour = 0; var timeMinute = 0; var timeSecond = 0;
    var totalTimeHour = 0; var totalTimeMinute = 0; var totalTimesecond = 0;
    
    document.getElementById("progressBar").style.width = timePercent + "%";
    
    if(Player.state == Player.PLAYING)
    {
        totalTimeHour = Math.floor(this.totalTime/3600000);
        timeHour = Math.floor(time/3600000);
        
        totalTimeMinute = Math.floor((this.totalTime%3600000)/60000);
        timeMinute = Math.floor((time%3600000)/60000);
        
        totalTimeSecond = Math.floor((this.totalTime%60000)/1000);
        timeSecond = Math.floor((time%60000)/1000);
        
        timeHTML = timeHour + ":";
        
        if(timeMinute == 0)
            timeHTML += "00:";
        else if(timeMinute <10)
            timeHTML += "0" + timeMinute + ":";
        else
            timeHTML += timeMinute + ":";
            
        if(timeSecond == 0)
            timeHTML += "00/";
        else if(timeSecond <10)
            timeHTML += "0" + timeSecond + "/";
        else
            timeHTML += timeSecond + "/";
            
        timeHTML += totalTimeHour + ":";
        
        if(totalTimeMinute == 0)
            timeHTML += "00:";
        else if(totalTimeMinute <10)
            timeHTML += "0" + totalTimeMinute + ":";
        else
            timeHTML += totalTimeMinute +":";
            
        if(totalTimeSecond == 0)
            timeHTML += "00";
        else if(totalTimeSecond <10)
            timeHTML += "0" + totalTimeSecond;
        else
            timeHTML += totalTimeSecond;
    }
    else
        timeHTML = "0:00:00/0:00:00";     
    
    widgetAPI.putInnerHTML(timeElement, timeHTML);
    
}

/* displays the current status of the player */
Display.status = function(status)
{
    widgetAPI.putInnerHTML(this.statusDiv, status);
}

/* change the volume slider if it is changed */
Display.setVolume = function(level)
{
    document.getElementById("volumeBar").style.width = level + "%";
    
    var volumeElement = document.getElementById("volumeInfo");

    widgetAPI.putInnerHTML(volumeElement, "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + Audio.getVolume());
}

/* format and display all clips in a playlist */
Display.setVideoList = function(nameList)
{
    var listHTML = "";
    
	j = 0;
	while (document.getElementById("video"+j) != null)
	{
		widgetAPI.putInnerHTML(document.getElementById("video"+j), "");
		j++
	}
	
    var i=0;
    for (var name in nameList)
    {
        this.videoList[i] = document.getElementById("video"+i);
        listHTML = nameList[name] ;
        widgetAPI.putInnerHTML(this.videoList[i], listHTML);
        i++;
    }
    this.videoList[this.FIRSTIDX].style.fontWeight="bold";
    if(i>5)
    {
        document.getElementById("next").style.opacity = '1.0';
        document.getElementById("previous").style.opacity = '1.0';
    }
	
    listHTML = "1 / " + i;
    widgetAPI.putInnerHTML(document.getElementById("videoCount"), listHTML);
	
}

/* displays the currently selected clip and changes it font weight to 'bold' */
Display.setVideoListPosition = function(position, move)
{    
    var listHTML = "";
    
    listHTML = (position + 1) + " / " + Data.getVideoCount();
	
    widgetAPI.putInnerHTML(document.getElementById("videoCount"), listHTML);
    
    if(Data.getVideoCount() < 5)
    {
        for (var i = 0; i < Data.getVideoCount(); i++)
        {
            if(i == position)
                this.videoList[i].style.fontWeight="bold";
            else
                this.videoList[i].style.fontWeight="normal";
        }
    }
    else if((this.currentWindow!=this.LASTIDX && move==Main.DOWN) || (this.currentWindow!=this.FIRSTIDX && move==Main.UP))
    {
        if(move == Main.DOWN)
            this.currentWindow ++;
        else
            this.currentWindow --;
            
        for (var i = 0; i <= this.LASTIDX; i++)
        {
            if(i == this.currentWindow)
                this.videoList[i].style.fontWeight="bold";
            else
                this.videoList[i].style.fontWeight="normal";
        }
    }
    else if(this.currentWindow == this.LASTIDX && move == Main.DOWN)
    {
        if(position == this.FIRSTIDX)
        {
            this.currentWindow = this.FIRSTIDX;
            
            for(i = 0; i <= this.LASTIDX; i++)
            {
                listHTML = Data.videoNames[i] ;
                widgetAPI.putInnerHTML(this.videoList[i], listHTML);
				
                
                if(i == this.currentWindow)
                    this.videoList[i].style.fontWeight="bold";
                else
                    this.videoList[i].style.fontWeight="normal";
            }
        }
        else
        {            
            for(i = 0; i <= this.LASTIDX; i++)
            {
                listHTML = Data.videoNames[i + position - this.currentWindow] ;
                widgetAPI.putInnerHTML(this.videoList[i], listHTML);
				
				
            }
        }
    }
    else if(this.currentWindow == this.FIRSTIDX && move == Main.UP)
    {
        if(position == Data.getVideoCount()-1)
        {
            this.currentWindow = this.LASTIDX;
            
            for(i = 0; i <= this.LASTIDX; i++)
            {
                listHTML = Data.videoNames[i + position - this.currentWindow] ;
                
                if(i == this.currentWindow)
                    this.videoList[i].style.fontWeight="bold";
                else
                    this.videoList[i].style.fontWeight="normal";
            }
        }
        else
        {            
            for(i = 0; i <= this.LASTIDX; i++)
            {
                listHTML = Data.videoNames[i + position] ;
                widgetAPI.putInnerHTML(this.videoList[i], listHTML);
				
            }
        }
    }
}

/* display the entries of a clip in a (more or less) well-formated manner */
Display.setDescription = function(description,number)
{
    var descriptionElement = document.getElementById("description");
	var descriptionString = "";
	for(i = 0; i < description.length; i++)
	{
		if(i == number)	descriptionString += "<span style='font-weight:bold'>"+description[i]+"</span><br>";
		else descriptionString += description[i] + "<br>";
	}
	widgetAPI.putInnerHTML(descriptionElement, descriptionString);
}

Display.hide = function()
{
    document.getElementById("main").style.display="none";
}

Display.show = function()
{
    document.getElementById("main").style.display="block";
}

