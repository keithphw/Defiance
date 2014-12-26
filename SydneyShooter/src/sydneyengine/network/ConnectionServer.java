/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.network;

/**
 *
 * @author woodwardk
 */
public interface ConnectionServer {
	public void bindAndListen(int tcpPort) throws java.io.IOException;
	public void close();
	public ConnectionServerListener getConnectionServerListener();
	public void setConnectionServerListener(ConnectionServerListener connectionServerListener);
}
