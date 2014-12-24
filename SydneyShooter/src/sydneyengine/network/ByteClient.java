/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.network;

import java.io.*;
import java.net.*;
/**
 *
 * @author woodwardk
 */
public interface ByteClient extends ByteServerOrClient{
	public void connect(InetSocketAddress serverAddress) throws IOException;
}
