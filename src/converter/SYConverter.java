package converter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
		
		Queue<Track> tracks = new LinkedList<Track>();
		
		if(args.length == 0 || args[0].contains("current")) {
			Track currentTrack = TitleFinder.getCurrentTrack();
			if(currentTrack.getArtist() == null || currentTrack.getTitle() == null) {
				System.out.println("No Track was found");
				System.exit(1);
			}
			currentTrack.setUrl(YTSearch.searchYouTube(currentTrack.getTitle() + " " + currentTrack.getArtist()));
			tracks.add(currentTrack);
		}else if(args[0].contains("playlist") && args.length == 2) {
			
		}
		
		Queue<Thread> downloads = new ArrayDeque<Thread>(tracks.size());
		
		while(!tracks.isEmpty()) {
			YTDownloader downloader = new YTDownloader(tracks.peek());
			Thread download = new Thread(downloader);
			download.start();
			System.out.println(tracks.poll().getUrl() + " is getting downloaded...");
			downloads.add(download);
		}
		
		//TODO print exit status
	}

}
