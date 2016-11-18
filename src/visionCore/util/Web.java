package visionCore.util;

import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

public class Web {

	public static BufferedImage getImage(String url) {
		
		BufferedImage img = null;
		
		try	{
			
			URLConnection connection =  new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			
			img = ImageIO.read(connection.getInputStream());
			
		} catch (Exception e) { }
		
		return img;
	}
	
	public static String tryGetHTML(String url, int tries, long wait) {
		
		return tryGetHTML(url, tries, wait, true);
	}
	
	public static String tryGetHTML(String url, int tries, long wait, boolean clean) {
		
		String html = Web.getHTML(url, clean);
		for (int t = 0; (html == null || html.length() <= 0) && t < tries; t++) { 
			
			try { Thread.sleep(wait); } catch (Exception e) {}
			
			html = Web.getHTML(url, clean); 
		}
		
		return html;
	}
	
	public static String getHTML(String url) {
		
		return getHTML(url, true);
	}
	
	public static String getHTML(String url, boolean clean) {
		
		String result = null;
		URLConnection connection = null;
		
		try {
			
			connection = new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			Scanner scanner = new Scanner(connection.getInputStream());
			scanner.useDelimiter("\\Z");
			result = scanner.next();
			
			if (clean) {
			
				result = clean(result);
			}
		  
		} catch (Exception e) { }
		
		return result;
	}
	
	public static String getHTML(String url, String user, String password) {
		
		String result = null;
		URLConnection connection = null;
		
		try {
			
			connection = new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			
			String userPassword = user + ":" + password;
			String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + encoding);
			
			Scanner scanner = new Scanner(connection.getInputStream());
			scanner.useDelimiter("\\Z");
			result = scanner.next();
			
		} catch (Exception e) { }
		
		return result;
	}
	
	public static String getDecodedHTML(String url, boolean clean) {
		
		String result = null;
		
		try {
			
			URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			
			Reader reader = null;
			if ("gzip".equals(connection.getContentEncoding())) {
				
				reader = new InputStreamReader(new GZIPInputStream(connection.getInputStream()));
				
			} else {
				
				reader = new InputStreamReader(connection.getInputStream());
			}
			
			StringBuilder sb = new StringBuilder(20000);
			
			while (true) {
				int ch = reader.read();
				if (ch==-1) { break; }
				
				sb.append((char)(ch));
			}
			
			result = sb.toString();
			
			if (clean) {
				
				result = clean(result);
			}
		  
		} catch (Exception e) { }
		
		return result;
	}
	
	public static String clean(String html) {
		
		String result = html.replace("<br>", "");
		result = result.replace("<br \\>", "");
		result = result.replace("<br />", "");
		result = result.replace("<i>", "");
		result = result.replace("</i>", "");
		result = result.replace("<b>", "");
		result = result.replace("</b>", "");
		result = result.replace("&quot;", "\"");
		result = result.replace("&hellip;", "...");
		result = result.replace("â€¦", "...");
		result = result.replace("â€™", "'");
		result = result.replace("Ã©", "é");
		result = result.replace("&#233;", "é");
		result = result.replace("Ã  ", "à");
		result = result.replace("â€œ", "\"");
		result = result.replace("â€?", "\"");
		result = result.replace("â€”", "-");
		result = result.replace("&#039;", "'");
		result = result.replace("&amp;", "&");
		result = result.replace("&gt;", "");
		
		return result;
	}
	
	public static String removeTag(String html, String tag) {
		
		tag = tag.replace("<", "");
		tag = tag.replace(">", "");
		String t = "<"+tag+">";
		
		try {
			
			int ind = html.toLowerCase().indexOf(tag.toLowerCase());
			String s = html.substring(0, ind);
			html = html.substring(ind);
			
			String closing = "</"+tag.toLowerCase()+">";
			
			if (html.contains(closing)) {
			
				html = html.substring(html.toLowerCase().indexOf(closing)+closing.length());
				
			} else if (html.contains(">")) { 
				
				html = html.substring(html.toLowerCase().indexOf(">")+1);
				
			} else { html = ""; }
			
			html = s+html;
			
			if (html.toLowerCase().contains(tag.toLowerCase())) { html = removeTag(html, tag); }
			
		} catch (Exception | Error e) { }
		
		return html;
	}
	
	public static boolean hasConnection() {
		
		try {
			
			Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces();
			while(eni.hasMoreElements()) {
	        	
	        	Enumeration<InetAddress> eia = eni.nextElement().getInetAddresses();
	            while(eia.hasMoreElements()) {
	            	
	            	InetAddress ia = eia.nextElement();
	                if (!ia.isAnyLocalAddress() && !ia.isLoopbackAddress() && !ia.isSiteLocalAddress()) {
	                	
	                	if (!ia.getHostName().equals(ia.getHostAddress())) {
	                		
	                		return true;
	                	}
	                }
	            }
	        }
			
		} catch (Exception e) { }
		
		return false;
	}
	
}
