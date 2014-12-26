/*
 * ViewPane.java
 *
 * Created on 12 November 2007, 18:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine.shooter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.VolatileImage;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import sydneyengine.AbstractEvent;
import sydneyengine.Controller;
import sydneyengine.EventWrapper;
import sydneyengine.MockSystem;
import sydneyengine.ui.CustomCursors;
import sydneyengine.ui.StatusMenu;
import sydneyengine.ui.Updatable;

// good java2d option is -Dsun.java2d.trace=,count

/**
 *
 * Sets up the Graphics2D object which is passed to all render(Graphics2D) methods 
 * of game objects.
 * 
 * Also handles basic events by putting them into the AWTEventCache.
 * 
 * this is unique for each player, so it can show things like frames per seconds, personal messages, etc.
 * 
 * @author CommanderKeith
 */
public class ViewPane extends JComponent implements Updatable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	
	protected GameFrame gameFrame;
	protected Controller controller;
	protected Player player;
	VolatileImage backImage;
	protected Graphics2D backImageGraphics2D;
	Point2D.Float centre = new Point2D.Float(0, 0);	// this is the centre of the component, it is not the view's centre in world coordinates. 
	Point2D.Float viewCenterInWorldCoords = new Point2D.Float();
	BBox viewRectInWorldCoords = new BBox();
	protected AffineTransform originalTransform;
	protected float scaleFactor = 1f;
	protected float scaleSpeed = 0.8f;
	long lastRenderTimeNanos = -1;
	volatile boolean scaleUp = false;
	volatile boolean scaleDown = false;
	int count = 1;
	// The following are set by the mouseMoved etc methods and change frequently in Swing's 
	// thread so they should not be accessed in by game objects running in the Controller thread.
	protected volatile float relativeMouseXNow = 0;
	protected volatile float relativeMouseYNow = 0;
	// The following should be copies of the above, copied when rendering starts. 
	// They can be accessed by game world objects and they won't change during rendering.
	protected float relativeMouseX = 0;
	protected float relativeMouseY = 0;
	protected boolean showMapDescriptionsNow = true;
	protected boolean showMapDescriptions = true;
	public static boolean SHOW_STATS = false;
	
	protected StatusMenu statusMenu;
	
	float maxTimeToKeepMessageTextEventsSeconds = 5f;
	int maxMessageLinesToDisplay = 1;
	

	/** Creates a new instance of ViewPane */
	public ViewPane(GameFrame gameFrame, Controller controller) {
		this.gameFrame = gameFrame;		
		this.controller = controller;
		setOpaque(false);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		//addComponentListener(this);
		setCursor(CustomCursors.getGameCursor());
		
		statusMenu = new StatusMenu(this);
		
//		pv = new PainterVariables(this);
	}

	@Override
	public void update(Graphics g) {
	//System.out.println(this.getClass().getSimpleName()+": update() called********************************");
	}

	@Override
	public void paint(Graphics g) {
	//System.out.println(this.getClass().getSimpleName()+": paint() called********************************");
	}

	public Point2D.Float getCentre() {
		return centre;//new Point2D.Float(this.getWidth()/2f, this.getHeight()/2f);
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * This method is used to allow components to be modified.  Any JInternalFrames which are 
	 * contained this.getGameFrame().getDesktopPane() that have content pane which implements 
	 * Updatable will have doMove called on them.
	 * @param seconds
	 * @param timeAtStartOfMoveSeconds
	 */
	@Override
	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		final Component[] componentsInDesktopPane = this.getGameFrame().getDesktopPane().getComponentsWithMainFirst();
		final int numGlassPaneComponents = ((Container) this.getGameFrame().getGlassPane()).getComponentCount();

		// Only paint extra components if there are more than one - the the first component
		// will be this ViewPane or else the component holding this ViewPane.
		if (componentsInDesktopPane.length > 1 || numGlassPaneComponents > 0) {
			// update any components that implement Updatable:
			for (int i = 1; i < componentsInDesktopPane.length; i++) {
				Component c = componentsInDesktopPane[i];
				//System.out.println(this.getClass().getSimpleName() + ": c == "+c);
				if (c instanceof JInternalFrame) {
					if (((JInternalFrame) c).getContentPane() instanceof Updatable) {
						Updatable updatableComponent = (Updatable) ((JInternalFrame) c).getContentPane();
						//System.out.println(this.getClass().getSimpleName() + ": updatableComponent.doMove()!!, updatableComponent == "+updatableComponent);
						updatableComponent.doMove(seconds, timeAtStartOfMoveSeconds);
					}
				}
			}
		}
	}
	
	protected VolatileImage createVolatileImage() {	
		return createVolatileImage(getWidth(), getHeight(), Transparency.OPAQUE);
	}
	
	protected VolatileImage createVolatileImage(int width, int height, int transparency) {	
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		VolatileImage image = null;

		image = gc.createCompatibleVolatileImage(width, height, transparency);

		int valid = image.validate(gc);

		if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
			image = this.createVolatileImage(width, height, transparency);
		}
		System.out.println(this.getClass().getSimpleName() + ": initiated VolatileImage backImage for quick rendering");
		return image;
	}
	public void render() {
		if (this.isVisible() == false){
			System.out.println(this.getClass().getSimpleName() + ": this.isVisible() == false !!!");
			this.setVisible(true);
			return;
		}
		if (player.getWorld() == null) {
			System.out.println(this.getClass().getSimpleName() + ": player.getWorld() == null!!!");
			return;
		}
//		if (player.getBox() == null) {
//			System.out.println(this.getClass().getSimpleName() + ": player.getBox() == null, so player mustn't have respawned yet");
//			return;
//		}
		if (getWidth() <= 0 || getHeight() <= 0) {			
			System.out.println(this.getClass().getSimpleName() + ": width &/or height <= 0!!!");
			return;
		}
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		if (backImage == null || getWidth() != backImage.getWidth() || getHeight() != backImage.getHeight() || backImage.validate(gc) != VolatileImage.IMAGE_OK) {
			//backImage = this.getGraphicsConfiguration().createCompatibleVolatileImage(getWidth(), getHeight(), Transparency.OPAQUE);
			backImage = createVolatileImage();
		}
		
		do {
			int valid = backImage.validate(gc);
			if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
				backImage = createVolatileImage();
			}
			try {
				backImageGraphics2D = backImage.createGraphics();
				renderWorldThenMenus(); // This is assumed to be created somewhere else, and is only used as an example.
			} finally {
				// It's always best to dispose of your Graphics objects.
				backImageGraphics2D.dispose();
			}
		} while (backImage.contentsLost());
		
		
		
		if (getGraphics() != null) {
			// This painting need not be on the EDT since we are just painting an image, no components.
			getGraphics().drawImage(backImage, 0, 0, null);
			Toolkit.getDefaultToolkit().sync(); // to flush the graphics commands to the graphics card. 
			//see http://www.javagaming.org/forums/index.php?topic=15000.msg119601;topicseen#msg119601
		} else {
			System.out.println(this.getClass().getSimpleName() + ": getGraphics() == " + getGraphics() + ", so the component may not be displayable.");
			this.setVisible(true);
		}
	}
	
	protected void renderWorldThenMenus(){
		originalTransform = backImageGraphics2D.getTransform();
		renderWorld(player.getWorld(), backImageGraphics2D);
		backImageGraphics2D.setTransform(originalTransform);
		if (this.hasFocus() == false) {
			//backImageGraphics2D.setTransform(getOriginalAT());
			backImageGraphics2D.setColor(Color.BLACK);
			String unFocusedString = "Click here to focus!";
			backImageGraphics2D.drawString(unFocusedString, this.getCentre().x - this.getFontMetrics(backImageGraphics2D.getFont()).stringWidth(unFocusedString) / 2, this.getCentre().y);
		}
		// Here we paint any components like menus which are contained by the GameFrame's GameDesktopPane
		final Component[] componentsInDesktopPane = this.getGameFrame().getDesktopPane().getComponentsWithMainFirst();
		final int numGlassPaneComponents = ((Container) this.getGameFrame().getGlassPane()).getComponentCount();

		// Only paint extra components if there are more than one - the the first component
		// will be this ViewPane or else the component olding this ViewPane.
		if (componentsInDesktopPane.length > 1 || numGlassPaneComponents > 0) {
			//System.out.println(this.getClass().getSimpleName()+": GameFrame.listenedToWindow.isVisible() == true");
			// must exec this code on the Event Dispatch Thread to avoid dead-locks with the EDT's painting and modifications.
			final ViewPane thisViewPane = this;
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						if (thisViewPane.isVisible() == true && thisViewPane.isShowing()) {
							Point viewPaneLoc = thisViewPane.getLocationOnScreen();
							AffineTransform originalAT = backImageGraphics2D.getTransform();
							for (int i = 1; i < componentsInDesktopPane.length; i++) {
								Component c = componentsInDesktopPane[i];
								if (c.isShowing() == false) {
									continue;
								}
								if (c instanceof JInternalFrame) {
									if (((JInternalFrame) c).isVisible() == false) {
										continue;
									}
								}
								Point componentLoc = c.getLocationOnScreen();
								// translate the backImageGraphics2D's AffineTransform to paint the component c where it is meant to appear.
								backImageGraphics2D.translate(componentLoc.x - viewPaneLoc.x, componentLoc.y - viewPaneLoc.y);

								//backImageGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

								c.paintAll(backImageGraphics2D);
								backImageGraphics2D.setTransform(originalAT);
							}
							if (numGlassPaneComponents > 0) {
								Component[] componentsInGlassPane = ((Container) getGameFrame().getGlassPane()).getComponents();
								for (int i = 1; i < componentsInGlassPane.length; i++) {
									Component c = componentsInGlassPane[i];
									Point componentLoc = c.getLocationOnScreen();
									// translate the backImageGraphics2D's AffineTransform to paint the component c where it is meant to appear.
									backImageGraphics2D.translate(componentLoc.x - viewPaneLoc.x, componentLoc.y - viewPaneLoc.y);
									c.paintAll(backImageGraphics2D);
									backImageGraphics2D.setTransform(originalAT);
									System.out.println(this.getClass().getSimpleName() + ": painting component i == " + i + " in glasspPane");
								}
							}
						}
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			} catch (java.lang.reflect.InvocationTargetException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	protected void renderWorld(GameWorld world, Graphics2D g) {
		// Here we set the mouse coords. Can not directly access relativeMouseXNow since it is changed 
		// in Swing's thread, perhaps while rendering takes place. So to keep it the 
		// same throughout rendering we store it as relativeMouseX.

		// The following should be copies of the above, copied when rendering starts. 
		// They can be accessed by game world objects and they won't change during rendering.
		relativeMouseX = relativeMouseXNow;
		relativeMouseY = relativeMouseYNow;

		showMapDescriptions = showMapDescriptionsNow;

		centre.x = getWidth() / 2f;
		centre.y = getHeight() / 2f;
		
		long currentTime = MockSystem.nanoTime();
		if (lastRenderTimeNanos == -1) {
			lastRenderTimeNanos = currentTime;
		}
		if (scaleUp != scaleDown) {
			// limits on zoom in/ out
			if (scaleUp && scaleFactor>.25) {
				scaleFactor -= scaleSpeed * ((currentTime - this.lastRenderTimeNanos) / 1000000000.0);
			}
			if (scaleDown && scaleFactor<4) {
				scaleFactor += scaleSpeed * ((currentTime - this.lastRenderTimeNanos) / 1000000000.0);
			}
		}
		lastRenderTimeNanos = currentTime;

		viewCenterInWorldCoords = getPlayer().getViewCentreOnMap();
		float scaledWidth = getWidth()/scaleFactor;
		float scaledHeight = getHeight()/scaleFactor;
		
		viewRectInWorldCoords.x = viewCenterInWorldCoords.x - scaledWidth/2f;
		viewRectInWorldCoords.y = viewCenterInWorldCoords.y - scaledHeight/2f;
		viewRectInWorldCoords.w = scaledWidth;
		viewRectInWorldCoords.h = scaledHeight;
		
		
		g.translate(centre.x, centre.y);
		g.scale(scaleFactor, scaleFactor);
		g.translate(-viewCenterInWorldCoords.x, -viewCenterInWorldCoords.y);

		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		world.render(this);
		g.setTransform(originalTransform);
		
		
		int lineHeight = 20;		
		int numMessageDisplayed=0;
		Font orig= g.getFont();
		for (int i = player.messages.size() - 1; i >= 0; i--) 
		{
			
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16 - 2* numMessageDisplayed));
			
			PersonalMessage m= player.messages.get(i);

			String message= m.message;
			if(message.contains("Picked up")){ // picking up a weapon
				g.setColor(Color.black);
			}else if(message.contains("out of ammo")){
				g.setColor(Color.red);
			}else if(message.contains("spawn")){
				g.setColor(Color.cyan);
			}else if(message.contains("Obtained")){
				// for non-weapon items
				g.setColor(Color.blue);
			}else if(message.contains("STUNNED")) {
				g.setColor(Color.white);
			}
			else g.setColor(Color.black);
			
			g.drawString(message, this.getWidth()/3, 100 + lineHeight*numMessageDisplayed);
			
			numMessageDisplayed++;
			if (numMessageDisplayed >= this.maxMessageLinesToDisplay) 
			{
				break;
			}
			
		}
		g.setFont(orig);
		
		
		
		if (isSHOW_STATS()){
			g.setTransform(originalTransform);
			g.setColor(Color.RED);
			int xStringCoord = 20;
			int yStringCoord = 27;
			int yStringInc = 15;
			int stringCounter = 0;
			g.drawString("IP: "+getGameFrame().serverHostIPString, xStringCoord, yStringCoord + yStringInc * stringCounter++);
			g.drawString("Server Host Name: "+ getGameFrame().serverHostNameString, xStringCoord, yStringCoord + yStringInc * stringCounter++);
			//g.drawString("FPS: " + this.getGameFrame().getController().getFPSCounter().getFPSRounded(), xStringCoord, yStringCoord + yStringInc * stringCounter++);
			g.drawString("Millis/frame: " + Math.round(getGameFrame().getController().getFPSCounter().getAvTimeBetweenUpdatesMillis() * 10) / 10.0, xStringCoord, yStringCoord + yStringInc * stringCounter++);
			int seconds = (int) this.getPlayer().getWorld().getTotalElapsedSeconds();
			g.drawString("UpTime: " + seconds + (seconds % 2 == 0 ? "*" : ""), xStringCoord, yStringCoord + yStringInc * stringCounter++);
			g.drawString("Used Memory: " + (Math.round(getGameFrame().getController().getFPSCounter().getUsedMemory() / 10000f) / 100f) + " M", xStringCoord, yStringCoord + yStringInc * stringCounter++);
			g.drawString("Free Memory: " + (Math.round(getGameFrame().getController().getFPSCounter().getFreeMemory() / 10000f) / 100f) + " M", xStringCoord, yStringCoord + yStringInc * stringCounter++);
			//System.out.println(this.getClass().getSimpleName()+": getUsedMemory() == "+(Math.round(getGameFrame().getController().getFPSCounter().getUsedMemory() / 10000f) / 100f));
			//System.out.println(this.getClass().getSimpleName()+": getFreeMemory() == "+(Math.round(getGameFrame().getController().getFPSCounter().getFreeMemory() / 10000f) / 100f));
			long latencyFromServerToThisVMNanos = getGameFrame().getController().getLatencyToServerNanos();
			g.drawString("Latency (millis): " + Math.round(latencyFromServerToThisVMNanos / 1000000f), xStringCoord, yStringCoord + yStringInc * stringCounter++);
			long serverClockDiffNanos = getGameFrame().getController().getServerClockDiffNanos();
			//g.drawString("Clock diff: " + Math.round(serverClockDiffNanos / 1000000f), xStringCoord, yStringCoord + yStringInc * stringCounter++);
			g.drawString("getNumDoMaxTimeMoves(): " + world.getNumDoMaxTimeMoves(), xStringCoord, yStringCoord + yStringInc * stringCounter++);
			g.drawString("Stored Objects: " + world.getController().getSSOut().getNumStoredObjects(), xStringCoord, yStringCoord + yStringInc * stringCounter++);

			//g.drawString("getWorldMouseX() == " + getWorldMouseX(), xStringCoord, yStringCoord + yStringInc * stringCounter++);
			//g.drawString("getWorldMouseY() == " + getWorldMouseY(), xStringCoord, yStringCoord + yStringInc * stringCounter++);
		}
		
		
			g.setColor(Color.BLACK);
			/*
			g.fillRect(290, 5, 90, 15);
			g.setColor(Color.WHITE);
			*/
			g.drawString("FPS: " + this.getGameFrame().getController().getFPSCounter().getFPSRounded(), 20, 15);
		
		
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
			scaleUp = true;
			return;
		} else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
			scaleDown = true;
			return;
		} else if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE || e.getKeyCode() == KeyEvent.VK_DEAD_TILDE) {
			showMapDescriptionsNow = !showMapDescriptionsNow;
			return;
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			getGameFrame().doGameMenu(this);
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			getStatusMenu().getAllyChatSelector().setSelected(e.isShiftDown());
			getStatusMenu().getMessageField().requestFocus();
		}

		PlayerKeyEvent playerKeyEvent = new PlayerKeyEvent(getPlayer(), PlayerKeyEvent.KEY_PRESS, e.getKeyCode());
		sendEvent(playerKeyEvent);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
			scaleUp = false;
			return;
		} else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
			scaleDown = false;
			return;
		}
		/* else if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE || e.getKeyCode() == KeyEvent.VK_DEAD_TILDE) {
		showMapDescriptionsNow = false;
		return;
		}*/
		PlayerKeyEvent playerKeyEvent = new PlayerKeyEvent(getPlayer(), PlayerKeyEvent.KEY_RELEASE, e.getKeyCode());
		sendEvent(playerKeyEvent);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (hasFocus() == false) {
			requestFocus();
		}
		doMouseEvent(e, PlayerMouseEvent.MOUSE_PRESS);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		doMouseEvent(e, PlayerMouseEvent.MOUSE_RELEASE);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	MouseEvent lastMouseMovedEvent = null;
	long lastMouseMovedEventSystemTime = 0;
	boolean lastMouseMovedEventSent = true;
	long minNanosBetweenMouseMoveEventSends = 50000000;	// 0.05 seconds
	@Override
	public void mouseDragged(MouseEvent e) {
		lastMouseMovedEvent = e;
		long timeNow = MockSystem.nanoTime();
		if (timeNow > lastMouseMovedEventSystemTime + minNanosBetweenMouseMoveEventSends){
			doMouseEvent(e, PlayerMouseEvent.MOUSE_DRAG);
			lastMouseMovedEventSystemTime = timeNow;
			lastMouseMovedEventSent = true;
		}else{
			//lastMouseMovedEventSystemTime = timeNow;
			lastMouseMovedEventSent = false;
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		lastMouseMovedEvent = e;
		long timeNow = MockSystem.nanoTime();
		if (timeNow > lastMouseMovedEventSystemTime + minNanosBetweenMouseMoveEventSends){
			doMouseEvent(e, PlayerMouseEvent.MOUSE_MOVE);
			lastMouseMovedEventSystemTime = timeNow;
			lastMouseMovedEventSent = true;
		}else{
			//lastMouseMovedEventSystemTime = timeNow;
			lastMouseMovedEventSent = false;
		}
	}

	protected void doMouseEvent(MouseEvent e, int eventType) {
		float mx = (e.getPoint().x - centre.x) / this.getScaleFactor();
		float my = (e.getPoint().y - centre.y) / this.getScaleFactor();

		PlayerMouseEvent playerMouseEvent = new PlayerMouseEvent(getPlayer(), eventType, mx, my, e.getButton());
		
		// only send mouse events that aren't mouse move?
		//if(!(playerMouseEvent.getMouseEventType()==103))
		sendEvent(playerMouseEvent);
		relativeMouseXNow = mx;
		relativeMouseYNow = my;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		PlayerMouseWheelEvent playerMouseWheelEvent = new PlayerMouseWheelEvent(getPlayer(), e.getWheelRotation());
		sendEvent(playerMouseWheelEvent);
	}

	public void sendEvent(AbstractEvent e) {
		sendEvent(e.getEventWrapper());
	}
	
	// This is a convenience method.
	// Note: this method can be called from any thread since the last line:
	// getController().getEventStore().addEventFromViewPane(e);
	// is synchronized. However, in this game this method is only called by the 
	// main game loop thread so the EventStore's synchronization is not needed 
	// (but it's not chucked out).
	public void sendEvent(EventWrapper e) {
		GameWorld world = this.getPlayer().getWorld();
		if (world == null) {
			System.out.println(this.getClass().getSimpleName() + ": world == null!!!");
			return;
		}
		if (world.getSystemNanosAtStart() == -1) {
			// world.setLastUpdatedSystemNanos has not been called yet on the ServerController's thread,
			// so ignore this and wait until that thread has started the game loop.
			System.out.println(this.getClass().getSimpleName() + ": world.getSystemNanosAtStart() == " + world.getSystemNanosAtStart());
			return;
		}
		double timeStamp = world.getEventTimeStampNowSeconds();
		e.setTimeStamp(timeStamp);
		e.setId(this.getPlayer().getSSCode());
		e.setCount(count);
		count += 1;

		getController().getEventStore().addEventFromViewPane(e);
	}

	public GameFrame getGameFrame() {
		return gameFrame;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public Graphics2D getBackImageGraphics2D() {
		return backImageGraphics2D;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public AffineTransform getOriginalTransform() {
		return originalTransform;
	}

	public void setOriginalTransform(AffineTransform originalTransform) {
		this.originalTransform = originalTransform;
	}

	/**
	 * x coord of mouse cursor relative to the player.
	 * @return
	 */
	public float getRelativeMouseX() {
		return relativeMouseX;
	}

	/**
	 * y coord of mouse cursor relative to the player.
	 * @return
	 */
	public float getRelativeMouseY() {
		return relativeMouseY;
	}

	/**
	 * x coord of mouse cursor relative to the map.
	 * @return
	 */
	public float getWorldMouseX() {
		return player.getX() + relativeMouseX;
	}

	/**
	 * y coord of mouse cursor relative to the map.
	 * @return
	 */
	public float getWorldMouseY() {
		return player.getY() + relativeMouseY;
	}

	public boolean isShowMapDescriptions() {
		return showMapDescriptions;
	}

	public void setShowMapDescriptions(boolean showMapDescriptions) {
		this.showMapDescriptions = showMapDescriptions;
	}

	public GameWorld getWorld() {
		return controller.getWorld();
	}

	public StatusMenu getStatusMenu() {
		return statusMenu;
	}

	public static boolean isSHOW_STATS() {
		return SHOW_STATS;
	}

	public static void setSHOW_STATS(boolean SHOW_STATS) {
		ViewPane.SHOW_STATS = SHOW_STATS;
	}

	public BBox getViewRectInWorldCoords() {
		return viewRectInWorldCoords;
	}
	public Point2D.Float getViewCenterInWorldCoords() {
		return viewCenterInWorldCoords;
	}

	public float getRelativeMouseXNow() {
		return relativeMouseXNow;
	}

	public void setRelativeMouseXNow(float relativeMouseXNow) {
		this.relativeMouseXNow = relativeMouseXNow;
	}

	public float getRelativeMouseYNow() {
		return relativeMouseYNow;
	}

	public void setRelativeMouseYNow(float relativeMouseYNow) {
		this.relativeMouseYNow = relativeMouseYNow;
	}
}
