package converter;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import dataTypes.PlaylistGetter;
import dataTypes.Track;
import directory.DirectoryManager;
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
	
	//boolean to remember, to log or not; default is off
	boolean log = false;
	
	public SYConverter() {
		
	}
	
	private void download(String[] args) {
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
				String url = it.next();
				if(url.matches("spotify:user:.+:playlist:.+"))
					tracks = spotifyPlaylist.getPlaylist(url.split(":")[4]);
				else {
					System.err.println("Wrong Spotify URI: " + url + "; NOTE Only playlists are valid");
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
						+ "\t\tplaylist to download->...->Share->Copy Spotify URI"
						+ "\t-v/--verbose show verbose output");
				System.exit(0);
			}else if(current.equals("--verbose") || current.equals("-v")) 
				log = true;
			else {
				System.err.println("Error: Invalid argument: " + current + 
						"\nTry 'syController --help' for more information");
				System.exit(1);
			}
		}
		
		if(!log)
			Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		
		List<Track> localTracks = new DirectoryManager(System.getProperty("user.dir")).getPlaylist(null);
		tracks = sortOut(tracks, localTracks);
		
		//set url
		for(Track track : tracks) 
			track.setYoutubeURL(YTSearch.searchYouTube((track.getTitle() + " " + track.getArtists()[0])));
		
		//download tracks
		for(int i = 0; i < tracks.size(); i++){
			Track track = tracks.get(i);
			YTDownloader downloader = new YTDownloader(track);
			Thread download = new Thread(downloader);
			download.start();
			System.out.println(track.getYoutubeURL() + " is getting downloaded...");
			//wait for download to be finished
			try {
				download.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				//create ID3 v2.4 Tag for downloaded mp3 file
				Track currentTrack = downloader.getMusicTrack();
				MP3File downloadMP3 = new MP3File(currentTrack.getFileLocation().toString());
				ID3v24Tag v24Tag = (ID3v24Tag)downloadMP3.getID3v2TagAsv24();
				
				//create ";"-seperated list of artist and store it into ID3 Tag
				String artists = "";
				for(String artist : currentTrack.getArtists())
					artists += artist + PlaylistGetter.listSeperator;
				artists = artists.substring(0, artists.length() -1);
				v24Tag.addField(FieldKey.ARTIST, artists);
				
				v24Tag.addField(FieldKey.TITLE, currentTrack.getTitle());
				
				downloadMP3.commit();
			}catch(ReadOnlyFileException e) {
				e.printStackTrace();
				System.err.println("Cannot write to " + downloader.getMusicTrack().getFileLocation() + ". Only having read "
						+ "permissions");
			}catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println(i +1 + "/" + tracks.size() + " downloaded");
		}
	}
	
	/**
	 * Compares two playlists(playlist fetched from spotify and tracks from local files).
	 * Tracks occurring in both, the spotify playlist and the local files, are going to be forgotton, because there is no need
	 * of removing them from the file system or to download them.
	 * Tracks occurring only in the spotify playlist, but not in the local tracks will be returned as "toBeDownloaded".
	 * Tracks occurring only in the localTracks but not in the spotify playlist are going to be removed from the file system.
	 * @param spotifyPlaylist
	 * @param localTracks
	 * @return List of tracks to be downloaded
	 */
	private List<Track>sortOut(List<Track> spotifyPlaylist, List<Track>localTracks){
		//create HashMap out of localTracks
		//key: <title>-<artist1artist2...artistN>
		HashMap<String, Track> toBeRemoved = new HashMap<String, Track>();
		for(Track cur : localTracks) 
			toBeRemoved.put(createKey(cur), cur);
		
		//compare spotify playlist to local tracks
		for(Iterator<Track> it = spotifyPlaylist.iterator(); it.hasNext();) {
			Track curSpotify = it.next();
			Track curLocal = toBeRemoved.get(createKey(curSpotify));
			//no need to download or remove
			if(curLocal != null) {
				it.remove();
				toBeRemoved.remove(createKey(curSpotify));
			}
		}
		
		//remove local files, which are not in spotify playlist
		for(Map.Entry<String, Track> curLocal : toBeRemoved.entrySet()) {
			File curFile = new File(curLocal.getValue().getFileLocation());
			try {
				curFile.delete();
				System.out.println(curFile.getAbsolutePath() + " was removed.");
			}catch(SecurityException e) {
				System.err.println(e.getMessage() + "\nCannot remove tracks not included in your spotify playlist.");
			}
		}
		return spotifyPlaylist;
	}

	/**
	 * 
	 * @param artists
	 * @return key created from Track.title and Track.artists
	 */
	private String createKey(Track track) {
		String artistsString = "";
		for(String artist : track.getArtists())
			artistsString += artist;
		return track.getTitle() + artistsString.trim();
	}
	
	public static void main(String[] args){
		new SYConverter().download(args);
	}
}
