/**
 *
 */
package directory;

import java.awt.geom.IllegalPathStateException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import dataTypes.PlaylistGetter;
import dataTypes.Track;

/**
 * @author boss
 *
 */
public class DirectoryManager implements PlaylistGetter {

	private File directory;

	public DirectoryManager(String directory) {
		File tmp = new File(directory);
		if (tmp.isDirectory())
			this.directory = tmp;
		else
			throw new IllegalPathStateException();
	}

	/**
         * @param id does not have any effect; null is valid as well
         * @return List of tracks stored in the directory defined in the constructor
         */
        @Override
        public List<Track> getPlaylist(String id) {
                File[] fileTracks = directory.listFiles(new MP3Filter());
                List<Track> tracks = new ArrayList(fileTracks.length);
                for(File file : fileTracks) {
                        try {
							MP3File mp3Track = new MP3File(file);
							ID3v24Tag mp3Tag = (ID3v24Tag)mp3Track.getID3v2TagAsv24();
							tracks.add(new Track(mp3Tag.getFirst(FieldKey.TITLE), convertToArtistArray(mp3Tag.getFirst(FieldKey.ARTIST))));
							tracks.get(tracks.size() -1).setFileLocation(file.getAbsolutePath());
						}  catch(ReadOnlyFileException | TagException | InvalidAudioFrameException e) {
						}catch (IOException  e) {
							//TODO Improve exception handling
							e.printStackTrace();
							return null;
						}
                }
                return tracks;
        }
        
     private String[] convertToArtistArray(String artistsString) {
    	 List<String> artists = new LinkedList();
    	 while(artistsString.contains(";")) {
    		 artists.add(artistsString.substring(0, artistsString.indexOf(listSeperator)));
    		 artistsString = artistsString.substring(artistsString.indexOf(listSeperator) +1);
    	 }
    	 artists.add(artistsString);
    	 return artists.toArray(new String[artists.size()]);
     }

	private class MP3Filter implements FileFilter {
		public boolean accept(File pathname) {
			if (pathname.getName().endsWith(".mp3") || pathname.getName().endsWith(".mp3'"))
				return true;
			return false;
		}
	}
}