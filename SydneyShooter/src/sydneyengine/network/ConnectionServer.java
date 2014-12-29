/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.network;

import java.net.InetSocketAddress;

/**
 *
 * @author woodwardk
 */
public interface ConnectionServer {
	public void bindAndListen(InetSocketAddress inetSocketAddress) throws java.io.IOException;
	public void close();
	public ConnectionServerListener getConnectionServerListener();
	public void setConnectionServerListener(ConnectionServerListener connectionServerListener);
	public InetSocketAddress getInetSocketAddress();
}