/**
 * 
 */
package spotify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpConnection;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*; 

import com.google.api.client.json.jackson2.JacksonFactory;

import authorization.AccessController;
import dataTypes.Track;

/**
 * @author boss
 *
 */
public class SpotifyPlaylist implements PlaylistGetter {

	private static String[] scopes = { "user-library-modify" };

	private HttpClient client;

	// Parsing Json-response
	private JacksonFactory jacksonFactory;

	// provides access token
	private AccessController accessController;

	// Base URL
	private final static String BASE_URL = "https://api.spotify.com";
	// Endpoint
	private final static String PLAYLIST_ENDPOINT = BASE_URL + "/v1/playlists/<id>/tracks";

	public SpotifyPlaylist() {
		accessController = new AccessController(scopes);
		jacksonFactory = new JacksonFactory();
		client = HttpClientBuilder.create().build();
	}

	@Override
	public List<Track> getPlaylist(String id) {
		List<Track> playlist = new LinkedList<Track>();
		
		NameValuePair[] query = {new BasicNameValuePair("fields", "items(track(name%2Cartists(name)))%2Cnext")};
		NameValuePair[] headers = {new BasicNameValuePair("Accept", "application/json"),
				new BasicNameValuePair("Content-Type", "application/json"),
				new BasicNameValuePair("Authorization", "Bearer " + accessController.getAccessToken())
		};
		JSONObject playlistJson = sendGetRequest(PLAYLIST_ENDPOINT.replace("<id>", id), query, headers);
		if(playlistJson == null)
			return null;
		String next = "null";
		do {
			JSONArray items = (JSONArray) playlistJson.get("items");
			for(Iterator<JSONObject> itemsField = items.iterator(); itemsField.hasNext(); ) {
				//get track name and artist name
				JSONObject track = (JSONObject) itemsField.next().get("track");
				//artist's names
				JSONArray artists = (JSONArray) track.get("artists");
				List<String> artistsNames = new LinkedList<String>();
				for(Iterator<JSONObject> artistsField = artists.iterator(); artistsField.hasNext();) 
					artistsNames.add((String) artistsField.next().get("name")); 
				//track name
				String trackName = (String) track.get("name");
				//store into playlist as a new Track
				playlist.add(new Track(trackName, artistsNames.toArray(new String[artistsNames.size()])));
			}
			//get next part of playlist
			next = (String) playlistJson.get("next");
			if(next != null) {
				headers[2] = new BasicNameValuePair("Authorization", "Bearer " + accessController.getAccessToken());
				playlistJson = sendGetRequest(next, headers);
			}
		}while(next != null);
		return playlist;
	}

	private JSONObject sendGetRequest(String url, NameValuePair[] headers) {
		try {
			// establish connection
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			// add headers
			for (NameValuePair header : headers)
				con.setRequestProperty(header.getName(), header.getValue());
			//handle errors
			if(con.getResponseCode() != 200) {
				//printing out raw json containing error message
				System.out.println("HTTP Response Code: " + con.getResponseCode());
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
				while(br.ready()) {
					System.out.println(br.readLine());
				}
				return null;
			}
			// create JsonObject representing playlist and return it
			return(JSONObject) new JSONParser().parse(new InputStreamReader(con.getInputStream()));
		} catch (IOException  | ParseException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private JSONObject sendGetRequest(String url, NameValuePair[] query, NameValuePair[] headers) {
		// add query parameters to url
		url += "?";
		for (NameValuePair pair : query)
			url += pair.getName() + "=" + pair.getValue() + "&";
		// remove last "&"
		url = url.substring(0, url.length() - 1);
		return sendGetRequest(url, headers);
	}
}
