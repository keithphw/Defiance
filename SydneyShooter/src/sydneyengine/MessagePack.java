/*
 * MessagePack.java
 *
 * Created on 15 November 2007, 14:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

/**
 *
 * @author CommanderKeith
 */
import java.util.*;
import java.io.*;
import sydneyengine.superserializable.*;
/**
 * MessagePack is used to keep the object, its bytes and the integer type of the message all together.
 * @author Keith
 */
public class MessagePack{
	protected int type;
	protected Object object;
	protected ByteArrayInputStream bin;

	public MessagePack(int type, Object object, ByteArrayInputStream bin) {
		this.type = type;
		this.object = object;
		this.bin = bin;
	}
	public void constructObject(SSObjectInputStream ssin) throws IOException{
		ssin.setInputStream(bin);
		object = ssin.readObject();
		ssin.readDone();
	}

	public ByteArrayInputStream getByteArrayInputStream(){
		return bin;
	}
	// must have constructObject() called on it before getObject() is called.
	public Object getObject(){
		return object;
	}

	public int getType() {
		return type;
	}
}
