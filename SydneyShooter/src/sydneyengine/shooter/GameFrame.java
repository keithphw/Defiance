/*
 * GameFrame.java
 *
 * Created on 12 November 2007, 18:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine.shooter;

import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import sydneyengine.ClientController;
import sydneyengine.ConnectionWelcomer;
import sydneyengine.Controller;
import sydneyengine.EventStoreClient;
import sydneyengine.EventStoreServer;
import sydneyengine.EventWrapper;
import sydneyengine.GameConstants;
import sydneyengine.LatencyInfo;
import sydneyengine.LatencyPostCard;
import sydneyengine.MessagePack;
import sydneyengine.Nexus;
import sydneyengine.ReceiverPollingServer;
import sydneyengine.Sender;
import sydneyengine.SenderLagSimulator;
import sydneyengine.ServerController;
import sydneyengine.lobby.LobbyClient;
import sydneyengine.network.Address;
import sydneyengine.network.ByteClientMina;
import sydneyengine.network.ConnectionServer;
import sydneyengine.network.ConnectionServerMina;
import sydneyengine.superserializable.ArrayListSS;
import sydneyengine.superserializable.FieldCache;
import sydneyengine.superserializable.SSCodeAllocator;
import sydneyengine.superserializable.SSObject;
import sydneyengine.superserializable.WeakSSObjectMap;
import sydneyengine.ui.ControlsPane;
import sydneyengine.ui.GameDesktopPane;
import sydneyengine.ui.HelpPane;
import sydneyengine.ui.JoinPane;
import sydneyengine.ui.LookAndFeelChooser;
import sydneyengine.ui.MenuPane;
import sydneyengine.ui.StartPane;
import sydneyengine.ui.StatusMenu;
import sydneyengine.ui.TeamsAndScoresPane;

/**
 *
 * @author CommanderKeith
 */
public class GameFrame extends JFrame implements GameConstants {
	
	protected static boolean substanceLnFExists = false;

