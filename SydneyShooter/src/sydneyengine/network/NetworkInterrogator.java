/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author Keith
 */
public class NetworkInterrogator {

	public ArrayList<InetAddress> getInetAddressList() throws SocketException{
		ArrayList<InetAddress> inetAddressList = new ArrayList<InetAddress>();
		//System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
		Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
		for (; n.hasMoreElements();) {
			NetworkInterface e = n.nextElement();

			Enumeration<InetAddress> a = e.getInetAddresses();
			for (; a.hasMoreElements();) {
				InetAddress addr = a.nextElement();
				inetAddressList.add(addr);
				//System.out.println("  " + addr.getHostAddress());
			}
		}
		return inetAddressList;
	}
	
	public static void main(String[] args) {
		try {
			System.out.println("Start of ipaddress lister method 1.");

			// See: http://stackoverflow.com/questions/19476872/java-get-local-ip
			System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements();) {
				NetworkInterface e = n.nextElement();

				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements();) {
					InetAddress addr = a.nextElement();
					System.out.println("  " + addr.getHostAddress());
				}
			}
			
			System.out.println("Start of ipaddress lister method 2.");
			InetAddress inet = InetAddress.getLocalHost();
			InetAddress[] ips = InetAddress.getAllByName(inet.getCanonicalHostName());
			if (ips != null) {
				for (int i = 0; i < ips.length; i++) {
					System.out.println(ips[i]);
				}
			}

			System.out.println("Start of ipaddress lister method 3.");
			// See: http://stackoverflow.com/questions/494465/how-to-enumerate-ip-addresses-of-all-enabled-nic-cards-from-java#495545
			try {
				System.out.println("Output of Network Interrogation:");
				System.out.println("********************************\n");

				InetAddress theLocalhost = InetAddress.getLocalHost();
				System.out.println(" LOCALHOST INFO");
				if (theLocalhost != null) {
					System.out.println("          host: " + theLocalhost.getHostName());
					System.out.println("         class: " + theLocalhost.getClass().getSimpleName());
					System.out.println("            ip: " + theLocalhost.getHostAddress());
					System.out.println("         chost: " + theLocalhost.getCanonicalHostName());
					System.out.println("      byteaddr: " + toMACAddrString(theLocalhost.getAddress()));
					System.out.println("    sitelocal?: " + theLocalhost.isSiteLocalAddress());
					System.out.println("");
				} else {
					System.out.println(" localhost was null");
				}

				Enumeration<NetworkInterface> theIntfList = NetworkInterface.getNetworkInterfaces();
				java.util.List<InterfaceAddress> theAddrList = null;
				NetworkInterface theIntf = null;
				InetAddress theAddr = null;

				while (theIntfList.hasMoreElements()) {
					theIntf = theIntfList.nextElement();

					System.out.println("--------------------");
					System.out.println(" " + theIntf.getDisplayName());
					System.out.println("          name: " + theIntf.getName());
					System.out.println("           mac: " + toMACAddrString(theIntf.getHardwareAddress()));
					System.out.println("           mtu: " + theIntf.getMTU());
					System.out.println("        mcast?: " + theIntf.supportsMulticast());
					System.out.println("     loopback?: " + theIntf.isLoopback());
					System.out.println("          ptp?: " + theIntf.isPointToPoint());
					System.out.println("      virtual?: " + theIntf.isVirtual());
					System.out.println("           up?: " + theIntf.isUp());

					theAddrList = theIntf.getInterfaceAddresses();
					System.out.println("     int addrs: " + theAddrList.size() + " total.");
					int addrindex = 0;
					for (InterfaceAddress intAddr : theAddrList) {
						addrindex++;
						theAddr = intAddr.getAddress();
						System.out.println("            " + addrindex + ").");
						System.out.println("            host: " + theAddr.getHostName());
						System.out.println("           class: " + theAddr.getClass().getSimpleName());
						System.out.println("              ip: " + theAddr.getHostAddress() + "/" + intAddr.getNetworkPrefixLength());
						System.out.println("           bcast: " + (intAddr.getBroadcast() == null ? "null" : intAddr.getBroadcast().getHostAddress()));
						int maskInt = Integer.MIN_VALUE >> (intAddr.getNetworkPrefixLength() - 1);
						System.out.println("            mask: " + toIPAddrString(maskInt));
						System.out.println("           chost: " + theAddr.getCanonicalHostName());
						System.out.println("        byteaddr: " + toMACAddrString(theAddr.getAddress()));
						System.out.println("      sitelocal?: " + theAddr.isSiteLocalAddress());
						System.out.println("");
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

			System.out.println("End of ipaddress lister.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String toIPAddrString(int ipa) {
		StringBuilder b = new StringBuilder();
		b.append(Integer.toString(0x000000ff & (ipa >> 24)));
		b.append(".");
		b.append(Integer.toString(0x000000ff & (ipa >> 16)));
		b.append(".");
		b.append(Integer.toString(0x000000ff & (ipa >> 8)));
		b.append(".");
		b.append(Integer.toString(0x000000ff & (ipa)));
		return b.toString();
	}

	public static String toMACAddrString(byte[] a) {
		if (a == null) {
			return "null";
		}
		int iMax = a.length - 1;

		if (iMax == -1) {
			return "[]";
		}

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(String.format("%1$02x", a[i]));

			if (i == iMax) {
				return b.append(']').toString();
			}
			b.append(":");
		}
	}

}

