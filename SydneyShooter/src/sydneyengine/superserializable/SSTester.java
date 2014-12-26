package sydneyengine.superserializable;

//author: Keith Woodward

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SSTester extends SSAdapter implements Serializable{
	
	static int numIterations = 8000;
	
	static boolean doSuperSerializable = true;
	static boolean doSerializable = true;
	
	static boolean write = true;
	static boolean read = true;
	
	int someInt = 4;
	String theString = "hi";
	String theString2 = "hi2";
	SSObject thisObj;
	ArrayListSS arrayList = new ArrayListSS();
	SSTester[][] array = new SSTester[1][2];
	Integer anInteger = new Integer(500);
	
	//sydneyengine.demo.physics.Scenario scenario = new sydneyengine.demo.physics.Scenario();
	
	public SSTester(){
	}
	
	public SSTester(int num){
		thisObj = this;
		
		for (int i = 0 ; i < 1; i++){
			arrayList.add(this);
		}
		array[0][0] = this;
		array[0][1] = this;
	}
	
	@Override
	public int getSSCode(){
		return code;
	}
	
	
	public static void main(String args[]){
		/*String property = System.getProperty("java.class.path");//"java.library.path");
		System.out.println(property);
		System.loadLibrary("jogl");*/
		
		//java.util.ArrayList<Class> sortedSSClasses = SSTools.getSortedSSClasses("net.slavebot");
		
		SSTester obj = new SSTester(0);
		
		byte[] buf = new byte[0];
		
		long nanos1 = 0;
		
		System.out.println("starting");
		
		
		if (doSuperSerializable){
			
			System.out.println("\nSuperSerializable:");
			long time1 = System.nanoTime();
			
			ByteArrayOutputStream byteOut = null;
			SSObjectOutputStream objectOut = null;
			ByteArrayInputStream byteIn = null;
			SSObjectInputStream objectIn = null;
			
			try{
				byteOut = new ByteArrayOutputStream();
				objectOut = new SSObjectOutputStream(byteOut);
				byteIn = new ByteArrayInputStream(buf);
				objectIn = new SSObjectInputStream(byteIn);
			} catch(IOException e){
				e.printStackTrace();
			}
			
		/*Class[] classes = objectOut.collectMemberClasses(obj);
		for (int i = 0; i < classes.length; i++){
			System.out.println(classes[i].getName());
		}*/
			
			int objectsWritten = 0;
			int nonSSWritten = 0;
			int objectsRead = 0;
			int nonSSRead = 0;
			
			
			try{
				if (write == false && read == true){
					byteOut = new ByteArrayOutputStream();
					objectOut.setOutputStream(byteOut);
					objectOut.writeObject(obj);
					objectsWritten = objectOut.objectsWritten;
					nonSSWritten = objectOut.nonSSWritten;
					objectOut.writeDone();
					buf = byteOut.toByteArray();
				}
			} catch(java.io.IOException e){
				e.printStackTrace();
				return;
			}
			
			SSTester readObj = null;
			for (int i = 0; i < numIterations; i++){
				//obj.scenario.addVehicle(new Tank());
				try{
					if (write){
						byteOut = new ByteArrayOutputStream();
						objectOut.setOutputStream(byteOut);
						objectOut.writeObject(obj);
						objectsWritten = objectOut.objectsWritten;
						nonSSWritten = objectOut.nonSSWritten;
						objectOut.writeDone();
						buf = byteOut.toByteArray();
					}
					if (read){
						byteIn = new ByteArrayInputStream(buf);
						objectIn.setInputStream(byteIn);
						readObj = (SSTester)objectIn.readObject();
						if (readObj == null){
							System.out.println("uh oh, null");
						}
						objectsRead = objectIn.objectsRead;
						nonSSRead = objectIn.nonSSRead;
						
						objectIn.readDone();
						//if (i%30 == 0){
						//objectIn.recycleStoredExceptFor(obj);
						//}
					}
					//System.out.println("\n\nobjectOut.objectsWritten: "+objectsWritten+"\nobjectIn.objectsRead: "+objectsRead+"\nobjectOut.nonSSWritten: "+nonSSWritten+"\nobjectIn.nonSSRead: "+nonSSRead);
				}catch(java.io.IOException e){
					e.printStackTrace();
					return;
				}
			}
			
			
			nanos1 = System.nanoTime() - time1;
			System.out.println("nanos: "+nanos1);
			System.out.println("millis/cycle: "+nanos1/((float)numIterations*1000000));
			System.out.println("buf.length: "+buf.length);
			
			System.out.println(""+readObj.equals(obj));
		}
		
		
		long nanos2 = 0;
		if (doSerializable){
			
			System.out.println("\nSerializable:");
			
			ByteArrayOutputStream byteOut = null;
			ObjectOutputStream objectOut = null;
			ByteArrayInputStream byteIn = null;
			ObjectInputStream objectIn = null;
			
			
			
			long time2 = System.nanoTime();
			
			try{
				if (write == false && read == true){
					byteOut = new ByteArrayOutputStream();
					objectOut = new ObjectOutputStream(
						new BufferedOutputStream(byteOut));
					objectOut.writeUnshared(obj);
					objectOut.flush();
					buf = byteOut.toByteArray();
					objectOut.reset();
				}
			} catch(java.io.IOException e){
				e.printStackTrace();
			}
			
			for (int i = 0; i < numIterations; i++){
				try{
					if (write){
						byteOut = new ByteArrayOutputStream();
						objectOut = new ObjectOutputStream(
							new BufferedOutputStream(byteOut));
						objectOut.writeUnshared(obj);
						objectOut.flush();
						buf = byteOut.toByteArray();
						objectOut.reset();
					}
					if (read){
						byteIn = new ByteArrayInputStream(buf);
						objectIn = new ObjectInputStream(byteIn);
						Object readObj = objectIn.readObject();
						
					}
					byteOut.reset();
				}catch(java.io.IOException e){
					e.printStackTrace();
				}catch(ClassNotFoundException e){
					e.printStackTrace();
				}
			}
			nanos2 = System.nanoTime() - time2;
			System.out.println("nanos: "+nanos2);
			System.out.println("millis/cycle: "+nanos2/((float)numIterations*1000000));
			System.out.println("buf.length: "+buf.length);
		}
		
		
		if (doSuperSerializable && doSerializable){
			double nOnN = ((double)nanos1)/((double)nanos2);
			System.out.println("\ntime taken ratio, SS:S " + nOnN+" : 1");
		}
	}
}