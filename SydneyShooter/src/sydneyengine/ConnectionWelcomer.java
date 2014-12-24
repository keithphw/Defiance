/*
 * ConnectionWelcomer.java
 *
 * Created on 15 November 2007, 13:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine;

import sydneyengine.shooter.Player;
import sydneyengine.network.*;
import sydneyengine.superserializable.*;
import java.io.*;

// all this class does is give out ConnectionWelcomers when asked.
public class ConnectionWelcomer implements ConnectionServerListener, GameConstants {

	int newClientVMCode = SSCodeAllocator.getVMCode();	// this is the vMCode that will be given to clients. Need to make sure that all are unique.
	Object newClientVMCodeMutex = new Object();
	ServingController servingController;

	public ConnectionWelcomer(ServingController servingController) {
		this.servingController = servingController;
	}

	public int getNewClientVMCode() {
		synchronized (newClientVMCodeMutex) {
			return ++newClientVMCode;
		}
	}

	public void connectionMade(ByteServer byteServer) {
		Thread t = new ConnectionCommunicator(this, servingController, byteServer);
		t.setDaemon(true);
		t.start();
	}

	public ServingController getServingController() {
		return servingController;
	}

	public void setServingController(ServingController servingController) {
		this.servingController = servingController;
	}

	public class ConnectionCommunicator extends Thread {

		ConnectionWelcomer connectionWelcomerImpl;
		ServingController servingController;
		ByteServer byteServer;
		protected SSObjectOutputStream ssout;
		protected SSObjectInputStream ssin;

		public SSObjectInputStream getTempSSIn() {
			return ssin;
		}

		public SSObjectOutputStream getTempSSOut() {
			return ssout;
		}

		public ConnectionCommunicator(ConnectionWelcomer connectionWelcomerImpl, ServingController servingController, ByteServer byteServer) {
			this.connectionWelcomerImpl = connectionWelcomerImpl;
			this.servingController = servingController;
			this.byteServer = byteServer;
			try {
				ssout = new SSObjectOutputStream();
				ssin = new SSObjectInputStream();
				ssout.syncStoredObjectsWith(ssin);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			this.setName("ConnectionWelcomer " + getName());

			System.out.println(this.getClass().getSimpleName() + ": about to welcome new client");


			Nexus nexus = new Nexus(byteServer);
			nexus.setController(servingController);
			//Player newPlayer = new Player();
			//nexus.setPlayer(newPlayer);

			int clientVMCode = getNewClientVMCode();
			System.out.println(this.getClass().getSimpleName() + ": sending clientVMCode == " + clientVMCode + ", note that the maximum VM code available (SSTools.MAX_VMCODE) == " + SSCodeAllocator.MAX_VMCODE);
			try {
				nexus.send(-1, clientVMCode, getTempSSOut());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			System.out.println(this.getClass().getSimpleName() + ": sent clientVMCode");

			System.out.println(this.getClass().getSimpleName() + ": about ot try to recieve the cleint's player");
			MessagePack messagePack = null;
			int maxMillisToLoop = 10000;	// 10 seconds
			long timeAtStartOfLoop = System.currentTimeMillis();
			while (true) {
				try {
					messagePack = nexus.recieve();
				} catch (IOException e) {
					e.printStackTrace();
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
						return;
					}
				}
			}
			try {
				messagePack.constructObject(getTempSSIn());
			} catch (IOException e) {
				e.printStackTrace();
			}
			Player newPlayer = (Player) messagePack.getObject();
			nexus.setPlayer(newPlayer);
			System.out.println(this.getClass().getSimpleName() + ": received player == " + newPlayer);
/*
			// send newPlayer's SS Code
			int playerSSCode = newPlayer.getSSCode();

			System.out.println(this.getClass().getSimpleName() + ": sending playerSSCode == " + playerSSCode);
			try {
				nexus.send(-1, playerSSCode, getTempSSOut());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			System.out.println(this.getClass().getSimpleName() + ": sent playerSSCode");

			// Need to add the newPlayer to the SS streams so that the nexus's newPlayer will 
			// be the same object that comes through the servingController's ssin stream when 
			// the client sends its player in its PlayerJoinEvent.
			// Note that a reference to newPlayer is kept in the nexus so it will not be 
			// garbage collected and removed from servingController's ssin or ssout stream's weakSSObjectMap.
			servingController.getSSIn().putStoredObject(newPlayer);
			servingController.getSSOut().putStoredObject(newPlayer);
*/
			System.out.println(this.getClass().getSimpleName() + ": calculating server clock diff");
			LatencyInfo tempLatencyInfo = null;
			for (int i = 0; i < NUM_CLOCK_SYNCS; i++) {
				try {
					nexus.getLatencyCalculator().initiateLatencyRequest();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				messagePack = null;
				timeAtStartOfLoop = System.currentTimeMillis();
				while (true) {
					try {
						messagePack = nexus.recieve();
					} catch (IOException e) {
						e.printStackTrace();
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
							return;
						}
					}
				}
				try {
					messagePack.constructObject(getTempSSIn());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				assert messagePack.getType() == Nexus.LATENCY_RESPONSE;
				assert messagePack.getObject() != null;
				nexus.getLatencyCalculator().computeLatencyAndClockDiff((LatencyPostCard) messagePack.getObject());
			}
			try {
				nexus.getLatencyCalculator().initiatorSendLatencyResults();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			System.out.println(this.getClass().getSimpleName() + ": finished calculating server clock diff, nexus.getLatencyCalculator().getServerClockDiffNanos() == " + nexus.getLatencyCalculator().getServerClockDiffNanos() + ", nexus.getLatencyCalculator().getLatencyToServerNanos() == " + nexus.getLatencyCalculator().getLatencyToServerNanos());

			// Once the nexus is added to the serverController, the nexus is sent the events and the world.
			// This must be done in the controller thread because that thread modifies the world, 
			// and we can't send the world from this thread half-way through a modification.
			servingController.addNexus(nexus);

			System.out.println(this.getClass().getSimpleName() + ": done initial setup. World will be sent to client shortly in the Controller thread.");
		}
	}
}

