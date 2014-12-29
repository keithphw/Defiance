
/*
 * CentralLobbyServer.java
 *
 * Created on 15 November 2007, 13:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine.lobby;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sydneyengine.GameConstants;
import sydneyengine.network.Address;
import sydneyengine.network.ByteServer;
import sydneyengine.network.ByteServerMina;
import sydneyengine.network.ConnectionServer;
import sydneyengine.network.ConnectionServerListener;
import sydneyengine.network.ConnectionServerMina;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;

/**
 * This is run on the central server, kindly hosted by Riven in Amsterdam.
 * 
 * @see     LobbyClient
 *
 * @author Keith Woodward
 */
//=========================== Kwuang says: should I also run this???
public class CentralLobbyServer implements ConnectionServerListener, GameConstants{

	static {
		// this is not needed when running from the jar, but it is needed when running from webstart.
		sydneyengine.shooter.GameFrame.setupSSToolsInstalledClasses();
	}
	
	public static final int CLIENT_TO_SERVER_NEW_HOSTED_GAME = 100;
	public static final int CLIENT_TO_SERVER_REQUEST_LOBBY_INFO = 101;
	public static final int SERVER_TO_CLIENT_LOBBY_INFO = 200;
	public static final int SERVER_TO_CLIENT_NUM_CONNECTED_GAMERS = 201;
	public static final int CLIENT_TO_SERVER_EXITED_HOSTED_GAME = 202;
	

	public static InetSocketAddress getStaticInetSocketAddress() {
		return staticInetSocketAddress;
	}

	protected Object connectedGamersMutex = new Object();
	protected List<ConnectedGamer> connectedGamers = new ArrayList<ConnectedGamer>();
	LobbyInfo lobbyInfo = new LobbyInfo();
	protected CentralConnectionWelcomer centralConnectionListenerThread;
	protected InfoSenderAndReceiver centralInfoSenderAndReceiver;
	protected static InetSocketAddress staticInetSocketAddress = null;		// this is needed and used by the clients, not by the central server

