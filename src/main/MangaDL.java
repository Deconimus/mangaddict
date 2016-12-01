package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import visionCore.dataStructures.tuples.Tuple;
import visionCore.util.Files;
import visionCore.util.Web;

public class MangaDL {

	
	private static AtomicReference<List<Tuple<String, String[]>>> queue;
	
	private static AtomicReference<QueueDaemon> queued;
	
	public static void load() {
		
		MangaDL.queue = new AtomicReference<List<Tuple<String, String[]>>>();
		List<Tuple<String, String[]>> q = new ArrayList<Tuple<String, String[]>>();
		
		Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
		
		File queueFile = new File(Main.abspath+"/res/mangadl.queue");
		if (queueFile.exists()) {
			
			List<String> lines = Files.readLines(queueFile, true);
			for (String l : lines) {
				
				l = l.replace("\n", "").replace("\r", "").trim();
				
				List<String> args = new ArrayList<String>();
				
				Matcher matcher = regex.matcher(l);
				for (int i = 0; i < 1000 && matcher.find(); i++) {
					
					args.add(matcher.group().trim());
				}
				
				if (args.size() > 1) {
					
					String runTitle = args.get(0).trim();
					args.remove(0);
					
					String[] arguments = args.toArray(new String[args.size()]);
					
					q.add(new Tuple<String, String[]>(runTitle, arguments));
				}
				
			}
			
		}
		
		queue.set(q);
		
		/*
		for (Tuple<String, String[]> args : q) {
			System.out.println(args.x);
			for (String arg : args.y) {
				System.out.println(arg);
			} System.out.println();
		}
		*/
		
		Thread startup = new Thread(){
			
			@Override
			public void run() {
				
				if (Web.hasConnection()) {
					
					if (Settings.mangaAutoUpdate && System.currentTimeMillis() - Settings.lastUpdated >= 3600L * 1000L * Settings.updateEveryHours) {
						
						if (Main.mangadl.get() == null) {
							
							Main.mangadl.set(MangaDL.updateMangas());
							
						} else {
							
							MangaDL.addToQueue("Updating Manga", new String[]{ "-u" });
						}
						
					}
					
				}
				
				MangaDL.startQueueDaemon();
				
			}
			
		};
		startup.setDaemon(true);
		startup.start();
		
	}
	
	
	public String title;
	public AtomicBoolean finished;
	public AtomicReference<String> status;
	
	public Process process;
	
	public AtomicBoolean cancelled;
	
	private Thread t;
	
	
	private MangaDL(String title, String[] args) {
		
		this.title = title;
		this.status = new AtomicReference<String>("");
		this.finished = new AtomicBoolean(false);
		
		String path = Settings.mangadlPath.trim();
		
		if (path == null || path.length() <= 2) {
			
			path = Main.abspath+"/res/bin/mangadl/mangadl.jar";
			
		} else if (!path.toLowerCase().endsWith(".jar")) {
			
			if (path.endsWith("/")) { path = path.substring(0, path.length()-1); }
			path = path+"/mangadl.jar";
		}
		
		File mangadl = new File(path);
		if (!mangadl.exists()) { System.out.println("MangaDL not found.."); this.finished.set(true); return; }
		
		int statusmode = 0;
		if (title.toLowerCase().contains("manga")) { statusmode = 1; }
		if (title.toLowerCase().startsWith("dumping")) { statusmode = 2; }
		
		if (statusmode == 0) {
			
			status.set("Grabbing info");
		}
		
		this.cancelled = new AtomicBoolean(false);
		
		boolean reloadInfos = false;
		
		for (String arg : args) {
			arg = arg.replace("\"", "").toLowerCase().trim();
			
			if (!arg.startsWith("-")) {
				
				if (Mangas.get(arg) == null) {
					
					reloadInfos = true;
				}
			}
			
		}
		
		t = new MangaDLThread(mangadl, Main.linux, reloadInfos, args, statusmode, title);
		t.start();
		
	}
	
	public void update(int delta) {
		
		if (!this.finished.get() && !t.isAlive()) { this.finished.set(true); }
	}
	
	
	public static MangaDL updateMangas() {
		
		return new MangaDL("Updating Manga", new String[]{ "-u" });
	}
	
	public static MangaDL updateManga(String title) {
		
		MangaDL.addToQueue("Updating "+title, new String[]{ "-d", "\""+title+"\"", "--noinput" });
		
		return new MangaDL("Updating "+title, new String[]{ "-d", title, "--noinput" });
	}
	
	public static MangaDL downloadManga(String title) {
		
		MangaDL.addToQueue("Downloading "+title, new String[]{ "-d", "\""+title+"\"", "--noinput" });
		
		return new MangaDL("Downloading "+title, new String[]{ "-d", title, "--noinput" });
	}
	
