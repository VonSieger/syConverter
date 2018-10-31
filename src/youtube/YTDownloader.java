package youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import dataTypes.Track;

/**
 * This class downloads a track from a given URL in mp3-format.
 * Dependency: youtube-dl(https://github.com/rg3/youtube-dl)
 * @author boss
 *
 */

public class YTDownloader implements Runnable{
	
	private Track musicTrack;
	
	/**
	 * Standard constructor
	 * @param musicTrack, providing the URL for later download
	 */
	public YTDownloader(Track musicTrack) {
		this.musicTrack = musicTrack;
	}
	
	/**
	 * Downloads from the given (Youtube-)URL an mp3 file using youtube-dl(https://github.com/rg3/youtube-dl)
	 * The file name is following the schema [Title]-[Artist].mp3.
	 * @return process of "youtube-dl", or null if no URL is provided
	 */
	private  Process download() {
		if(musicTrack.getUrl() == null)
			return null;
		try {
			Process youtubeDL = new ProcessBuilder("youtube-dl",
					"-o" + musicTrack.getTitle() + "-" + musicTrack.getArtists()[0] + ".%(ext)s'",
					"-x", "--audio-format", "mp3",
					musicTrack.getUrl()).start();
			youtubeDL.waitFor();
			
			//create ID3 v2.4 Tag for downloaded mp3 file
			MP3File downloadMP3 = new MP3File(System.getProperty("user.dir") + "/" + musicTrack.getTitle() + 
					"-" + musicTrack.getArtists()[0] + ".mp3");
			ID3v24Tag v24Tag = (ID3v24Tag)downloadMP3.getID3v2TagAsv24();
			
			//create ";"-seperated list of artist and store it into ID3 Tag
			String artists = "";
			for(String artist : musicTrack.getArtists())
				artists += artist + ";";
			artists = artists.substring(0, artists.length() -1);
			v24Tag.addField(FieldKey.ARTIST, artists);
			
			v24Tag.addField(FieldKey.TITLE, musicTrack.getTitle());
			
			downloadMP3.commit();
			
			return youtubeDL;
		} catch (IOException e) {
			e.printStackTrace();
		}catch(TagException| ReadOnlyFileException | InvalidAudioFrameException | CannotWriteException e) {
			//TODO improve exception handling
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
		return null;
	}

	@Override
	public void run() {
		//creating a "youtube-dl"-Process
		Process download = download();
		if(download == null)
			return;
		//Printing out standard output stream of youtube-dl.
		InputStream is = download.getInputStream();
		BufferedReader rIs = new BufferedReader(new InputStreamReader(is));
		String output = "";
		
		try {
			download.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			while(rIs.ready()) {
				output += rIs.readLine() + "\n";
			}
			System.out.println(output);
			rIs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Printing out error stream of youtube-dl
		is = download.getErrorStream();
		rIs = new BufferedReader(new InputStreamReader(is));
		output = "";
		
		try {
			while(rIs.ready()) {
				output += rIs.readLine() + "\n";
			}
			System.out.println(output);
			rIs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
