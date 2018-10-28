/**
 * 
 */
package authorization;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * Class providing access token from Spotify Web API according to the authorization code flow from OAuth2 protocol.
 * Guide: https://developer.spotify.com/documentation/general/guides/authorization-guide/
 * @author boss
 *
 */
public class AccessController implements AccessTokenHandler{

	//manage user credential
	private AuthorizationCodeFlow userCredential;
	//Parsing JSON
	JacksonFactory jacksonFactory;
	
	/*
	 * 
	 */
	private final String CLIENT_ID = "yourkey";
	private final String CLIENT_SECRET = "yourkey";
	
	//base URL for all endpoints
	private final String BASE_URL = "https://accounts.spotify.com";
	//used to get the code
	private final String AUTHORIZATION_ENDPOINT = BASE_URL + "/authorize";
	//final URL to send to spotify
	private String authorizationURL;
	//URL used to get the access token
	private final String ACCESSTOKEN_ENDPOINT = BASE_URL + "/api/token";
	//complete access token request(with code)
	private AuthorizationCodeTokenRequest accessTokenRequest;
	//complete access token request(with refresh token)
	private RefreshTokenRequest refreshTokenRequest;
	
	private String accessToken;
	//expire time in milliseconds
	private long expireTime = 0;
	//scopes; cannot be changed
	private final String[] scopes;
	
	public AccessController(String[] scopes) {
		this.scopes = scopes;
		jacksonFactory = new JacksonFactory();
		userCredential = new AuthorizationCodeFlow(BearerToken.authorizationHeaderAccessMethod(),
																						new NetHttpTransport(),
																						jacksonFactory,
																						new GenericUrl(ACCESSTOKEN_ENDPOINT),
																						new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
																						CLIENT_ID,
																						AUTHORIZATION_ENDPOINT);
		authorizationURL = AUTHORIZATION_ENDPOINT + "/?client_id=" + CLIENT_ID +
				"&redirect_uri=" + SpotifyCallbackServer.getCallbackCodeURI() +
				"&response_type=code";
		if(scopes.length > 0) {
			authorizationURL += "&scope=";
			for(int i = 0; i < scopes.length -1; i++) {
				authorizationURL += scopes[i] + "%20";
			}
			authorizationURL += scopes[scopes.length -1];
		}
		//initialize access token request (with empty code)
		accessTokenRequest = userCredential.newTokenRequest("");
		accessTokenRequest.setRedirectUri(SpotifyCallbackServer.getCallbackCodeURI().toString());
		accessTokenRequest.setGrantType("authorization_code");
		//initialize access token request (wity empty refresh token)
		refreshTokenRequest = new RefreshTokenRequest(userCredential.getTransport(),
				userCredential.getJsonFactory(),
				new GenericUrl(userCredential.getTokenServerEncodedUrl()),
				"");
		refreshTokenRequest.setGrantType("refresh_token");
		refreshTokenRequest.setClientAuthentication(userCredential.getClientAuthentication());
	}
	
	@Override
	/**
	 * Returns a valid access token from <BASE_URL> or null if an error occurred.
	 * @return String valid access token or null
	 * @throws SecurityException by opening the browser, User denies access, error from server for several reasons
	 */
	public String getAccessToken() {
		TokenResponse accessTokenResponse = null;
		//no refresh token available
		if(expireTime == 0) {
			SpotifyCallbackServer server = new SpotifyCallbackServer();
			server.start();
			//get user authorization (get code)
			System.out.println("Need auhtorization for these scopes:");
			for(String scope : scopes) System.out.println(scope);
			System.out.println("Opening browser...");
			//open authorization page in browser
			try {
				try {
					//Windows
					Desktop.getDesktop().browse(new URI(authorizationURL));
				}catch(UnsupportedOperationException  e) {
					//Linux
					Runtime.getRuntime().exec(new String[]{"xdg-open", authorizationURL} );
				}
			}catch(URISyntaxException e) {
				e.printStackTrace();
				return null;
			}catch(SecurityException e) {
				System.err.println("Opening browser is forbidden. Please check security settings.");
				e.printStackTrace();
				return null;
			}catch(IOException e) {
				e.printStackTrace();
				return null;
			}
			//get returned code
			HashMap<String, String> query;
			//waiting for user authorization
			while((query = server.getQueryCode()).isEmpty()) {};
			//stop server
			server.stop();
			//check if an error was returned and end program
			if(query.containsKey("error")) {
				System.out.println("Error occured:\n" + "Error: " + query.get("error"));
				return null;
			}
			//get access token with code
			accessTokenRequest.setCode(query.get("code"));
			try {
				accessTokenResponse = accessTokenRequest.execute();
			}catch (TokenResponseException e){
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			//accessToken is not expired
		}else if(expireTime > System.currentTimeMillis()) {
			return accessToken;
		}else { //accessToken is expired
			try {
				accessTokenResponse = refreshTokenRequest.execute();
			} catch(TokenResponseException e) {
				e.printStackTrace();
				return null;
			}catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		//"store" response
		accessToken = accessTokenResponse.getAccessToken();
		refreshTokenRequest.setRefreshToken(accessTokenResponse.getRefreshToken());
		expireTime = accessTokenResponse.getExpiresInSeconds() * 1000 + System.currentTimeMillis();
		return accessToken;
	}
}
