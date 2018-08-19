package spotify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import dataTypes.Track;

/**
 * Dependency: playerctl(https://github.com/acrisci/playerctl)
 * @author boss
 *
 */
public class TitleFinder {
	
	/**
	 * Using playerctl, the title and artist of the currently playing song are returned
	 * @return the current Track
	 */
	public static Track getCurrentTrack(){
		try {
			Process playerctlTitle = new ProcessBuilder("playerctl", "metadata", "title").start();
			Process playerctlArtist = new ProcessBuilder("playerctl", "metadata", "artist").start();
			
			InputStream titleStream = playerctlTitle.getInputStream();
			InputStream artistStream = playerctlArtist.getInputStream();
			
			playerctlTitle.waitFor(500, TimeUnit.MILLISECONDS);
			playerctlArtist.waitFor(500, TimeUnit.MILLISECONDS);
			
			BufferedReader titleReader = new BufferedReader(new InputStreamReader(titleStream));
			BufferedReader artistReader = new BufferedReader(new InputStreamReader(artistStream));
			
			return new Track(titleReader.readLine(), artistReader.readLine());
		}catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
