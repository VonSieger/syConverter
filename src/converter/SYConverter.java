package converter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
		List<Track> tracks = new LinkedList<Track>();
		
		//go through all parameters
		for(Iterator<String> it = parameters.iterator(); it.hasNext();) {
			String current = it.next();
			//download current track
			if(current.contains("current")) {
				Track currentTrack = TitleFinder.getCurrentTrack();
				if(currentTrack.getArtists() == null || currentTrack.getTitle() == null) {
					System.err.println("No Track was found");
					System.exit(1);
				}
				tracks.add(currentTrack);
			}else if(current.contains("playlist") && it.hasNext()) {
				SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist();
				//get playlist with <id>
				String uri = it.next();
				if(uri.matches("spotify:user:.+:playlist:.+"))
					tracks = spotifyPlaylist.getPlaylist(uri.split(":")[3]);
				else {
					System.err.println("Wrong Spotify URI: " + uri + "; NOTE Only playlists are valid");
					System.exit(1);
				}
				if(tracks == null)
					System.exit(1);
			}else if(current.contains("help")){
				System.out.println("Usage: syConverter [OPTIONS]\n"
						+ "Download one or more songs from YouTube specified by Spotify, look into options for details.\n"
						+ "Downloaded songs will be stored as a .mp3 files in \"<Title>-<Artist>.mp3\" format.\n"
						+ "Options:\n"
						+ "\t--help : Show this help and exit\n"
						+ "\t--current : Dowload the current song. The current song is the song returned by playerctl(dbus).\n"
						+ "\t--playlist <playlist-uri> : Download the specified playlist from youtube.\n"
						+ "\t\t<playlist-uri>:\n"
						+ "\t\tplaylist to download->...->Share->Copy Spotify URI");
				System.exit(0);
			}else {
				System.err.println("Error: Invalid argument: " + current + 
						"\nTry 'syController --help' for more information");
				System.exit(1);
			}
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
