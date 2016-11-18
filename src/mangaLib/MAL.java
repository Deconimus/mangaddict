package mangaLib;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import visionCore.util.Web;

public class MAL {

	
	public static final String MANGA_SEARCH_URL = "http://myanimelist.net/manga.php?q=";
	public static final String SPACE_REPLACE = "%20";
	
	public static final int STATUS_READING = 1, STATUS_FINISHED = 2, STATUS_ON_HOLD = 3, STATUS_DROPPED = 4, STATUS_PLAN_TO_READ = 6;
	
	
	public static class MALEntry {
		
		public int id, status;
		public String title, posterUrl;
		
		public MALEntry() {
			
			this.id = -1;
			this.status = -1;
			
			this.title = "";
			this.posterUrl = "";
		}
		
	}
	
	
	public static List<MALEntry> getMangaList(String user) {
		
		return getMangaList(user, -1);
	}
	
	public static List<MALEntry> getMangaList(String user, int statusFilter) {
		
		List<MALEntry> list = new ArrayList<MALEntry>();
		
		String html = Web.getHTML("http://myanimelist.net/malappinfo.php?u="+user+"&status=all&type=manga", false);
		
		Document doc = null;
		try { doc = DocumentHelper.parseText(html); } catch (Exception e) { e.printStackTrace(); return list; }
		
		Element root = doc.getRootElement();
		
		for (Iterator<Element> it = root.elementIterator("manga"); it.hasNext();) {
			Element mangaElem = it.next();
			
			MALEntry entry = new MALEntry();
			
			for (Iterator<Element> i = mangaElem.elementIterator(); i.hasNext();) {
				Element elem = i.next();
				
				String name = elem.getName().trim().toLowerCase();
				
				if (name.equals("series_title")) {
					
					entry.title = MangaInfo.cleanTitle(elem.getText().trim());
					
				} else if (name.equals("series_image")) {
					
					entry.posterUrl = elem.getText().trim();
					
				} else if (name.equals("my_status")) {
					
					int status = -1;
					try { status = Integer.parseInt(elem.getText().trim()); } catch (Exception e) {}
					
					entry.status = status;
					
				} else if (name.equals("series_mangadb_id")) {
					
					int id = -1;
					try { id = Integer.parseInt(elem.getText().trim()); } catch (Exception e) {}
					
					entry.id = id;
					
				}
				
			}
			
			if (statusFilter < 0 || statusFilter == entry.status) {
				
				list.add(entry);
			}
			
		}
		
		return list;
	}
	
	public static String getMangaUrlFromSearch(String html) {
		
		String f = "<div id=\"content\">";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<div class=\"normal_header";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<table";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<tbody>";
		html = html.substring(html.indexOf(f)+f.length());
		
		// skip the first one since it's not a result
		f = "</tr>";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<tr>";
		html = html.substring(html.indexOf(f)+f.length());
		
		// same shit here
		f = "</td>";
		html = html.substring(html.indexOf(f)+f.length());

		f = "<td>";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<a class=\"hoverinfo_trigger fw-b\" href=";
		html = html.substring(html.indexOf(f)+f.length()+1);
		
		return html.substring(0, html.indexOf(" id=")-1);
	}
	
	
	public static BufferedImage getProfilePic(String username) {
		
		String html = Web.getHTML("http://myanimelist.net/profile/"+username, false);
		if (html == null || html.length() < 100) { return null; }
		
		String f = "<div id=\"content\">";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<div class=\"user-profile\">";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<div id=\"user-image mb8\">";
		html = html.substring(html.indexOf(f)+f.length());
		
		f = "<img src=";
		html = html.substring(html.indexOf(f)+f.length()+1);
		
		String imgurl = html.substring(0, html.indexOf(">")-1);
		
		return Web.getImage(imgurl);
	}
	
}
