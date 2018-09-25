package dataTypes;

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
		this.title = title;
		this.artists = artists;
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
