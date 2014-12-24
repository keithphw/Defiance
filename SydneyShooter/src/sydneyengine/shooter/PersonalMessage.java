package sydneyengine.shooter;



public class PersonalMessage {
	Player player;
	String message;
	protected double timeStamp;
	
	public PersonalMessage(Player player, String message, double timeStamp) {
		this.player= player;
		this.message= message;
		this.timeStamp= timeStamp;
	}

	public String getText() {
		return message;
	}

	public double getTimeStamp() {
		return timeStamp;
	}
}
