/*
	Audio.js is used to manage the volume of the video
	This is based on the Samsung Tutorial http://www.samsungdforum.com/Guide/View/Developer_Documentation/Samsung_SmartTV_Developer_Documentation_2.5/JavaScript/Play_Audio_and_Video/Tutorial_Creating_a_Video_Application_With_HAS_%28HTTP_Adaptive_Streaming%29

*/

var Audio =
{
    plugin : null
}

Audio.init = function()
{	
    var success = true;
    this.plugin = document.getElementById("pluginAudio");
    if (!this.plugin)
    {
        success = false;
    }
    return success;
}

/* set current volume to 'delta' */
Audio.setRelativeVolume = function(delta)
{
    this.plugin.SetVolumeWithKey(delta);
    Display.setVolume( this.getVolume() );

}

Audio.getVolume = function()
{
    return this.plugin.GetVolume();
}
