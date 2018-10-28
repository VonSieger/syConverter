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
	private String url;
	
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
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
