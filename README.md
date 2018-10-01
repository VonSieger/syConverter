# syConverter
Tool for downloading current spotify song from YouTube

## Dependencies
- [playerctl](https://github.com/acrisci/playerctl)
- [youtube-dl](https://github.com/rg3/youtube-dl)
  - [ffmpeg](https://www.ffmpeg.org/) OR [avconv](https://libav.org/)
- [YouTube Data API](https://developers.google.com/youtube/v3/): 
  I used Maven to include the API. For more details look into the pom.xml
  A simple API Key is enough, OAuth is not needed. For further instructions look into [Getting Started Guid](https://developers.google.com/youtube/v3/getting-started)
- [Spotify Web API](https://developer.spotify.com/documentation/web-api/):
  Getting key: https://developer.spotify.com/dashboard/
  Parsing Json-Response with [json-simple](https://github.com/fangyidong/json-simple). Hot to import it is found on this [site](https://code.google.com/archive/p/json-simple/)
