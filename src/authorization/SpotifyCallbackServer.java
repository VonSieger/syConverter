/**
 * 
 */
package authorization;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.net.httpserver.HttpServer;

//TODO handle access restriction warnings
/**
 * This server is just for fetching the response of the /authorize endpoint.
 * Look at
 * https://developer.spotify.com/documentation/general/guides/authorization-guide/
 * for further details
 * 
 * @author boss
 *
 */
public class SpotifyCallbackServer {

	private static int PORT = 9091;
	private static String CALLBACK_CODE = "/callbackCode";
	private CallbackHandler callbackCodeHandler;
	//Lock for protecting query
	private Lock queryLock;
	private HashMap<String, String> query;
	private HttpServer server;

	public SpotifyCallbackServer() {
		try {
			server = HttpServer.create(new InetSocketAddress(PORT), 0);
			queryLock = new ReentrantLock();
			query = new HashMap<String, String>();
			callbackCodeHandler = new CallbackHandler(query, queryLock);
			server.createContext(CALLBACK_CODE, callbackCodeHandler);
		} catch (BindException e) {
			System.err.println(
					"ERROR: Unable to create server. Other application migth already run on " + PORT + " port.");
			e.printStackTrace();
			System.exit(1);
		}catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Returns the query parameters last sent to http://localhost:9091/callbackCode.
	 * This URL should be set as the redirect_uri in the authorization request to https://accounts.spotify.com/authorize.
	 * If authorization by the user was successful the query should contain a key "code" with the corresponding value.
	 * Otherwise a key "error" with the error message and eventually a "state" key.
	 * If there was no call to the /callbackCode, after the last call of this method, the returned HashMap is going to be empty.
	 * For details look at https://developer.spotify.com/documentation/general/guides/authorization-guide/
	 * (Authorization Code Flow 1.)
	 * @return query of last call to /callbackCode
	 */
	public HashMap<String, String> getQueryCode(){
		queryLock.lock();
		if(!query.isEmpty()) {
			HashMap<String, String> queryOld = query;
			query = new HashMap<String, String>();
			callbackCodeHandler.setQuery(query);
			queryLock.unlock();
			return queryOld;
		}
		queryLock.unlock();
		return query;
	}
	
	public static URI getCallbackCodeURI() {
		try {
			return new URI ("http://localhost:" + PORT + CALLBACK_CODE);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Wrapper for HttpServer.start():
	 * (https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html#stop-int-)
	 * Starts this server in a new background thread.
	 * The background thread inherits the priority, thread group and context class loader of the caller.
	 */
	public void start() {
		server.start();
	}
	
	/**
	 * Wrapper for HttpServer.stop(int delay):
	 * (https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html#stop-int-)
	 * stops this server by closing the listening socket and disallowing any new exchanges from being processed.
	 * The method will then block until all current exchange handlers have completed or else when approximately
	 *  delay seconds have elapsed (whichever happens sooner). Then, all open TCP connections are closed,
	 *  the background thread created by start() exits, and the method returns.
	 *  Once stopped, a HttpServer cannot be re-used.
	 */
	public void stop() {
		server.stop(0);
	}
}
