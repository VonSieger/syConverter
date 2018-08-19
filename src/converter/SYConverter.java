package converter;
import java.io.IOException;

import dataTypes.Track;
import spotify.TitleFinder;
import youtube.YTDownloader;
import youtube.YTSearch;
/**
 * SYConverter takes the currently played song in spotify, searches it title and artist on YouTube and downloads it afterwards from YouTube.
 * @author boss
 *
 */
public class SYConverter {

	public static void main(String[] args) throws IOException {
		Track currentTrack = TitleFinder.getCurrentTrack();
		if(currentTrack.getArtist() == null || currentTrack.getTitle() == null) {
			System.out.println("No Track was found");
			System.exit(1);
		}
		currentTrack.setUrl(YTSearch.searchYouTube(currentTrack.getTitle() + " " + currentTrack.getArtist()));
		YTDownloader downloader = new YTDownloader(currentTrack);
		Thread download = new Thread(downloader);
		download.start();
		System.out.println(currentTrack.getUrl() + " is getting downloaded...");
		//TODO print exit status
	}

}
