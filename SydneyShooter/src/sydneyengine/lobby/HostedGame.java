/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.lobby;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import sydneyengine.superserializable.SSAdapter;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;

/**
 *
 * @author CommanderKeith
 */
public class HostedGame extends SSAdapter{

	int numPlayers = 0;
	transient InetSocketAddress inetSocketAddress;
	
	public HostedGame() {
	}
	
	public HostedGame(InetSocketAddress inetSocketAddress) {
		this.inetSocketAddress = inetSocketAddress;
	}

	public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
		this.inetSocketAddress = inetSocketAddress;
	}

	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}

	public int getNumPlayers() {
		return numPlayers;
	}
	
	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException{		// this is the method that you over-ride if you want custom serialization
		out.writeFields(this);
		// write the inetSocketAddress:
		if (inetSocketAddress == null){
			out.writeBoolean(false);
		}else{
			out.writeBoolean(true);
			byte[] ipBytes = inetSocketAddress.getAddress().getAddress();
			out.writeInt(ipBytes.length);
			out.write(inetSocketAddress.getAddress().getAddress());
			out.writeInt(inetSocketAddress.getPort());
		}
	}
	@Override
	public void readSS(SSObjectInputStream in) throws java.io.IOException{	// this is the method that you over-ride if you want custom serialization
		in.readFields(this);
		// read the inetSocketAddress:
		if (in.readBoolean() == true){
			byte[] ipBytes = new byte[in.readInt()];
			in.read(ipBytes);
			InetAddress ip = InetAddress.getByAddress(ipBytes);
			inetSocketAddress = new InetSocketAddress(ip, in.readInt());
		}else{
			inetSocketAddress = null;
		}
	}

}