	static {
		try {
			String ipString = LOBBY_SERVER_IP_STRING;
			staticInetSocketAddress = new InetSocketAddress(Address.getByAddress(ipString), LOBBY_SERVER_PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	protected SSObjectOutputStream ssout;
	protected SSObjectInputStream ssin;
	Object sendMutex = new Object();

	public SSObjectInputStream getSSIn() {
		return ssin;
	}

	public SSObjectOutputStream getSSOut() {
		return ssout;
	}

	public CentralLobbyServer() {
		lobbyInfo.setDateOfServerBirth((new Date().toString()));
		try {
			ssout = new SSObjectOutputStream();
			ssin = new SSObjectInputStream();
			ssout.syncStoredObjectsWith(ssin);
		} catch (IOException e) {
			e.printStackTrace();
		}
		centralInfoSenderAndReceiver = new InfoSenderAndReceiver(this);
	}

	public void start() throws java.io.IOException{
		ConnectionServer connectionServer = new ConnectionServerMina();
		connectionServer.setConnectionServerListener(this);
		
		InetAddress localHostInetAddress = InetAddress.getLocalHost();
		connectionServer.bindAndListen(new InetSocketAddress(localHostInetAddress, GameConstants.LOBBY_SERVER_PORT));

		centralInfoSenderAndReceiver.start();
	}

	public void sendNumConnectedGamersToAll() {
		synchronized (this.connectedGamersMutex) {
			for (int i = 0; i < connectedGamers.size(); i++) {
				try {
					sendMessage(connectedGamers.get(i).getByteServer(), SERVER_TO_CLIENT_NUM_CONNECTED_GAMERS, connectedGamers.size());
				} catch (IOException e) {
					e.printStackTrace();
					//System.err.println(this.getClass().getSimpleName() + ": removing a ConnectedGamer since it threw an exception");
					// remove any game that this ConnectedGamer was hosting, if any.
					lobbyInfo.getHostedGames().remove(connectedGamers.get(i).getHostedGame());
					connectedGamers.remove(i);
					lobbyInfo.setNumPlayersConnected(connectedGamers.size());
					i--;
				}
			}
		}
	}
	public void sendLobbyInfoToAll() {
		synchronized (this.connectedGamersMutex) {
			for (int i = 0; i < connectedGamers.size(); i++) {
				try {
					sendMessage(connectedGamers.get(i).getByteServer(), SERVER_TO_CLIENT_LOBBY_INFO, lobbyInfo);
				} catch (IOException e) {
					e.printStackTrace();
					removeConnectedGamer(connectedGamers.get(i));
					i--;
				}
			}
		}
	}

	protected void sendMessage(ByteServer byteServer, int messageType, Object object) throws IOException {
		synchronized (sendMutex) {
			SSObjectOutputStream ssoutStreamToUse = ssout;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ssoutStreamToUse.setOutputStream(bout);
			ssoutStreamToUse.writeInt(messageType);
			ssoutStreamToUse.writeObject(object);
			ssoutStreamToUse.writeDone();
			byte[] bytes = bout.toByteArray();
			ssoutStreamToUse.setOutputStream(null);
			byteServer.sendTCP(bytes);
		}
	}

	public Object getMutex() {
		return connectedGamersMutex;
	}

	public List<ConnectedGamer> getConnectedGamers() {
		return connectedGamers;
	}
	
	@Override
	public void connectionMade(ByteServer byteServer){
		(new CentralConnectionWelcomer(byteServer, this)).start();
	}

	public class CentralConnectionWelcomer extends Thread{
		ByteServer byteServer;
		CentralLobbyServer centralLobbyServer;
		protected ConnectionServer connectionServer;

		public CentralConnectionWelcomer(ByteServer byteServer, CentralLobbyServer centralLobbyServer) {
			this.byteServer = byteServer;
			this.centralLobbyServer = centralLobbyServer;
		}

		@Override
		public void run() {
			Thread.currentThread().setName(this.getClass().getSimpleName() + " " + Thread.currentThread().getName());
			
			//System.out.println(this.getClass().getSimpleName() + ": got a connection");
			if (Runtime.getRuntime().freeMemory() < 200000){	// if less than 0.2MB of memory left, don't accept any more connections...
				System.out.println(this.getClass().getSimpleName() + ": too many connections, running out of memory! refusing connection... connectedGamers.size() == "+connectedGamers.size());
				try{
					byteServer.close();
				}catch (IOException e) {
					e.printStackTrace();
				}
				System.gc();
			}
			synchronized (connectedGamersMutex) {
				ConnectedGamer newConnectedGamer = new ConnectedGamer(byteServer);
				connectedGamers.add(newConnectedGamer);
				lobbyInfo.setNumPlayersConnected(connectedGamers.size());
				// set some of the stats:
				lobbyInfo.setTotalNumPlayersConnected(lobbyInfo.getTotalNumPlayersConnected()+1);
				if (lobbyInfo.getNumPlayersConnected() > lobbyInfo.getMostPlayersOnline()){
					lobbyInfo.setMostPlayersOnline(lobbyInfo.getNumPlayersConnected());
				}
			}
			sendLobbyInfoToAll();
		}
	}
	
	public void removeConnectedGamer(ConnectedGamer connectedGamer){
		getConnectedGamers().remove(connectedGamer);
		lobbyInfo.setNumPlayersConnected(connectedGamers.size());
		System.err.println(this.getClass().getSimpleName() + ": removing a ConnectedGamer. connectedGamers.size() == "+connectedGamers.size());
		if (connectedGamer.getHostedGame() != null){
			// remove any game that this ConnectedGamer was hosting, if any.
			lobbyInfo.getHostedGames().remove(connectedGamer.getHostedGame());
			for (int j = 0; j < getConnectedGamers().size(); j++) {
				try{
					sendMessage(getConnectedGamers().get(j).getByteServer(), CentralLobbyServer.SERVER_TO_CLIENT_LOBBY_INFO, lobbyInfo);
				}catch(IOException ex){
					ex.printStackTrace();
					removeConnectedGamer(getConnectedGamers().get(j));
					j--;
				}
			}
		}else{
			sendNumConnectedGamersToAll();
		}
	}

	public class InfoSenderAndReceiver extends Thread {

		CentralLobbyServer centralLobbyServer;
		protected volatile boolean shouldRun = true;

		public InfoSenderAndReceiver(CentralLobbyServer centralLobbyServer) {
			this.centralLobbyServer = centralLobbyServer;
		}

		@Override
		public void run() {
			Thread.currentThread().setName(this.getClass().getSimpleName() + " " + Thread.currentThread().getName());
			//System.out.println(this.getClass().getSimpleName() + ": started");
			while (shouldRun) {
				//System.out.println(this.getClass().getSimpleName() + ": sending/receiving");
				synchronized (connectedGamersMutex) {
					for (int i = 0; i < centralLobbyServer.getConnectedGamers().size(); i++) {
						ConnectedGamer connectedGamer = centralLobbyServer.getConnectedGamers().get(i);
						byte[] bytes = null;
						try {
							bytes = connectedGamer.getByteServer().recieveTCP();
						} catch (IOException e) {
							e.printStackTrace();
							removeConnectedGamer(connectedGamer);
							i--;
							continue;
						}
						if (bytes != null) {
							//System.out.println(this.getClass().getSimpleName() + ": " + recievedBytes.length + " " + new String(recievedBytes));
							ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
							try {
								ssin.setInputStream(bin);
								int messageType = ssin.readInt();
								if (messageType == CentralLobbyServer.CLIENT_TO_SERVER_NEW_HOSTED_GAME) {
									HostedGame newHostedGame = (HostedGame) ssin.readObject();
									System.out.println(this.getClass().getSimpleName()+": new hosted game from connectedGamer.getRemoteInetAddress() == "+connectedGamer.getRemoteInetAddress());
									newHostedGame.setInetSocketAddress(new InetSocketAddress(connectedGamer.getRemoteInetAddress(), GameConstants.DEFAULT_PORT_TCP));
									lobbyInfo.getHostedGames().add(newHostedGame);
									connectedGamer.setHostedGame(newHostedGame);
									// send the ConnectedGamers the new LobbyInfo with the newly created game!
									for (int j = 0; j < centralLobbyServer.getConnectedGamers().size(); j++) {
										centralLobbyServer.sendMessage(centralLobbyServer.getConnectedGamers().get(j).getByteServer(), CentralLobbyServer.SERVER_TO_CLIENT_LOBBY_INFO, lobbyInfo);
									}
									//System.out.println(this.getClass().getSimpleName() + ": new hostedGame added to lobbyInfo");
								}else if (messageType == CentralLobbyServer.CLIENT_TO_SERVER_EXITED_HOSTED_GAME) {
									lobbyInfo.getHostedGames().remove(connectedGamer.getHostedGame());
									connectedGamer.setHostedGame(null);
									centralLobbyServer.sendLobbyInfoToAll();
									//System.out.println(this.getClass().getSimpleName() + ": removed hostedGame since it was quit by the ConnectedGamer");
								} else if (messageType == CentralLobbyServer.CLIENT_TO_SERVER_REQUEST_LOBBY_INFO) {
									centralLobbyServer.sendMessage(connectedGamer.getByteServer(), CentralLobbyServer.SERVER_TO_CLIENT_LOBBY_INFO, lobbyInfo);
									//System.out.println(this.getClass().getSimpleName() + ": CLIENT_TO_SERVER_REQUEST_LOBBY_INFO recieved from client, so sent lobbyInfo");
								}
								ssin.readDone();
							} catch (IOException e) {
								// this shouldn't happen.
								e.printStackTrace();
								removeConnectedGamer(connectedGamer);
								i--;
								continue;
							}
						}
					}
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			closeNow();
		}

		public void close() {
			shouldRun = false;
		}

		public void closeNow() {
			System.out.println(this.getClass().getSimpleName() + ": closing 1");
			close();
			System.out.println(this.getClass().getSimpleName() + ": closing 2");

		}
	}

	public void close() {
		this.centralInfoSenderAndReceiver.close();
	}

	public class ConnectedGamer {

		protected ByteServer byteServer;
		protected HostedGame hostedGame;

		public ConnectedGamer(ByteServer byteServer) {
			this.byteServer = byteServer;
		}

		public ByteServer getByteServer() {
			return byteServer;
		}

		public HostedGame getHostedGame() {
			return hostedGame;
		}

		public void setHostedGame(HostedGame hostedGame) {
			this.hostedGame = hostedGame;
		}
		public InetAddress getRemoteInetAddress(){
			SocketAddress socketAddress = ((ByteServerMina)getByteServer()).getRefToIoSession().getRemoteAddress();
			return ((InetSocketAddress)socketAddress).getAddress();
		}
	}

	public static void main(String[] args) {
		CentralLobbyServer cls = new CentralLobbyServer();
		try{
			cls.start();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
