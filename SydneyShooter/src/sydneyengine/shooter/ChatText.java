/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import sydneyengine.superserializable.SSAdapter;

/**
 *
 * @author woodwardk
 */
public class ChatText extends SSAdapter implements Comparable{

	protected Player player;
	protected String text;
	protected boolean alliesOnly;
	protected double timeStamp;
	float x;
	float y;
	
	public ChatText(){
	}
	
	public ChatText(Player player, String text, boolean alliesOnly, double timeStamp){
		this.player = player;
		this.text = text;
		this.alliesOnly = alliesOnly;
		this.timeStamp = timeStamp;
	}

	public Player getPlayer(){
		return player;
	}
	public double getTimeStamp() {
		return timeStamp;
	}

	public String getText() {
		return text;
	}

	public boolean isAlliesOnly() {
		return alliesOnly;
	}
	@Override
	public int compareTo(Object other){
		assert other instanceof ChatText : other;
		double thisTimeStamp = getTimeStamp();
		double otherTimeStamp = ((ChatText)other).getTimeStamp();
		if (thisTimeStamp > otherTimeStamp){
			return 1;
		}else if (thisTimeStamp < otherTimeStamp){
			return -1;
		}else{
			return 0;
		}
	
	}
}
