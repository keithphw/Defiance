/*
 * GameConstants.java
 *
 * Created on 27 November 2007, 14:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

/**
 *
 * @author CommanderKeith
 */
public interface GameConstants {
	// used in GameFrame.doCreate and doJoinGame
	int CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS = 1;
	int DEDICATED_SERVER_CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS = 40;
	boolean WORLD_DO_MOVE_BETWEEN_EVENTS_IF_TIME_STAMPS_EQUAL = false;
	double WORLD_EVENT_TIME_STAMP_MULTIPLE_SECONDS = 0.05;
	double WORLD_MAX_UPDATE_ELAPSED_SECONDS = WORLD_EVENT_TIME_STAMP_MULTIPLE_SECONDS + 0.00001;
	
	double WORLD_MIN_TIME_GAP_SECONDS_LAN = 0.5;
	double WORLD_MAX_TIME_GAP_SECONDS_LAN = 0.8;
	double EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_LAN = 1.5;
	
	long SERVER_CONTROLLER_MIN_TIME_BETWEEN_CLIENT_UPDATES_NANOS_LAN = 1000000000;//1000000000L == 1 second.  Set this to a low value to stress-test.
	
	double WORLD_MIN_TIME_GAP_SECONDS_INTERNET = 1.0;
	double WORLD_MAX_TIME_GAP_SECONDS_INTERNET = 1.3;
	double EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_INTERNET = 2.9;
	long SERVER_CONTROLLER_MIN_TIME_BETWEEN_CLIENT_UPDATES_NANOS_INTERNET = 4000000000L;	// 3.3 seconds
	
	String LOBBY_SERVER_IP_STRING = "213.247.55.3";//"192.168.0.146";//"213.247.55.3";//"127.0.0.1";//"213.247.55.3";//"169.254.107.67";//"172.20.109.51"//"192.168.1.13"//"192.168.0.146";
	int LOBBY_SERVER_PORT = 4320;
	int DEFAULT_PORT_TCP = 4321;
	int DEFAULT_PORT_UDP = 4322;	// note that UDP is not implemented yet.
	
	// used in GameFrame.doJoin method:
	int NUM_CLOCK_SYNCS = 3;
	
	// used in GameFrame:
	public final String PLAYER_NAME = "PLAYER_NAME";
	public final String LAN_SERVER_IP_ADDRESS = "LAN_SERVER_IP_ADDRESS";
}
