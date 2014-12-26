/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.network;

import java.io.IOException;
import java.net.InetSocketAddress;
/**
 *
 * @author woodwardk
 */
public interface ByteClient extends ByteServerOrClient{
	public void connect(InetSocketAddress serverAddress) throws IOException;
}
