/*******************************************************************************
 * Copyright 2014 Bowen Cai
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

			if (connection.getResponseCode() != 200) {
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
		}
		return Gplume.VERSION_STR;
	}
	
	private static final String HEADER_VERSION = "current_version";
	private static final String HEADER_CODE = "header_code";
	private static void prepareConnection(HttpURLConnection connection) {
		
		connection.addRequestProperty(HEADER_VERSION, Gplume.VERSION_STR);
		connection.setRequestProperty("User-Agent", sysInfo());
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
	private static String sysInfo() {
		return null;
	}
	static {
//		System.setProperty("http.proxyHost", "127.0.0.1");
//		System.setProperty("http.proxyPort", "12345");
//		System.setProperty("https.proxyHost", "127.0.0.1");
//		System.setProperty("https.proxyPort", "12345");
	}
	public static void main(String[] args) {
		System.out.println(check());
	}

}
