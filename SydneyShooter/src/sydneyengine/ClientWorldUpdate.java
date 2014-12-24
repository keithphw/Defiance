/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine;

import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author woodwardk
 */

public class ClientWorldUpdate extends SSAdapter{

	protected RewindableWorld world;
	byte[] worldBytes = null;

	public ClientWorldUpdate() {
	}
	public ClientWorldUpdate(RewindableWorld world) {
		this.world = world;
	}

	// This method should only be called by the Controller thread.
	public void serialize(SSObjectOutputStream ssout) throws IOException {
		assert world != null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ssout.setOutputStream(bout);
		ssout.writeObject(world);
		ssout.writeDone();
		worldBytes = bout.toByteArray();
		
		//System.out.println(this.getClass().getSimpleName()+": serialize, worldBytes.length == "+worldBytes.length);
	}
	
	// This method should only be called by the Controller thread.
	public void deserialize(SSObjectInputStream ssin) throws IOException {
		assert worldBytes != null;
		assert worldBytes.length > 0;
		ByteArrayInputStream bin = new ByteArrayInputStream(worldBytes);
		ssin.setInputStream(bin);
		world = (RewindableWorld) ssin.readObject();
		ssin.readDone();
		//System.out.println(this.getClass().getSimpleName()+": deserialize, worldBytes.length == "+worldBytes.length);
	}

	public boolean isDeserialized() {
		return world != null;
	}

	public boolean isSerialized() {
		return worldBytes != null;
	}

	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		assert isSerialized();
		out.writeInt(worldBytes.length);
		out.write(worldBytes);
	}

	public void readSS(SSObjectInputStream in) throws IOException {	// this is the method that you over-ride if you want custom serialization
		// question: is this method called for each class level?? or will sub-class vars not get written?
		// answer: no, this method will not be called at each class-level,
		// but in.readFields reads all vars from all class levels so sub-classes won't miss out.
		int worldBytesLength = in.readInt();
		worldBytes = new byte[worldBytesLength];
		in.read(worldBytes);
	}

	public RewindableWorld getWorld() {
		return world;
	}
}
