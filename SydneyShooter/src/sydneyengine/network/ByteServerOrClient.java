/*
 * ByteServerOrClient.java
 *
 * Created on 11 June 2007, 01:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine.network;

import java.io.*;
/**
 *
 * @author CommanderKeith
 */
public interface ByteServerOrClient {
		public byte[] recieveTCP() throws IOException;

		public void sendTCP(byte[] bytes) throws IOException;

		//public void sendUDP(byte[] bytes) throws IOException;
		//public byte[] recieveUDP() throws IOException;

		public void close() throws IOException;
}
