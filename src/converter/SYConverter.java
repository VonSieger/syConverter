package converter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;

import authorization.AccessController;
import dataTypes.Track;
import spotify.SpotifyPlaylist;
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
		List<String> parameters = Arrays.asList(args);
		if(parameters.contains("current") && parameters.contains("playlist")) {
			System.err.println("Error: Too many arguments");
			System.exit(1);
		}
		List<Track> tracks = new LinkedList<Track>();
		
		if(parameters.size() == 0 || (parameters.contains("current") && parameters.size() == 1)) {//standard execution || parameters equals current
			Track currentTrack = TitleFinder.getCurrentTrack();
			if(currentTrack.getArtists() == null || currentTrack.getTitle() == null) {
				System.err.println("No Track was found");
				System.exit(1);
			}
			tracks.add(currentTrack);
		}else if(parameters.get(0).contains("playlist") && parameters.size() == 2) {//first argument must be playlist && playlist id must be given
			SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist();
			tracks = spotifyPlaylist.getPlaylist(parameters.get(1));
			if(tracks == null)
				System.exit(1);
		}else {
			System.err.println("Error: Invalid arguments");
			System.exit(1);
		}
		//set url
		for(Track track : tracks) 
			track.setUrl(YTSearch.searchYouTube((track.getTitle() + " " + track.getArtists()[0])));
		
		//download tracks
		for(Track track : tracks){
			YTDownloader downloader = new YTDownloader(track);
			Thread download = new Thread(downloader);
			download.start();
			System.out.println(track.getUrl() + " is getting downloaded...");
			//wait for download to be finished
			while(download.isAlive());
		}
	}
}
