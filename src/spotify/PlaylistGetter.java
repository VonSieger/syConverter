/**
 * 
 */
package spotify;

import java.util.List;

import dataTypes.Track;

/**
 * @author boss
 *
 */
public interface PlaylistGetter {

	public List<Track> getPlaylist(String id);
}
