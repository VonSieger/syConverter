package authorization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;

import com.sun.net.httpserver.HttpExchange;

import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * @author boss
 *
 */

public class CallbackHandler implements HttpHandler{
	
	//Lock for protecting query
	private Lock queryLock;
	private HashMap<String, String> query;

	public CallbackHandler(HashMap<String, String> parameters, Lock queryLock) {
		this.query = parameters;
		this.queryLock = queryLock;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
	 */
	@Override
	public void handle(HttpExchange he) {
		queryLock.lock();
		String queryRaw = he.getRequestURI().getQuery();
		//parse query
		String[] querySplit = queryRaw.split("&");
		for(String param : querySplit) {
			String[] pair = param.split("=");
			query.put(pair[0], pair[1]);
		}
		
		//write response
		String response = "";
		if(query.containsKey("error"))
			response = "Error: " + query.get("error");
		else
			response = "Authorization was successful. \nYou can now close the browser.";
		try {
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
	        os.write(response.toString().getBytes());
	        os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		queryLock.unlock();
	}
	

	/**
	 * @return the query
	 */
	public HashMap<String, String> getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(HashMap<String, String> query) {
		this.query = query;
	}
}
