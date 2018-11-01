/**
 * 
 */
package dataTypes;

import java.util.List;

/**
 * @author boss
 *
 */
public interface PlaylistGetter {
	
	public final String listSeperator = ";";

	public List<Track> getPlaylist(String id);
}
