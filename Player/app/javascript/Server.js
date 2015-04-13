/*
	Server.js is where the playlists are stored and read in form of a structured XML-file
	This is based on the Samsung Tutorial http://www.samsungdforum.com/Guide/View/Developer_Documentation/Samsung_SmartTV_Developer_Documentation_2.5/JavaScript/Play_Audio_and_Video/Tutorial_Creating_a_Video_Application_With_HAS_%28HTTP_Adaptive_Streaming%29
*/

var Server =
{
    /* Callback function to be set by client */
    dataReceivedCallback : null,
    
    XHRObj : null,
    url : "XML/videoList.xml", //where the xml is stored
	filename: "videoList.xml"
}

Server.init = function()
{
    var success = true;

    if (this.XHRObj)
    {
        this.XHRObj.destroy();  // Save memory
        this.XHRObj = null;
    }
    
    return success;
}

/* create a XHR Object to to read in the xml playlist-file */
Server.fetchVideoList = function()
{
    if (this.XHRObj == null)
    {
        this.XHRObj = new XMLHttpRequest();
    }
    
    if (this.XHRObj)
    {
        this.XHRObj.onreadystatechange = function()
            {
                if (Server.XHRObj.readyState == 4)
                {
                    Server.createVideoList();
                }
            }
            
        this.XHRObj.open("GET", this.url, true);
        this.XHRObj.send(null);
     }
    else
    {
        alert("Failed to create XHR");
    }
}

/* Loads the xml file, extracts all the information needed and capsules it in a Data-Element (-> Data.js) */
Server.createVideoList = function()
{
    if (this.XHRObj.status != 200)
    {
        Display.status("XML Server Error " + this.XHRObj.status);
    }
    else
    {
        var xmlElement = this.XHRObj.responseXML.documentElement;
        
        if (!xmlElement)
        {
            alert("Failed to get valid XML");
        }
        else
        {		
			xmlElement = openFileAndParseXML(this.filename);
			
			//if no file is stored, none exists -> dont load anything
			if(xmlElement == null) return;
            
			// Parse RSS
            // Get all "item" elements
            var items = xmlElement.getElementsByTagName("item");
            
			//init
            var videoNames = [ ];
            var videoURLs = [ ];
            var videoDurations = [ ];
            var entryTitles = [[],[]];
			var entryURLs = [[],[]];
			
			//for every 'item' element (the overall shows that the user has selected)
            for (var index = 0; index < items.length; index++)
            {
				//read and save title, duration and get entry-elements
                var titleElement = items[index].getElementsByTagName("title")[0];
                var durationElement = items[index].getElementsByTagName("duration")[0];
				var entries = items[index].getElementsByTagName('entry');

				
				var entryTitleElement = [ ];
				var entryURLElement = [ ];
				
				//now iterate through every entry element (specific clip in a show) inside of an item
				for (var j = 0; j < entries.length; j++)
				{
					//here the clip titles and most importantly links are stored
					entryTitleElement[j] = entries[j].getElementsByTagName("title")[0].firstChild.data;
					
					//if it's the first one, it's the main url (starting point)
					if(j == 0)	var linkElement = entries[j].getElementsByTagName("link")[0];
					
					entryURLElement[j] = entries[j].getElementsByTagName("link")[0].firstChild.data;
					
				}
                
				//if all necessary elements have been found, save them in temporary arrays
                if (titleElement && durationElement && linkElement)
                {
                    videoNames[index] = titleElement.firstChild.data;
                    videoURLs[index] = linkElement.firstChild.data;
                    videoDurations[index] = durationElement.firstChild.data;
					if(entryTitleElement)
					{
						entryTitles[index] = entryTitleElement;
						entryURLs[index] = entryURLElement;
					}
                }
            }
			
			//save all elements to Data
            Data.setVideoNames(videoNames);
            Data.setVideoURLs(videoURLs);
            Data.setVideoDurations(videoDurations);
			Data.setEntryURLs(entryURLs);
			Data.setEntryTitles(entryTitles);
            
            if (this.dataReceivedCallback)
            {
                this.dataReceivedCallback();    /* Notify all data is received and stored */
            }
        }
    }
}

/* Open file from the database and parse String to XML. */
function openFileAndParseXML(name) {
	alert("Reading Playlist File from database and parse to XML");
	
	var file = fileSystem.openCommonFile(curWidget.id + name, 'r');
	if(file == null) return null;
	alert(file.readAll());
	var string = file.readAll();
	fileSystem.closeCommonFile(file);
	
	if (window.ActiveXObject){
                  var doc=new ActiveXObject('Microsoft.XMLDOM');
                  doc.async='false';
                  doc.loadXML(string);
                } else {
                  var parser=new DOMParser();
                  var doc=parser.parseFromString(string,'text/xml');
	}
	return doc;
}