	static {
		// this is not needed when running from the jar, but it is needed when running from webstart.
		setupSSToolsInstalledClasses();
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		// The below just sets the look and feel for the Swing GUI.  If Substance is there, it uses it, otherwise it defaults to the System L&F.
		//JFrame.setDefaultLookAndFeelDecorated(true);
		//JDialog.setDefaultLookAndFeelDecorated(true);
		//String lookAndFeelClassName = "";	// this will make it so that the system LnF is used.
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceMistSilverLookAndFeel";
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceGreenMagicLookAndFeel";
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceChallengerDeepLookAndFeel";
		String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceRavenGraphiteGlassLookAndFeel";
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceCremeLookAndFeel";
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel";
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceBusinessBlueSteelLookAndFeel";
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceMagmaLookAndFeel";
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceMangoLookAndFeel";
		//String lookAndFeelClassName = "org.jvnet.substance.skin.SubstanceEmeraldDuskLookAndFeel";
		//org.jvnet.substance.SubstanceLookAndFeel substanceLookAndFeel = new org.jvnet.substance.SubstanceLookAndFeel();
		try {
			UIManager.setLookAndFeel(lookAndFeelClassName);
			substanceLnFExists = true;
		} catch (Exception ex) {
			// if the defaultLookAndFeelClassName can't be found, use the system look and feel.  This will occur for example when this app is not bundled with Substance LnF but defaultLookAndFeelClassName = "org.jvnet.substance.skin.SubstanceCremeLookAndFeel";.
			lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(lookAndFeelClassName);
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}
		// Print some stuff:
		System.out.println(GameFrame.class.getSimpleName() + ": Runtime.getRuntime().availableProcessors() == " + Runtime.getRuntime().availableProcessors());
		System.out.println(GameFrame.class.getSimpleName() + ": Runtime.getRuntime().maxMemory() == " + Runtime.getRuntime().maxMemory());
		System.out.println(System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version"));
	}
	protected GameDesktopPane desktopPane;
	protected ViewPane viewPane;
	String title = "SydneyEngine v0.2";
	protected Controller currentController;
	int waitToCloseMillis = 250;
	protected int portNumTCP = DEFAULT_PORT_TCP;
	protected int portNumUDP = DEFAULT_PORT_UDP;
	protected InetAddress localHost = null;
	protected String localHostNameString = null;
	protected String localHostIPString = null;
	String serverHostNameString = null;
	String serverHostIPString = null;
	protected Preferences userPrefs = Preferences.userRoot().node(title);
	LobbyClient lobbyClient;
	protected String playerName;
	static GameFrame staticGameFrame = null;
	// This method should only be used for debugging.
	public static GameFrame getStaticGameFrame() {
		return staticGameFrame;
	}

	public static void main(String[] args) {
		new GameFrame();
	}
	
	/** Creates a new instance of GameFrame */
	public GameFrame() {
		staticGameFrame = this;
		lobbyClient = new LobbyClient();
		lobbyClient.start();
		
		playerName = this.getFromPrefs(PLAYER_NAME);
		if (playerName == null) {
			playerName = (Math.random() > 0.5 ? "Soldier" : "Digger");
		}
		// Setup the window
		//Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		//setSize(1000, 800);
		//setSize(700, 700);
		
		
		//setSize((int)(screen.getWidth()*5/6), (int)(screen.getHeight()*5/6));
		//setLocationRelativeTo(null);

		
		
		
		
		GraphicsEnvironment env = GraphicsEnvironment.
            getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
		GraphicsDevice device = devices[0];
		boolean isFullScreen = device.isFullScreenSupported();
        if (false && isFullScreen) {
            // Full-screen mode
			JFrame.setDefaultLookAndFeelDecorated(false);
			JDialog.setDefaultLookAndFeelDecorated(true);
			setUndecorated(true);
			setResizable(false);
			
            device.setFullScreenWindow(this);
			device.setDisplayMode(new DisplayMode(device.getFullScreenWindow().getWidth(), 
					device.getFullScreenWindow().getHeight(), 32, DisplayMode.REFRESH_RATE_UNKNOWN));
            validate();
        } else {
            // Windowed mode
            setSize(900, 600);
            setLocationRelativeTo(null);
		}
		
		
		
		
		// Let the viewPane grab the focus when this JFrame is made visible.
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				/*if (getController() != null && getController().getViewPane() != null) {
					getController().getViewPane().requestFocus();
				}*/
			}

			@Override
			public void windowClosing(WindowEvent e) {
				close();
				
			}
		});
		localHost = null;
		try {
			localHost = InetAddress.getLocalHost();
			localHostNameString = localHost.getHostName();
			localHostIPString = localHost.getHostAddress();
		} catch (UnknownHostException ex) {
			System.out.println(this.getClass().getSimpleName() + ": Can't get local host");
			ex.printStackTrace();
		}
		final JPanel bigPane = new JPanel(new BorderLayout());
		bigPane.setOpaque(false);
		setContentPane(bigPane);
		this.getRootPane().setOpaque(false);

		desktopPane = new GameDesktopPane();
		//desktop.putClientProperty("JDesktopPane.dragMode", "outline");
		getContentPane().add(desktopPane, BorderLayout.CENTER);

		doStart();

		setVisible(true);
		((JComponent) getContentPane()).revalidate();
		repaint();
	}

	public void doStart() {
		System.out.println("------------------> "+ this.getClass().getSimpleName() + ": doStart() method started");
		// Since this method closes the existing game, we tell the central server that 
		// our old hosted game has ended, even though there may not have been a game before.
		try {
			lobbyClient.sendNotificationOfExitedHostedGame();
		} catch (IOException e) {
			System.out.println("Exception caught:");
			e.printStackTrace();
			// this isn't anything to worry about, just says there wasn't a previous game...
		}

		if (this.getController() != null) {
			this.getController().closeAndWait(waitToCloseMillis);
		}

		serverHostNameString = this.localHostNameString;
		serverHostIPString = this.localHostIPString;

		Player player = new Player();
		player.setName(getPlayerName());

		GameWorld world = new GameWorld();
		EventStoreServer eventStore = new EventStoreServer();
		world.setEventStore(eventStore);
		GameWorld tailWorld = null;

		tailWorld = (GameWorld) world.deepClone(new FieldCache(), new WeakSSObjectMap<SSObject, Object>());

		world.setTwin(tailWorld);
		world.setHead(true);
		tailWorld.setTwin(world);
		tailWorld.setHead(false);
		player.setWorld(world);

		final ServerController controller = new ServerController(this, player, world, eventStore);
		controller.setSleepBetweenUpdatesMillis(15);
		setController(controller);
		world.setController(controller);
		tailWorld.setController(controller);
		eventStore.setController(controller);

		ViewPane newViewPane = new ViewPane(this, controller);
		setViewPane(newViewPane);
		player.setViewPane(newViewPane);
		newViewPane.setPlayer(player);
		controller.setViewPane(newViewPane);


		PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player);
		playerJoinEvent.setTimeStamp(0);
		eventStore.addEventFromViewPane(playerJoinEvent.getEventWrapper());

		Thread gameThread;
		gameThread = new Thread(controller);
		gameThread.setName("Controller " + gameThread.getName());

		setTitle(title);
		desktopPane.removeAll();
		desktopPane.setMainComponent(newViewPane);	// this is equivalent to desktopPane.add(split, JDesktopPane.DEFAULT_LAYER);

		JInternalFrame internalFrame = null;
		//Create the internal frame if necessary.
		if (internalFrame == null || internalFrame.isClosed()) {
			internalFrame = new JInternalFrame("Start Menu");
			internalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			internalFrame.setLayout(new BorderLayout());
			StartPane startPane = new StartPane(newViewPane, internalFrame);
			internalFrame.setContentPane(startPane);

			internalFrame.setResizable(true);
			internalFrame.setIconifiable(false);
			internalFrame.setMaximizable(false);
			internalFrame.setClosable(false);
			internalFrame.pack();
			internalFrame.setLocation((getWidth() - internalFrame.getWidth()) / 2, (getHeight() - internalFrame.getHeight()) / 2);
			//And don't forget to add it to the desktop pane!
			desktopPane.add(internalFrame, JLayeredPane.MODAL_LAYER);
			internalFrame.setVisible(true);
		}

		gameThread.start();
	}
	// Convenience method
	public void doJoinGame(boolean internetGame, String ipString) throws IOException {
		InetAddress ip = null;
		try {
			try {
				ip = Address.getByAddress(ipString);
			} catch (Exception ex) {
				ip = InetAddress.getByName(ipString);
			}
		} catch (java.io.IOException ex) {
			throw ex;
		}
		InetSocketAddress addr = new InetSocketAddress(ip, portNumTCP);
		doJoinGame(internetGame, addr);
	}

	/**
	 * Joins an existing game using lag-tolerant internet game settings from GameConstants or high-performance LAN game settings, depending on whether intenetGame is true or false.
	 * 
	 * @param internetGame if true uses internet game settings from GameConstants which have a high lag tolerance, if false uses high-preformance LAN game settings.
	 */
	public void doJoinGame(boolean internetGame, InetSocketAddress addr) throws IOException {
		System.out.println(this.getClass().getSimpleName() + ": doJoinGame method");
		// make the client
		final Nexus nexus;
		ByteClientMina byteClientMina = new ByteClientMina();
		try {
			byteClientMina.connect(addr);
			serverHostNameString = addr.getHostName();
			serverHostIPString = addr.getAddress().getHostAddress();
		} catch (java.io.IOException ex) {
			throw ex;
		}
		nexus = new Nexus(byteClientMina);
		
		
		// To simulate a clock difference as if this client and the server are on 
		// different computers when they are actually on the same computer and there 
		// is no clock difference.
		//MockSystem.setClockIncrementNanos(1000000000L);

		

		EventStoreClient eventStore = new EventStoreClient();
		ClientController controller = new ClientController(this, null, nexus, eventStore);
		// The below can be commented out if you don't want the client to queue sends on a different thread,
		// and the client will just send stuff straight away, pausing the current thread.
		Sender sender = new SenderLagSimulator(controller);
		nexus.setController(controller);
		sender.start();

		// The below encode communicates with the ConnectionWelcomerImpl class.
		// Get the VM encode that we're meant to use.  This will be a unique number that the server and no other clients will have.  
		//It is needed for the SS streams so new SSObjects created on this VM will not have the same SS encode as those 
		// constructed on other VM's.
		int newClientVMCode = -1;
		System.out.println(this.getClass().getSimpleName() + ": about to try to recieve newClientVMCode");
		MessagePack messagePack = null;
		int maxMillisToLoop = 10000;	 //10 seconds = maximum waiting time, aborts if longer
		long timeAtStartOfLoop = System.currentTimeMillis();
		while (true) {
			try {
				messagePack = nexus.recieve();
			} catch (IOException e) {
				e.printStackTrace();
				controller.closeAndWait(250);
				return;
			}
			if (messagePack != null) {
				break;
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
				}
				if (System.currentTimeMillis() - timeAtStartOfLoop > maxMillisToLoop) {
					System.err.println(this.getClass().getSimpleName() + ": returning from doJoin method after not receiving a response from the server. Thread.dumpStack(): ");
					Thread.dumpStack();
					controller.closeAndWait(250);
					return;
				}
			}
		}
		// Need to call constructObject() since as it is messagePack.getObject() will return null.
		try {
			messagePack.constructObject(controller.getSSIn());
		} catch (IOException e) {
			e.printStackTrace();
			controller.closeAndWait(250);
			return;
		}
		newClientVMCode = ((Integer) messagePack.getObject()).intValue();
		System.out.println(this.getClass().getSimpleName() + ": recieved newClientVMCode == " + newClientVMCode);
		SSCodeAllocator.setVMCode(newClientVMCode);

		Player player = new Player();
		player.setName(getPlayerName());
		controller.setPlayer(player);
		nexus.setPlayer(player);

		System.out.println(this.getClass().getSimpleName() + ": sending player, which == " + player);
		assert player.getWorld() == null : "player.getWorld() must == null since the client isn't meant"
				+ " to have the world until the server sends it, and the client should never tell"
				+ " the server what its world should be. player.getWorld() == "+player.getWorld();
		
		try {
			nexus.send(-1, player, controller.getSSOut());
		} catch (IOException ex) {
			ex.printStackTrace();
			controller.closeAndWait(250);
			return;
		}
		System.out.println(this.getClass().getSimpleName() + ": sent player");

		System.out.println(this.getClass().getSimpleName() + ": calculating server clock diff");
		for (int i = 0; i < NUM_CLOCK_SYNCS; i++) {
			messagePack = null;
			timeAtStartOfLoop = System.currentTimeMillis();
			while (true) {
				try {
					messagePack = nexus.recieve();
				} catch (IOException e) {
					e.printStackTrace();
					controller.closeAndWait(250);
					return;
				}
				if (messagePack != null) {
					break;
				} else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ex) {
					}
					if (System.currentTimeMillis() - timeAtStartOfLoop > maxMillisToLoop) {
						System.err.println(this.getClass().getSimpleName() + ": returning from doJoin method after not receiving a response from the server. Thread.dumpStack(): ");
						Thread.dumpStack();
						controller.closeAndWait(250);
						return;
					}
				}
			}
			// Need to call constructObject() otherwise messagePack.getObject() will return null.
			try {
				messagePack.constructObject(controller.getSSIn());
			} catch (IOException e) {
				e.printStackTrace();
				doStart();
			}
			assert messagePack.getType() == Nexus.LATENCY_REQUEST;
			assert messagePack.getObject() != null;

			try {
				nexus.getLatencyCalculator().respondToLatencyRequest((LatencyPostCard) messagePack.getObject());
			} catch (IOException e) {
				e.printStackTrace();
				doStart();
			}
		}
		// here we get the latency results:
		messagePack = null;
		timeAtStartOfLoop = System.currentTimeMillis();
		while (true) {
			try {
				messagePack = nexus.recieve();
			} catch (IOException e) {
				e.printStackTrace();
				controller.closeAndWait(250);
				return;
			}
			if (messagePack != null) {
				break;
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
				}
				if (System.currentTimeMillis() - timeAtStartOfLoop > maxMillisToLoop) {
					System.err.println(this.getClass().getSimpleName() + ": returning from doJoin method after not receiving a response from the server. Thread.dumpStack(): ");
					Thread.dumpStack();
					controller.closeAndWait(250);
					return;
				}
			}
		}
		// Need to call constructObject() otherwise messagePack.getObject() will return null.
		try {
			messagePack.constructObject(controller.getSSIn());
		} catch (IOException e) {
			e.printStackTrace();
			doStart();
		}
		assert messagePack.getType() == Nexus.LATENCY_RESULTS;
		assert messagePack.getObject() != null;
		nexus.getLatencyCalculator().setLatencyInfo((LatencyInfo) messagePack.getObject());
		System.out.println(this.getClass().getSimpleName() + ": finished calculating server clock diff, nexus.getLatencyCalculator().getServerClockDiffNanos() == " + nexus.getLatencyCalculator().getServerClockDiffNanos() + ", nexus.getLatencyCalculator().getLatencyToServerNanos() == " + nexus.getLatencyCalculator().getLatencyToServerNanos());

		// get the first gameWorld
		System.out.println(this.getClass().getSimpleName() + ": about to try to recieve first world!");
		messagePack = null;
		timeAtStartOfLoop = System.currentTimeMillis();
		while (true) {
			try {
				messagePack = nexus.recieve();
			} catch (IOException e) {
				e.printStackTrace();
				controller.closeAndWait(250);
				return;
			}
			if (messagePack != null) {
				break;
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
					
				}
				if (System.currentTimeMillis() - timeAtStartOfLoop > maxMillisToLoop) {
					System.err.println(this.getClass().getSimpleName() + ": returning from doJoin method after not receiving a response from the server. Thread.dumpStack(): ");
					Thread.dumpStack();
					controller.closeAndWait(250);
					return;
				}
			}
		}
		try {
			messagePack.constructObject(controller.getSSIn());
		} catch (IOException e) {
			e.printStackTrace();
			controller.closeAndWait(250);
			return;
		}
		final GameWorld world = (GameWorld) messagePack.getObject();
		System.out.println(this.getClass().getSimpleName() + ": world recieved from server! world == "+world);
		assert world != null;
		GameWorld tailWorld = (GameWorld) world.deepClone(new FieldCache(), new WeakSSObjectMap<SSObject, Object>());
		world.setController(controller);
		tailWorld.setController(controller);
		world.setEventStore(eventStore);
		eventStore.setController(controller);
		world.setTwin(tailWorld);
		world.setHead(true);
		tailWorld.setTwin(world);
		tailWorld.setHead(false);
		player.setWorld(world);
		controller.setWorld(world);


		// get the old events
		System.out.println(this.getClass().getSimpleName() + ": about to try to recieve the old events!");
		messagePack = null;
		timeAtStartOfLoop = System.currentTimeMillis();
		while (true) {
			try {
				messagePack = nexus.recieve();
			} catch (IOException e) {
				e.printStackTrace();
				controller.closeAndWait(250);
				return;
			}
			if (messagePack != null) {
				break;
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
				}
				if (System.currentTimeMillis() - timeAtStartOfLoop > maxMillisToLoop) {
					System.err.println(this.getClass().getSimpleName() + ": returning from doJoin method after not receiving a response from the server. Thread.dumpStack(): ");
					Thread.dumpStack();
					controller.closeAndWait(250);
					return;
				}
			}
		}
		try {
			messagePack.constructObject(controller.getSSIn());
		} catch (IOException e) {
			e.printStackTrace();
			controller.closeAndWait(250);
			return;
		}
		ArrayListSS<EventWrapper> oldEvents = (ArrayListSS<EventWrapper>) messagePack.getObject();
		System.out.println(this.getClass().getSimpleName() + ": oldEvents recieved from server! oldEvents.size() == " + oldEvents.size());
		eventStore.setEvents(oldEvents);

		if (internetGame) {
			controller.setSleepBetweenUpdatesMillis(CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS);
			world.setDoMoveBetweenEventsIfTimeStampsEqual(WORLD_DO_MOVE_BETWEEN_EVENTS_IF_TIME_STAMPS_EQUAL);
			world.setEventTimeStampMultipleSeconds(WORLD_EVENT_TIME_STAMP_MULTIPLE_SECONDS);
			world.setMaxUpdateElapsedSeconds(WORLD_MAX_UPDATE_ELAPSED_SECONDS);
			world.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_INTERNET);
			world.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_INTERNET);
			tailWorld.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_INTERNET);
			tailWorld.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_INTERNET);
			eventStore.setMinSecondsToKeepUserEvents(EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_INTERNET);
		} else { // LAN game
			controller.setSleepBetweenUpdatesMillis(CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS);
			world.setDoMoveBetweenEventsIfTimeStampsEqual(WORLD_DO_MOVE_BETWEEN_EVENTS_IF_TIME_STAMPS_EQUAL);
			world.setEventTimeStampMultipleSeconds(WORLD_EVENT_TIME_STAMP_MULTIPLE_SECONDS);
			world.setMaxUpdateElapsedSeconds(WORLD_MAX_UPDATE_ELAPSED_SECONDS);
			world.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_LAN);
			world.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_LAN);
			tailWorld.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_LAN);
			tailWorld.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_LAN);
			eventStore.setMinSecondsToKeepUserEvents(EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_LAN);
		}

		if (this.getController() != null) {
			this.getController().closeAndWait(waitToCloseMillis);
		}
		this.getDesktopPane().removeAll();
		setController(controller);
		final ViewPane newViewPane = new ViewPane(this, controller);
		player.setViewPane(newViewPane);
		newViewPane.setPlayer(player);
		controller.setViewPane(newViewPane);
		setViewPane(newViewPane);

		Thread gameThread = new Thread(controller);
		gameThread.setName("ClientController " + gameThread.getName());

		System.out.println(this.getClass().getSimpleName() + ": done clientController");

		JPanel somePane = getContainerFor(newViewPane);
		desktopPane.removeAllNonMainComponents();
		desktopPane.setMainComponent(somePane);	// this is equivalent to desktopPane.add(split, JDesktopPane.DEFAULT_LAYER);
		somePane.revalidate();

		gameThread.start();
		newViewPane.requestFocus();
		/*
		// start the latencyChecker thread which is designed to keep a track of the latency between the server and all clients.
		// This should be done at the end of all of the direct calls to nexus.recieve since it will interfere with that.
		nexus.getLatencyCalculator().startLatencyChecker();
		*/
	}
	public JPanel getContainerFor(final ViewPane viewPane) {
		JPanel somePane = new JPanel(new BorderLayout());
		somePane.add(viewPane, BorderLayout.CENTER);
		StatusMenu statusMenu = viewPane.getStatusMenu();
		JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.add(statusMenu);
		somePane.add(toolBar, BorderLayout.SOUTH);
		somePane.setOpaque(true);
		//somePane.setBackground(Color.pink);
		return somePane;
	}

	public void doJoinMenu(ViewPane viewPane) {
		desktopPane.removeAllNonMainComponents();

		JInternalFrame internalFrame = null;
		//Create the internal frame if necessary.
		if (internalFrame == null || internalFrame.isClosed()) {
			internalFrame = new JInternalFrame("Join Menu");
			internalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			internalFrame.setLayout(new BorderLayout());
			JoinPane joinPane = new JoinPane(viewPane);
			internalFrame.setContentPane(joinPane);

			internalFrame.setResizable(true);
			internalFrame.setIconifiable(false);
			internalFrame.setMaximizable(false);
			internalFrame.setClosable(false);
			internalFrame.pack();
			internalFrame.setLocation((this.getWidth() - internalFrame.getWidth()) / 2, (this.getHeight() - internalFrame.getHeight()) / 2);
			//And we mustn't forget to add it to the desktop pane!
			desktopPane.add(internalFrame, JLayeredPane.MODAL_LAYER);
			internalFrame.setVisible(true);
		}
	}

	public void doOptionsMenu(final ViewPane viewPane) {
		JInternalFrame internalFrame = null;
		//Create the internal frame if necessary.
		if (internalFrame == null || internalFrame.isClosed()) {
			internalFrame = new JInternalFrame("Options Menu");
			internalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			internalFrame.setLayout(new BorderLayout());
			ControlsPane startPane = new ControlsPane(viewPane, internalFrame);
			internalFrame.setContentPane(startPane);

			internalFrame.setResizable(true);
			internalFrame.setIconifiable(false);
			internalFrame.setMaximizable(false);
			internalFrame.setClosable(true);
			internalFrame.pack();
			int xLoc = (this.getWidth() - internalFrame.getWidth()) / 2;
			int yLoc = (this.getHeight() - internalFrame.getHeight()) / 2;
			if (yLoc < 0) {
				yLoc = 0;
			}
			internalFrame.setLocation(xLoc, yLoc);
			//And we mustn't forget to add it to the desktop pane!
			desktopPane.add(internalFrame, JLayeredPane.MODAL_LAYER);
			internalFrame.setVisible(true);
		}
	}

	public void doGameMenu(ViewPane viewPane) {
		JInternalFrame internalFrame = null;
		//Create the internal frame if necessary.
		if (internalFrame == null || internalFrame.isClosed()) {
			internalFrame = new JInternalFrame("Game Menu");
			internalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			internalFrame.setLayout(new BorderLayout());
			MenuPane startPane = new MenuPane(viewPane, internalFrame);
			internalFrame.setContentPane(startPane);

			internalFrame.setResizable(true);
			internalFrame.setIconifiable(false);
			internalFrame.setMaximizable(false);
			internalFrame.setClosable(true);
			internalFrame.pack();
			int xLoc = (this.getWidth() - internalFrame.getWidth()) / 2;
			int yLoc = (this.getHeight() - internalFrame.getHeight()) / 2;
			if (yLoc < 0) {
				yLoc = 0;
			}
			internalFrame.setLocation(xLoc, yLoc);
			//And we mustn't forget to add it to the desktop pane!
			desktopPane.add(internalFrame, JLayeredPane.MODAL_LAYER);
			internalFrame.setVisible(true);
		}
	}

	public void doHelpMenu(ViewPane viewPane) {
		JInternalFrame internalFrame = null;
		//Create the internal frame if necessary.
		if (internalFrame == null || internalFrame.isClosed()) {
			internalFrame = new JInternalFrame("Help Menu");
			internalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			internalFrame.setLayout(new BorderLayout());
			HelpPane startPane = new HelpPane(viewPane, internalFrame);
			internalFrame.setContentPane(startPane);

			internalFrame.setResizable(true);
			internalFrame.setIconifiable(false);
			internalFrame.setMaximizable(false);
			internalFrame.setClosable(true);
			internalFrame.pack();
			int xLoc = (this.getWidth() - internalFrame.getWidth()) / 2;
			int yLoc = (this.getHeight() - internalFrame.getHeight()) / 2;
			if (yLoc < 0) {
				yLoc = 0;
			}
			internalFrame.setLocation(xLoc, yLoc);
			//And we mustn't forget to add it to the desktop pane!
			desktopPane.add(internalFrame, JLayeredPane.MODAL_LAYER);
			internalFrame.setVisible(true);
		}
	}

	public void doTeamsAndScoresMenu(ViewPane viewPane) {
		JInternalFrame internalFrame = null;
		//Create the internal frame if necessary.
		if (internalFrame == null || internalFrame.isClosed()) {
			internalFrame = new JInternalFrame("Teams and Scores Menu");
			internalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			internalFrame.setLayout(new BorderLayout());
			TeamsAndScoresPane startPane = new TeamsAndScoresPane(viewPane, internalFrame);
			internalFrame.setContentPane(startPane);

			internalFrame.setResizable(true);
			internalFrame.setIconifiable(false);
			internalFrame.setMaximizable(false);
			internalFrame.setClosable(true);
			internalFrame.pack();
			int xLoc = (this.getWidth() - internalFrame.getWidth()) / 2;
			int yLoc = (this.getHeight() - internalFrame.getHeight()) / 2;
			if (yLoc < 0) {
				yLoc = 0;
			}
			internalFrame.setLocation(xLoc, yLoc);
			//And we mustn't forget to add it to the desktop pane!
			desktopPane.add(internalFrame, JLayeredPane.MODAL_LAYER);
			internalFrame.setVisible(true);
		}
	}

	/**
	 * Creates (hosts) a new game using lag-tolerant internet game settings from GameConstants or high-performance LAN game settings, depending on whether intenetGame is true or false.
	 * 
	 * @param internetGame if true uses internet game settings from GameConstants which have a high lag tolerance, if false uses high-preformance LAN game settings.
	 */
	public void doCreate(boolean internetGame) {
		System.out.println(this.getClass().getSimpleName() + ": doCreate method");
		serverHostNameString = this.localHostNameString;
		serverHostIPString = this.localHostIPString;
		
		ConnectionServer connectionServer = null;
		try {
			connectionServer = new ConnectionServerMina();
			connectionServer.bindAndListen(portNumTCP);
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		Player player = new Player();
		player.setName(getPlayerName());

		GameWorld world = new GameWorld();
		EventStoreServer eventStore = new EventStoreServer();
		world.setEventStore(eventStore);
		GameWorld tailWorld = null;

		tailWorld = (GameWorld) world.deepClone(new FieldCache(), new WeakSSObjectMap<SSObject, Object>());

		world.setTwin(tailWorld);
		world.setHead(true);
		tailWorld.setTwin(world);
		tailWorld.setHead(false);

		assert world.headAndTailWorldObjectsNotMixed();

		final ServerController controller = new ServerController(this, player, world, eventStore);
		world.setController(controller);
		tailWorld.setController(controller);
		Sender sender = new SenderLagSimulator(controller);
		sender.start();
		eventStore.setController(controller);

		if (internetGame) {
			controller.setSleepBetweenUpdatesMillis(CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS);
			controller.setMinTimeBetweenClientUpdatesNanos(SERVER_CONTROLLER_MIN_TIME_BETWEEN_CLIENT_UPDATES_NANOS_INTERNET);
			world.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_INTERNET);
			world.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_INTERNET);
			tailWorld.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_INTERNET);
			tailWorld.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_INTERNET);
			eventStore.setMinSecondsToKeepUserEvents(EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_INTERNET);
		} else { // LAN game
			controller.setSleepBetweenUpdatesMillis(CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS);
			controller.setMinTimeBetweenClientUpdatesNanos(SERVER_CONTROLLER_MIN_TIME_BETWEEN_CLIENT_UPDATES_NANOS_LAN);
			world.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_LAN);
			world.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_LAN);
			tailWorld.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_LAN);
			tailWorld.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_LAN);
			eventStore.setMinSecondsToKeepUserEvents(EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_LAN);
		}

		PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player);
		playerJoinEvent.setTimeStamp(0);
		eventStore.addEventFromViewPane(playerJoinEvent.getEventWrapper());

		ViewPane newViewPane = new ViewPane(this, controller);
		setViewPane(newViewPane);
		player.setViewPane(newViewPane);
		newViewPane.setPlayer(player);
		controller.setViewPane(newViewPane);

		ReceiverPollingServer receiver = new ReceiverPollingServer(controller);
		controller.setReceiver(receiver);
		receiver.start();


		// start the latencyChecker thread which is designed to keep a track of the latency between the server and all clients.
		// Note that it will never affect the nexus.getLatencyInfo()'s serverClockDiff, only its latency.
		// This should be done at the end of all of the direct calls to nexus.recieve since it will interfere with that.

		Thread gameThread;
		gameThread = new Thread(controller);
		gameThread.setName("ServerController " + gameThread.getName());
		JPanel viewPanel = new JPanel(new BorderLayout());
		viewPanel.add(newViewPane);

		if (this.getController() != null) {
			this.getController().closeAndWait(waitToCloseMillis);
		}
		setController(controller);
		ConnectionWelcomer connectionWelcomer = new ConnectionWelcomer(controller);
		connectionServer.setConnectionServerListener(connectionWelcomer);
		controller.setConnectionServer(connectionServer);
		controller.setConnectionServer(connectionServer);
		/*ConnectionListener connectionListener = new ConnectionListener(controller, connectionServer, connectionWelcomer);
		controller.setConnectionListener(connectionListener);*/

		//setTitle(title);
		this.getDesktopPane().removeAll();
		JPanel somePane = getContainerFor(newViewPane);
		desktopPane.setMainComponent(somePane);	// this is equivalent to desktopPane.add(split, JDesktopPane.DEFAULT_LAYER);
		somePane.revalidate();
		gameThread.start();
		//connectionListener.startTakingConnections();
		newViewPane.requestFocus();
		if (internetGame) {
			try {
				lobbyClient.sendNotificationOfNewHostedGame();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void doCreateSinglePlayer(boolean internetGame) {
		System.out.println(this.getClass().getSimpleName() + ": doCreate method");
		serverHostNameString = this.localHostNameString;
		serverHostIPString = this.localHostIPString;
		
		ConnectionServer connectionServer = null;
		try {
			connectionServer = new ConnectionServerMina();
			connectionServer.bindAndListen(portNumTCP);
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		Player player = new Player();
		player.setName(getPlayerName());

		GameWorld world = new GameWorld();
		EventStoreServer eventStore = new EventStoreServer();
		world.setEventStore(eventStore);
		GameWorld tailWorld = null;

		tailWorld = (GameWorld) world.deepClone(new FieldCache(), new WeakSSObjectMap<SSObject, Object>());

		world.setTwin(tailWorld);
		world.setHead(true);
		tailWorld.setTwin(world);
		tailWorld.setHead(false);

		assert world.headAndTailWorldObjectsNotMixed();

		final ServerController controller = new ServerController(this, player, world, eventStore);
		world.setController(controller);
		tailWorld.setController(controller);
		Sender sender = new SenderLagSimulator(controller);
		sender.start();
		eventStore.setController(controller);

		if (internetGame) {
			controller.setSleepBetweenUpdatesMillis(CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS);
			controller.setMinTimeBetweenClientUpdatesNanos(SERVER_CONTROLLER_MIN_TIME_BETWEEN_CLIENT_UPDATES_NANOS_INTERNET);
			world.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_INTERNET);
			world.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_INTERNET);
			tailWorld.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_INTERNET);
			tailWorld.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_INTERNET);
			eventStore.setMinSecondsToKeepUserEvents(EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_INTERNET);
		} else { // LAN game
			controller.setSleepBetweenUpdatesMillis(CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS);
			controller.setMinTimeBetweenClientUpdatesNanos(SERVER_CONTROLLER_MIN_TIME_BETWEEN_CLIENT_UPDATES_NANOS_LAN);
			world.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_LAN);
			world.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_LAN);
			tailWorld.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_LAN);
			tailWorld.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_LAN);
			eventStore.setMinSecondsToKeepUserEvents(EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_LAN);
		}

		PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(player);
		playerJoinEvent.setTimeStamp(0);
		eventStore.addEventFromViewPane(playerJoinEvent.getEventWrapper());

		ViewPane newViewPane = new ViewPane(this, controller);
		setViewPane(newViewPane);
		player.setViewPane(newViewPane);
		newViewPane.setPlayer(player);
		controller.setViewPane(newViewPane);

		ReceiverPollingServer receiver = new ReceiverPollingServer(controller);
		controller.setReceiver(receiver);
		receiver.start();


		// start the latencyChecker thread which is designed to keep a track of the latency between the server and all clients.
		// Note that it will never affect the nexus.getLatencyInfo()'s serverClockDiff, only its latency.
		// This should be done at the end of all of the direct calls to nexus.recieve since it will interfere with that.

		Thread gameThread;
		gameThread = new Thread(controller);
		gameThread.setName("ServerController " + gameThread.getName());
		JPanel viewPanel = new JPanel(new BorderLayout());
		viewPanel.add(newViewPane);

		if (this.getController() != null) {
			this.getController().closeAndWait(waitToCloseMillis);
		}
		setController(controller);
		ConnectionWelcomer connectionWelcomer = new ConnectionWelcomer(controller);
		connectionServer.setConnectionServerListener(connectionWelcomer);
		controller.setConnectionServer(connectionServer);

		//setTitle(title);
		this.getDesktopPane().removeAll();
		JPanel somePane = getContainerFor(newViewPane);
		desktopPane.setMainComponent(somePane);	// this is equivalent to desktopPane.add(split, JDesktopPane.DEFAULT_LAYER);
		somePane.revalidate();
		gameThread.start();
		newViewPane.requestFocus();
		if (internetGame) {
			try {
				lobbyClient.sendNotificationOfNewHostedGame();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// This method installs all of the classes to the SSTools class, which in turn means that all
	// SSStreams will be born with these classes pre-installed.
	// Normally you don't have to do this since the SSTools class takes care of it by default
	// by looking up all of the classes, but for some reason this doesn't work with WebStart so
	// I'm doing it this manual way.  This manual way is also a bit quicker since it avoids
	// taking the time to look for all of the SSClasses.
	public static void setupSSToolsInstalledClasses() {
//		ArrayList<Class> ssClassesToPreInstall = new ArrayList();
//		try {
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.AbstractEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.ClientWorldUpdate"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.EventWrapper"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.LatencyInfo"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.LatencyPostCard"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.lobby.HostedGame"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.lobby.LobbyInfo"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.RewindableWorld"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.BallisticRocket"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.Bot"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.Bullet"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.CapturableFlag"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.ChangeTeamEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.ChatText"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.ChatTextEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.FlameBall"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.FlameThrower"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.GameEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.GameWorld"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.Gun"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.HealthPack"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.InvisibilityShroud"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.ItemHolder"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.ItemSpawner"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.KPolygon"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.MachineGun"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.MachineGunBullet"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.Obstacle"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.Pistol"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.PistolBullet"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.Player"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.PlayerEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.PlayerJoinEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.PlayerKeyEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.PlayerMouseEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.PlayerMouseWheelEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.RemovePlayerEvent"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.RocketLauncher"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.ShotGun"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.ShotGunPellet"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.SniperRifle"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.SniperRifleBullet"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.SpawnFlag"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.StunDart"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.Team"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.TranquilizerGun"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.shooter.Water"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.superserializable.ArrayListSS"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.superserializable.SSAdapter"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.superserializable.SSTester"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.superserializable.TestDad"));
//			ssClassesToPreInstall.add(Class.forName("sydneyengine.superserializable.TestSon"));
//
//
//		}catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		SSTools.setSSClassesToPreInstall(ssClassesToPreInstall);
		
	}
	public InetAddress getLocalHost() {
		return localHost;
	}

	public String getLocalHostNameString() {
		return localHostNameString;
	}

	public void close() {
		if (getController() != null) {
			getController().closeAndWait(1000);
		}
		dispose();
		saveToPrefs(GameConstants.PLAYER_NAME, this.getPlayerName());
		// There's no need to do System.exit(0) ordinarily, but for some reason if this app is
		// WebStarted, WebStart doesn't close and it stays running even though the frame is not shown.
		System.exit(0);
	}

	public GameDesktopPane getDesktopPane() {
		return desktopPane;
	}

	public void setDesktopPane(GameDesktopPane desktopPane) {
		this.desktopPane = desktopPane;
	}

	public Controller getController() {
		return currentController;
	}

	public void setController(Controller controller) {
		this.currentController = controller;
	}

	public ViewPane getViewPane() {
		return viewPane;
	}

	public void setViewPane(ViewPane viewPane) {
		this.viewPane = viewPane;
	}

	public String getLocalHostIPString() {
		return localHostIPString;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public LobbyClient getLobbyClient() {
		return lobbyClient;
	}

	public void doLookAndFeelMenu(ViewPane v) {
		LookAndFeelChooser lnfChooser = new LookAndFeelChooser(this);
	}

	public static boolean isSubstanceLnFPresent() {
		return substanceLnFExists;
	}

	public void updateLookAndFeel() {
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(desktopPane);
	}

	public Preferences getUserPrefs() {
		return userPrefs;
	}

	public String getFromPrefs(String key) {
		String obj = getUserPrefs().get(key, null);
		return obj;
	}

	public boolean saveToPrefs(String key, String obj) {
		if (obj == null) {
			getUserPrefs().remove(key);
			return true;
		}
		getUserPrefs().put(key, obj);
		try {
			getUserPrefs().flush();
		} catch (BackingStoreException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
}
