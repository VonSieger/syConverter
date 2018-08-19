package dataTypes;

/**
 * The track class provides the basic information about a track.
 * @author boss
 *
 */

public class Track {
	private String title;
	private String artist;
	private String url;
	
	/**
	 * 
	 * @param title
	 * @param artist
	 */
	public Track(String title, String artist) {
		this.title = title;
		this.artist = artist;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the artist
	 */
	public String getArtist() {
		return artist;
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
