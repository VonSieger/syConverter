package youtube;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.*;
import com.google.api.services.youtube.YouTube.Search.List;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

/**
 * YTSearch searches with the Google Data API in YouTube for the song name.
 * Google Data API: https://developers.google.com/youtube/v3/getting-started
 * @author boss
 *
 */

public class YTSearch {
	
	//only one top result
	private static final int MAX_ITEMS = 1;
	//create your own API Key: https://developers.google.com/youtube/v3/getting-started
	//No OAuth needed
	private static final String API_KEY = "yourKey";
	
	public YTSearch() {
		
	}
	
	/**
	 * The method searches YouTube with the help of the Google Data API  for the regex.
	 * @param regex to search for on Youtube
	 * @return URL matching the regex
	 */
	public static String searchYouTube(String regex) {
		YouTube youtube  = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
           	public void initialize(HttpRequest request) throws IOException {
           	}
		}).setApplicationName("syConverter").build();
		
		try {
			YouTube.Search.List search = youtube.search().list("id,snippet");
			//API Key
			search.setKey(API_KEY);
			//Search term
			search.setQ(regex);
			//only search for videos
			search.setType("video");
			//Max Results
			search.setMaxResults((long) MAX_ITEMS);
			
			//execute search and return full URL
			SearchListResponse searchResult = search.execute();
			java.util.List<SearchResult> searchResultList = searchResult.getItems();
			SearchResult first = searchResultList.get(0);
			ResourceId firstResultID = first.getId();
			return "https://www.youtube.com/watch?v=" + firstResultID.getVideoId();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
