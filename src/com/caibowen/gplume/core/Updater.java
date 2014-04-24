package com.caibowen.gplume.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Updater {

	/**
	 * 
	 * @return latest version string
	 */
	public static String check() {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) 
					new URL("http://www.caibowen.com/bot/check-update/web/gplume")
						.openConnection();
			
			connection.setRequestMethod("GET");
			prepareConnection(connection);
			connection.connect();

			if (connection.getResponseCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				
				String inputLine;
				StringBuilder response = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				connection.disconnect();
				
				return response.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Gplume.VERSION_STR;
		}
		return Gplume.VERSION_STR;
	}
	
	private static final String HEADER_VERSION = "current_version";
	private static final String HEADER_CODE = "header_code";
	private static void prepareConnection(HttpURLConnection connection) {
		
		connection.addRequestProperty(HEADER_VERSION, Gplume.VERSION_STR);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847");
		connection.setRequestProperty(HEADER_CODE, getCode());
		connection.setRequestProperty("Connection", "close");
		connection.setAllowUserInteraction(false);
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(3000);
		connection.setInstanceFollowRedirects(true);
		connection.setDefaultUseCaches(false);
		connection.setDoOutput(false);
	}
	
	private static String getCode() {
		return null;	
	}

	static {
		System.setProperty("http.proxyHost", "127.0.0.1");
//		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "12345");
//		System.setProperty("https.proxyPort", "12345");
	}
	public static void main(String[] args) {
		System.out.println(check());
	}

}