	public static MangaDL refreshMangas() {
		
		return new MangaDL("Refreshing Manga", new String[]{ "-r" });
	}
	
	public static MangaDL refreshManga(String title) {
		
		return new MangaDL("Refreshing "+title, new String[]{ "-r", title });
	}
	
	public static MangaDL dumpHot() {
		
		return new MangaDL("Dumping meta for popular manga.", new String[]{ "--dumpsearch" });
	}
	
	public static MangaDL dumpSearch(String title) {
		
		return new MangaDL("Dumping search for +\""+title+"\"", new String[]{ "--dumpsearch", title });
	}
	
	public static MangaDL dumpMAL() {
		
		return new MangaDL("Dumping MAL list", new String[]{ "--dumpmal", "\""+Settings.MAL_userName+"\"" });
	}
	
	public static MangaDL runWithArgs(String runTitle, String[] args) {
		
		return new MangaDL(runTitle, args);
	}
	
	
	private class MangaDLThread extends Thread {
		
		private File mangadl;
		private String args[], runtitle;
		
		private int statusmode;
		
		private boolean linux, reloadInfos;
		
		public MangaDLThread(File mangadl, boolean linux, boolean reloadInfos, String[] args, int statusmode, String runtitle) {
			
			//this.setDaemon(true);
			
			this.mangadl = mangadl;
			this.args = args;
			this.statusmode = statusmode;
			
			this.linux = linux;
			this.reloadInfos = reloadInfos;
			
			this.runtitle = runtitle;
			
		}
		
		@Override
		public void run() {
			
			try {
				
				List<String> cmdAndArgs = new ArrayList<String>(args.length+7);
				
				cmdAndArgs.add("java");
				cmdAndArgs.add("-jar");
				
				if (!linux) {
					
					cmdAndArgs.add("\""+mangadl.getAbsolutePath()+"\"");
					
				} else {
					
					cmdAndArgs.add(mangadl.getName()); // give me a fucking break..
					
				}
				
				File mangaDir = new File(Settings.mangaDir);
				File metaIn = new File(Settings.metaIn);
				
				cmdAndArgs.add("--output");
				cmdAndArgs.add("\""+mangaDir.getAbsolutePath().replace("\\", "/")+"\"");
				cmdAndArgs.add("--metaout");
				cmdAndArgs.add("\""+metaIn.getAbsolutePath().replace("\\", "/")+"\"");
				
				for (int i = 0; i < args.length; i++) {
					
					if (args[i].contains(" ") && !(args[i].startsWith("\"") && args[i].endsWith("\""))) {
						
						args[i] = "\""+args[i]+"\"";
					}
				}
				
				for (String arg : args) { cmdAndArgs.add(arg); }
				
				File dir = mangadl.getParentFile();
				
				/*
				for (String arg : cmdAndArgs) {
					System.out.print(arg+" ");
				} System.out.print("\n"); */

				ProcessBuilder pb = new ProcessBuilder(cmdAndArgs);
				pb.directory(dir);
				//pb.redirectErrorStream(true); //dont set to true or mari wont work...
				Process p = pb.start();
				
				process = p;
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				for (String line = "", cur = ""; (line = reader.readLine()) != null;) {
					line = line.trim();
					String lineLC = line.toLowerCase();
					
					if (statusmode == 2) {
						
						status.set(line);
						
					} else {
					
						if (lineLC.startsWith("downloading or updating")) {
							
							try {
								
								cur = line.substring(line.indexOf("\"")+1);
								cur = cur.substring(0, cur.indexOf("\""));
								
								if (statusmode == 1) {
									
									status.set("Updating "+cur);
									
								}
								
							} catch (Exception e) { }
							
						} else if (lineLC.startsWith("saving chapter")) {
							
							String chap = "";
							
							try {
								
								chap = line.substring(lineLC.indexOf("saving chapter")+15).trim();
								
								if (chap.contains(" ")) {
									
									chap = chap.substring(0, chap.indexOf(" "));
								}
								
							} catch (Exception e) {}
							
							double chnr = 0;
							
							try {
								
								chnr = Double.parseDouble(chap.trim());
								
							} catch (Exception e) {}
							
							if (chnr >= 2.0 && reloadInfos) {
								
								Mangas.reload();
								reloadInfos = false;
							}
							
							if (statusmode == 0) {
								
								status.set("Saving chapter "+chap);
								
							} else if (statusmode == 1) {
								
								status.set("Saving "+cur+" Ch."+chap);
								
							}
							
						} else if (lineLC.startsWith("starting a webclient")) {
							
							if (statusmode == 0) {
								
								status.set("Bypassing age warning");
							}
							
						}
						
					}
						
					if (!Main.mainthread.isAlive()) {
						
						cancelled.set(true);
						p.destroy();
						System.out.println("Killed MangaDL (in loop)");
						return;
					}
					
				}
				
				if (!Main.mainthread.isAlive() && p.isAlive()) {
					
					cancelled.set(true);
					p.destroy();
					System.out.println("Killed MangaDL (after loop)");
					return;
				}
				
			} catch (Exception e) { e.printStackTrace(); cancelled.set(true); }
			
			if (cancelled.get()) {
				
				try {
					
					process.destroy();
					
				} catch (Exception e) {}
				
			} else {
				
				if (args.length == 1 && args[0].equals("-u")) {
					
					Settings.lastUpdated = System.currentTimeMillis();
					Settings.save();
				}
				
				Tuple<String, String[]> firstQueued = MangaDL.getFirstFromQueue();
				
				if (firstQueued != null && runtitle.replace("\"", "").trim().toLowerCase().equals(firstQueued.x.replace("\"", "").trim().toLowerCase())) {
					
					MangaDL.removeFirstFromQueue();
				}
				
			}
			
			finished.set(true);
			
		}
		
	}
	
	
	public static void startQueueDaemon() {
		
		if (queued == null) { queued = new AtomicReference<QueueDaemon>(); }
		
		QueueDaemon qd = queued.get();
		
		if (qd != null && qd.isAlive()) {
			
			System.out.println("QueueDaemon is already running.");
			
		} else {
			
			qd = new QueueDaemon();
			qd.start();
			
			queued.set(qd);
			
		}
		
	}
	
