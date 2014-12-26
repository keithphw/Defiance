/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.lobby;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import sydneyengine.network.ByteClientMina;
import sydneyengine.network.ByteServerOrClient;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;

/**
 * =============== IMPORTANT: This is run on the client and connects to the *CentralLobbyServer*.
 * 
 * @see     CentralLobbyServer
 *
 * @author Keith Woodward
 */
public class LobbyClient {

	protected LobbyInfo lobbyInfo = new LobbyInfo();
	protected InfoSenderAndReceiver infoSenderAndReceiver;
	protected ByteServerOrClient byteClient;
	Object sendMutex = new Object();
	boolean connected = false;

	protected SSObjectOutputStream ssout;
	protected SSObjectInputStream ssin;

	public SSObjectInputStream getSSIn() {
		return ssin;
	}

	public SSObjectOutputStream getSSOut() {
		return ssout;
	}

	public LobbyClient() {
		try {
			ssout = new SSObjectOutputStream();
			ssin = new SSObjectInputStream();
			ssout.syncStoredObjectsWith(ssin);
		} catch (IOException e) {
			e.printStackTrace();
		}
		infoSenderAndReceiver = new InfoSenderAndReceiver(this);
		infoSenderAndReceiver.setDaemon(true);
		System.out.println("LobbyClient(): LobbyClient object constructed.");
	}

	public void start() {
		this.infoSenderAndReceiver.start();
		
		System.out.println("LobbyClient.start(): InfoSenderAndReceiver Started, state= " + this.infoSenderAndReceiver.getState());
	}

	public void sendNotificationOfNewHostedGame() throws IOException {
		System.out.println("Sending notification of new hosting game...");
		HostedGame hostedGame = new HostedGame();
		sendMessage(CentralLobbyServer.CLIENT_TO_SERVER_NEW_HOSTED_GAME, hostedGame);
	}
	
	public void sendNotificationOfExitedHostedGame() throws IOException {
		System.out.println("Sending notification of exited hosting game...");
		sendMessage(CentralLobbyServer.CLIENT_TO_SERVER_EXITED_HOSTED_GAME, null);
	}

	public void sendRequestForLobbyInfo() throws IOException {
		System.out.println("Sending request for lobby info...");
		sendMessage(CentralLobbyServer.CLIENT_TO_SERVER_REQUEST_LOBBY_INFO, null);
	}

	protected void sendMessage(int messageType, Object object) throws IOException {
		synchronized (sendMutex) {
			if (connected == false){
				System.err.println(this.getClass().getSimpleName()+".sendMessage method: not sending since byteClient == null (probably because could not connect, connected == "+connected);
				return;
			}
			SSObjectOutputStream ssoutStreamToUse = ssout;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ssoutStreamToUse.setOutputStream(bout);
			ssoutStreamToUse.writeInt(messageType);
			if (object != null){
				ssoutStreamToUse.writeObject(object);
			}
			ssoutStreamToUse.writeDone();
			byte[] bytes = bout.toByteArray();
			ssoutStreamToUse.setOutputStream(null);
			byteClient.sendTCP(bytes);
		}
	}

	public LobbyInfo getLobbyInfo() {
		return lobbyInfo;
	}

	public void setLobbyInfo(LobbyInfo lobbyInfo) {
		this.lobbyInfo = lobbyInfo;
	}

	public ByteServerOrClient getByteClient() {
		return byteClient;
	}

	public class InfoSenderAndReceiver extends Thread {

		LobbyClient lobbyClient;
		protected volatile boolean shouldRun = true;

		public InfoSenderAndReceiver(LobbyClient lobbyClient) {
			this.lobbyClient = lobbyClient;
		}

		//automatically processed
		@Override
		public void run() {
			Thread.currentThread().setName(this.getClass().getSimpleName() + " " + Thread.currentThread().getName());
			while (shouldRun) {
				while (shouldRun && connected == false) {
					try {
						byteClient = new ByteClientMina();
						((ByteClientMina)byteClient).connect(CentralLobbyServer.getStaticInetSocketAddress());
						connected = true;
						System.out.println(this.getClass().getSimpleName() + ": connected!");
						break;
					} catch (java.io.IOException ex) {
						//ex.printStackTrace();
						//System.out.println(this.getClass().getSimpleName() + ": could not connect to central server host but will keep on trying.");
						try {
							Thread.sleep(10000);
							System.out.println("LobbyClient.run(): Thread successfully slept");
						} catch (InterruptedException e) {
							System.out.println(e);
							//e.printStackTrace();
						}
						continue;
					}
				}
				//System.out.println(this.getClass().getSimpleName() + ": sending/receiving");
				byte[] bytes = null;
				try {
					bytes = lobbyClient.getByteClient().recieveTCP();
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println(this.getClass().getSimpleName() + ": threw an exception so disconnecting, then will attempt to reconnect.");
					try {
						byteClient.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					connected = false;
					continue;
				}
				if (bytes != null) {
					//System.out.println(this.getClass().getSimpleName() + ": " + recievedBytes.length + " " + new String(recievedBytes));
					ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
					try {
						ssin.setInputStream(bin);
						int messageType = ssin.readInt();
						if (messageType == CentralLobbyServer.SERVER_TO_CLIENT_LOBBY_INFO){
							lobbyInfo = (LobbyInfo)ssin.readObject();
							System.out.println(this.getClass().getSimpleName() + ": new lobbyInfo recieved from server, lobbyInfo.getNumGamersConnected() == "+lobbyInfo.getNumPlayersConnected());
						}else if (messageType == CentralLobbyServer.SERVER_TO_CLIENT_NUM_CONNECTED_GAMERS){
							lobbyInfo.setNumPlayersConnected(((Integer)ssin.readObject()).intValue());//ssin.readInt());
							System.out.println(this.getClass().getSimpleName() + ": SERVER_TO_CLIENT_NUM_CONNECTED_GAMERS recieved from server: "+lobbyInfo.getNumPlayersConnected());
						}
						ssin.readDone();
						
					} catch (IOException e) {
						// this shouldn't happen.
						e.printStackTrace();
						/*System.err.println(this.getClass().getSimpleName() + ": threw an exception when reading byte array so disconnecting, then will attempt to reconnect.");
						try {
							byteClient.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						connected = false;
						continue;*/
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
		this.infoSenderAndReceiver.close();
		try {
			byteClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isConnected() {
		return connected;
	}

	public static void main(String[] args) {
		LobbyClient lc = new LobbyClient();
		lc.start();
	}
}
