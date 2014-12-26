package sydneyengine.shooter;

import java.io.IOException;

import sydneyengine.superserializable.SSAdapter;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;



public class PersonalMessage extends SSAdapter {
	Player player;
	String message;
	protected double timeStamp;
	
	public PersonalMessage(Player player, String message, double timeStamp) {
		this.player= player;
		this.message= message;
		this.timeStamp= timeStamp;
	}
	
	//needed to satisfy SS streams??
	public PersonalMessage() {
		
	}

	public String getText() {
		return message;
	}

	public double getTimeStamp() {
		return timeStamp;
	}
	
	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
	}

	@Override
	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
	}
}
