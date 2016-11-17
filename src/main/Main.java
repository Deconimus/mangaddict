package main;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.renderer.Renderer;

import visionCore.geom.Vector2f;
import visionCore.io.MultiPrintStream;
import visionCore.math.FastMath;

public class Main extends BasicGame {

	public static AppGameContainer display;
	public static String abspath;
	
	public static boolean makeScreenShot = false, linux;
	
	public static Scene currentScene, lastScene;
	
	public static float displayScale;
	
	public static AtomicReference<MangaDL> mangadl;
	
	public static Thread mainthread;
	
	
	public static boolean ctrlDown, altDown, shiftDown;
	
	private boolean mouseDown[];
	
	public static float mouseX, mouseY, lXAxis, lYAxis, rXAxis, rYAxis;
	
	
	static {
		
		mouseX = 0f; mouseY = 0f;
		lXAxis = 0f; lYAxis = 0f;
		rXAxis = 0f; rYAxis = 0f;
		
	}
	
	
	public Main() {
		super("MangAddict");
		
		mouseDown = new boolean[12];
		for (int i = 0; i < mouseDown.length; i++) { mouseDown[i] = false; }
		
	}
	
	public static void main(String[] args) throws SlickException {
		
		setAbsPath();
		Settings.load();
		
		System.out.println(System.getProperty("os.name"));
		linux = !System.getProperty("os.name").toLowerCase().trim().contains("windows");
		
		System.out.println(new File(abspath+"/bin/natives/windows&linux").getAbsolutePath());
		
		if (linux) {
			
			try {
				
				ProcessBuilder pb = new ProcessBuilder(Arrays.asList(new String[]{ "java", "-version" }));
				//pb.directory(dir);
				pb.redirectErrorStream(true);
				Process p = pb.start();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				for (String line = "", cur = ""; (line = reader.readLine()) != null;) { System.out.println(line); }
				
			} catch (Exception e) {}
		}
		
		//System.setProperty("org.lwjgl.librarypath", new File(abspath+"/bin/natives/windows&linux").getAbsolutePath());
		//System.out.println(System.getProperty("org.lwjgl.librarypath"));
		
		Renderer.setRenderer(Renderer.VERTEX_ARRAY_RENDERER);
		
		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devicesArray = g.getScreenDevices();
		
		java.awt.Rectangle r = devicesArray[Settings.video_monitor].getDefaultConfiguration().getBounds();
		
		int fps = devicesArray[Settings.video_monitor].getDisplayMode().getRefreshRate();
		
		if (Settings.video_targetFPS >= 5) { fps = Settings.video_targetFPS; }
		
		boolean video_fullscreen = false, video_borderless = true;
		Vector2f video_resolution = new Vector2f((float)r.getWidth(), (float)r.getHeight());
		
		int w = Settings.video_width;
		int h = Settings.video_height;
		
		if (w < 640) { w = r.width; }
		if (h < 480) { h = r.height; }
		
		video_resolution.set(w, h);
		video_borderless = !Settings.video_windowDecoration;
		
		//video_resolution = new Vector2f(1280, 800);
		//video_resolution = new Vector2f(1024, 768);
		//video_resolution = new Vector2f(1680, 1050);
		//video_resolution = new Vector2f(1280, 1024);
		
		display = new AppGameContainer(new Main());
		
		if (video_borderless) {
			
			System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		}
		
		display.setDisplayMode((int)video_resolution.x, (int)video_resolution.y, video_fullscreen);
		
		display.setShowFPS(Settings.video_showFPS);
		display.setTargetFrameRate(60);
		display.setAlwaysRender(false);
		display.setVSync(false);
		
		int x = (int)(r.x + (r.getWidth() - video_resolution.x) * 0.5f);
		int y = (int)(r.y + (r.getHeight() - video_resolution.y) * 0.5f);
		
		if (!video_fullscreen && !video_borderless) { y -= 8f; }
		
		if (Settings.video_dodgeTaskbar != 0) {
			
			y = (int)FastMath.clampToRangeC(y + Settings.video_dodgeTaskbar, 0, r.getHeight() - video_resolution.y);
		}
		
		Display.setLocation(x, y);
		
		displayScale = (float)video_resolution.y / 1080f;
		
		Display.setResizable(false);
		
		/*
		JFrame frame = new JFrame("asd");
		frame.setBounds((int)x, (int)y, (int)video_resolution.x, (int)video_resolution.y);
		frame.setUndecorated(true);
		frame.setLayout(null);
		
		Canvas canvas = new Canvas();
		canvas.setBounds(0, 0, (int)video_resolution.x, (int)video_resolution.y);
		frame.getContentPane().add(canvas);
		
		frame.setVisible(true);
		*/
		
		display.setForceExit(false);
		
		mainthread = Thread.currentThread();
		
		display.start();
		
		// After exit
		
		MangaDL.stopQueueDaemon();
		
		DownloadMenu.killMangaDL();
		
		if (mangadl != null && mangadl.get() != null) {
			
			if (mangadl.get().process.isAlive()) {
				
				mangadl.get().cancelled.set(true);
				mangadl.get().process.destroy();
				System.out.println("Killed MangaDL");
			}
		}
		
		Settings.save();
		
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		
		//setOutStream(false);
		
		//FBOs.load();
		//Shaders.load();
		
		System.out.println(Display.getWidth());
		
		mangadl = new AtomicReference<MangaDL>();
		
		GUIRes.load();
		
		currentScene = new LoadingScene();
		
		try {
		
			if (currentScene != null) {
				
				currentScene.init();
				lastScene = currentScene;
			}
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
	}
	
	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		
		if (mangadl.get() != null) {
			
			mangadl.get().update(delta);
			
			if (mangadl.get().finished.get()) {
				
				mangadl.set(null);
			}
			
		}
		
		if (MAL.avatar == null && MAL.hasUser()) {
			
			MAL.loadAvatarImage();
		}
		
		Input in = gc.getInput();
		
		mouseX = in.getMouseX();
		mouseY = in.getMouseY();
		
		for (int i = 0; i < in.getControllerCount(); i++) {
			Controller controller = Controllers.getController(i);
			
			if (controller.getName().toLowerCase().contains("xbox")) {
			
				lXAxis = controller.getXAxisValue();
				lYAxis = controller.getYAxisValue();
				
				rXAxis = controller.getRXAxisValue();
				rYAxis = controller.getRYAxisValue();
				
			}
			
		}
		
		for (int i = 0; i < mouseDown.length && display.hadFocus(); i++) {
			
			boolean down = in.isMouseButtonDown(i);
			
			if (down ^ mouseDown[i]) {
				
				mouseInput(i, down && !mouseDown[i]);
				mouseDown[i] = down;
			}
		}
		
		try {
			
			if (currentScene != lastScene && currentScene != null) {
				
				currentScene.init();
			}
			
			if (currentScene != null) {
				
				currentScene.update(delta);
			}
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
		lastScene = currentScene;
		
	}
	
	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		
		try {
		
			if (currentScene != null) {
				
				currentScene.render(g);
			}
			
			if (makeScreenShot) {
				
				g.fillRect(-2, -2, 1, 1);
				
				makeScreenShot = false;
				ScreenShooter.makeScreenShot(g);
			}
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
	}

	@Override
	public void keyPressed(int key, char c) {
		
		handleInput(key, c, true);
	}
	
	@Override
	public void keyReleased(int key, char c) {
		
		handleInput(key, c, false);
	}
	
	public void handleInput(int key, char c, boolean pressed) {
		
		if (key == Input.KEY_LCONTROL || key == Input.KEY_RCONTROL) {
			
			ctrlDown = pressed;
			
		} else if (key == Input.KEY_LALT || key == Input.KEY_RALT) {
			
			altDown = pressed;
			
		} else if (key == Input.KEY_LSHIFT || key == Input.KEY_RSHIFT) {
			
			shiftDown = pressed;
			
		}
		
		try {
			
			if (currentScene != null) {

				currentScene.handleInput(key, c, pressed);
			}
			
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
		if (pressed && (key == Input.KEY_F2 || key == Input.KEY_F12)) {
			
			Main.makeScreenShot = true;
		}
		
	}
	
	public void mouseInput(int button, boolean pressed) {
		
		try {
			
			if (currentScene != null) {

				currentScene.mouseInput(button, pressed);
			}
			
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
	}
	
	@Override
	public void mouseWheelMoved(int change) {
		
		try {
			
			if (currentScene != null) {

				currentScene.mouseWheelMoved(change);
			}
			
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
	}
	
	
	public void handleControllerInput(int controller, int button, boolean pressed) {
		
		int key = -1;
		char c = '^';
		
		if (button == 0) {
			
			key = Input.KEY_UP;
			
		} else if (button == 1) {
			
			key = Input.KEY_RIGHT;
			
		} else if (button == 2) {
			
			key = Input.KEY_DOWN;
			
		} else if (button == 3) {
			
			key = Input.KEY_LEFT;
			
		} else if (button == 5) {
			
			key = Input.KEY_ENTER;
			
		} else if (button == 6) {
			
			key = Input.KEY_ESCAPE;
			
		} else if (button == 7) {
			
			if (currentScene instanceof ImageView) {
				
				key = Input.KEY_L;
				
			} else { key = Input.KEY_F1; }
			
		} else if (button == 8) {
			
			if (currentScene instanceof ImageView) {
				
				key = Input.KEY_L;
				
			} else { key = Input.KEY_F3; }
			
		} else if (button == 12) {
			
			key = Input.KEY_SPACE;
			
		} else if (button == 9) {
			
			key = Input.KEY_MINUS;
			
		} else if (button == 10) {
			
			key = Input.KEY_ADD;
			
		}
		
		if (key > -1) {
			
			handleInput(key, c, pressed);
		}
		
	}
	
	@Override
	public void controllerButtonPressed(int controller, int button) {
		handleControllerInput(controller, button + 4, true);
	}
	
	@Override
	public void controllerButtonReleased(int controller, int button) {
		handleControllerInput(controller, button + 4, false);
	}
	
	@Override
	public void controllerUpPressed(int controller) {
		handleControllerInput(controller, 0, true);
	}
	
	@Override
	public void controllerUpReleased(int controller) {
		handleControllerInput(controller, 0, false);
	}
	
	@Override
	public void controllerRightPressed(int controller) {
		handleControllerInput(controller, 1, true);
	}
	
	@Override
	public void controllerRightReleased(int controller) {
		handleControllerInput(controller, 1, false);
	}
	
	@Override
	public void controllerDownPressed(int controller) {
		handleControllerInput(controller, 2, true);
	}
	
	@Override
	public void controllerDownReleased(int controller) {
		handleControllerInput(controller, 2, false);
	}
	
	@Override
	public void controllerLeftPressed(int controller) {
		handleControllerInput(controller, 3, true);
	}
	
	@Override
	public void controllerLeftReleased(int controller) {
		handleControllerInput(controller, 3, false);
	}
	
	private static void setAbsPath() {
		
		try {
			
			abspath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath().replace("\\", "/");
			
			if (abspath.endsWith("/bin")) {
				
				abspath = abspath.substring(0, abspath.indexOf("/bin"));
			}
			
			if (abspath.endsWith(".jar")) {
				
				abspath = new File(abspath).getParentFile().getAbsolutePath();
			}
			
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	
	public static void setOutStream(boolean keepPrevLog) {
		
		try {
			
			File logfile = new File(abspath+"/log.txt");
			
			String prevLines = "";
			
			if (keepPrevLog && logfile.exists()) {
				
				try {
				
					BufferedReader br = new BufferedReader(new FileReader(logfile));
					
					int i = 0;
					for (String line = ""; (line = br.readLine()) != null; i++) {
						line = line.replace("\n", "").replace("\r", "").trim();
						
						prevLines += line+"\r\n";
					}
					
					br.close();
					
				} catch (Exception e) { e.printStackTrace(); }
				
			}
			
			PrintStream logOut = new PrintStream(new FileOutputStream(logfile));
			
			if (keepPrevLog && logfile.exists()) { logOut.print(prevLines); }
			
			PrintStream multiOut = new MultiPrintStream(logOut, System.out);
			PrintStream multiErr = new MultiPrintStream(logOut, System.err);
			
			System.setOut(multiOut);
			System.setErr(multiErr);
			
		} catch (Exception | Error e) { e.printStackTrace(); }
		
	}

}

