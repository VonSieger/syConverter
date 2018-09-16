/**
 * 
 */
package spotify;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author boss
 *
 */
public class URIBuilder {
	
	public static URI generateURI(String endpoint, Set<Map.Entry<String, String>> parameters) throws URISyntaxException {
		
		//creating query
		String query = "";
		for(Iterator<Map.Entry<String, String>> it = parameters.iterator(); it.hasNext();) {
			Map.Entry<String, String> current = it.next();
			query += "?" + current.getKey() + "=" + current.getValue();
		}
		
		//join endpoint with query
		if(!endpoint.startsWith("/"))
			endpoint = "/" + endpoint;
		if(!endpoint.endsWith("/"))
			endpoint += "/";
		return new URI("https://accounts.spotify.com" + endpoint + query);
  	}
}
