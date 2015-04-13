/*
	Data.js is used to capsule the currently loaded playlist and all of it's entries.
	This is based on the Samsung Tutorial http://www.samsungdforum.com/Guide/View/Developer_Documentation/Samsung_SmartTV_Developer_Documentation_2.5/JavaScript/Play_Audio_and_Video/Tutorial_Creating_a_Video_Application_With_HAS_%28HTTP_Adaptive_Streaming%29

*/

var Data =
{
    videoNames : [ ],
    videoURLs : [ ],
    videoDescriptions : [ ],
	videoDurations : [ ],
	videoEntryURLs : [[],[]],
	videoEntryTitles : [[],[]]
}

Data.setVideoNames = function(list)
{
    this.videoNames = list;
}

Data.setVideoURLs = function(list)
{
    this.videoURLs = list;
}

Data.setVideoDescriptions = function(list)
{
    this.videoDescriptions = list;
}

Data.setVideoDurations = function(list)
{
	this.videoDurations = list;
}

Data.setEntryURLs = function(list)
{
	this.videoEntryURLs = list;
}

Data.getEntryURLs = function(index)
{
	var url = this.videoEntryURLs[index];
	
	if (url)    // Check for undefined entry (outside of valid array)
    {
        return url;
    }
    else
    {
        return null;
    }
}

Data.getEntryTitles = function(index)
{
	var title = this.videoEntryTitles[index];
	
	if (title)    // Check for undefined entry (outside of valid array)
    {
        return title;
    }
    else
    {
        return null;
    }
}

Data.setEntryTitles = function(list)
{
	this.videoEntryTitles = list;
}

Data.getVideoURL = function(index)
{
    var url = this.videoURLs[index];
    
    if (url)    // Check for undefined entry (outside of valid array)
    {
        return url;
    }
    else
    {
        return null;
    }
}

Data.getVideoCount = function()
{
    return this.videoURLs.length;
}

Data.getVideoNames = function()
{
    return this.videoNames;
}

Data.getVideoDescription = function(index)
{
    var description = this.videoDescriptions[index];
    
    if (description)    // Check for undefined entry (outside of valid array)
    {
        return description;
    }
    else
    {
        return "No description";
    }
}

Data.getVideoDuration = function(index)
{
	return this.videoDurations;
}
