package dataTypes;

import java.text.Normalizer;

/**
 * The track class provides the basic information about a track.
 * @author boss
 *
 */

public class Track {
	private String title;
	private String[] artists;
	private String youtubeURL;
	private String fileLocation;
	
	/**
	 * 
	 * @param title
	 * @param artists
	 */
	public Track(String title, String[] artists) {
		//initialize attributes and remove special characters
		this.title = Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		this.artists = artists;
		for(String artist : this.artists)
			artist = Normalizer.normalize(artist, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the artists
	 */
	public String[] getArtists() {
		return artists;
	}

	/**
	 * @return the youtubeURL
	 */
	public String getYoutubeURL() {
		return youtubeURL;
	}

	/**
	 * @param youtubeURL the youtubeURL to set
	 */
	public void setYoutubeURL(String URL) {
		this.youtubeURL = URL;
	}

	/**
	 * @return the fileLocation
	 */
	public String getFileLocation() {
		return fileLocation;
	}

	/**
	 * @param fileLocation the fileLocation to set
	 */
	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}
	
	
}