	public static void addToQueue(String runtitle, String[] args) {
		
		if (!(runtitle.startsWith("\"") && runtitle.endsWith("\""))) {
			
			runtitle = "\""+runtitle+"\"";
		}
		
		List<Tuple<String, String[]>> q = queue.get();
		q.add(new Tuple<String, String[]>(runtitle, args));
		
		queue.set(q);
		
		saveQueue();
	}
	
	public static void clearQueue() {
		
		List<Tuple<String, String[]>> q = queue.get();
		q.clear();
		
		queue.set(q);
		
		saveQueue();
	}
	
	public static Tuple<String, String[]> getFirstFromQueue() {
		
		if (queue.get().isEmpty()) { return null; }
		
		return queue.get().get(0);
	}
	
	public static void removeFirstFromQueue() {
		
		List<Tuple<String, String[]>> q = queue.get();
		
		if (q != null && !q.isEmpty()) {
			
			q.remove(0);
		}
		
		queue.set(q);
		
		saveQueue();
	}
	
	private static void saveQueue() {
		
		List<Tuple<String, String[]>> q = queue.get();
		
		String txt = "";
		
		for (Tuple<String, String[]> entry : q) {
			
			txt += entry.x+" ";
			
			for (String arg : entry.y) {
				arg = arg.trim();
				
				if (arg.contains(" ") && !(arg.startsWith("\"") && arg.endsWith("\""))) {
					
					arg = "\""+arg+"\"";
				}
				
				txt += arg+" ";
			}
			
			txt = txt.trim()+"\r\n";
			
		}
		
		File qf = new File(Main.abspath+"/res/mangadl.queue");
		if (qf.exists()) { qf.delete(); }
		
		Files.writeText(qf, txt);
	}
	
	public static void stopQueueDaemon() {
		
		if (queued == null) { queued = new AtomicReference<QueueDaemon>(); }
		
		QueueDaemon qd = queued.get();
		
		if (qd != null && qd.isAlive()) {
			
			qd.interrupt();
			qd = null;
		}
		
	}
	
	private static class QueueDaemon extends Thread {
		
		public static final long SLEEP_MILLIS = 500;
		
		public QueueDaemon() {
			
			this.setDaemon(true);
		}
		
		@Override
		public void run() {
			
			while (!interrupted()) {
				
				MangaDL mdl = Main.mangadl.get();
				List<Tuple<String, String[]>> q = MangaDL.queue.get();
				
				if ((mdl == null || mdl.finished.get()) && !q.isEmpty()) {
					
					String runtitle = q.get(0).x.replace("\"", "").trim();
					String[] args = q.get(0).y;
					
					if (runtitle.length() < 1) { runtitle = "Queued Task"; }
					
					Main.mangadl.set(MangaDL.runWithArgs(runtitle, args));
					
				}
				
				try { sleep(SLEEP_MILLIS); } catch (InterruptedException e) { return; }
				
			}
			
		}
		
	}
	
}
