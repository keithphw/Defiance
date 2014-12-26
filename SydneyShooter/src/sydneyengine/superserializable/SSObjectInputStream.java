package sydneyengine.superserializable;

//author: Keith Woodward

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SSObjectInputStream extends DataInputStream implements SSObjectStream{
	
	protected FieldCache fieldCache = null;
	
	protected HashMap<Integer, Boolean> readStatus = null;

	volatile protected WeakSSObjectMap<SSObject, Object> storedObjects;
	volatile protected WeakSSObjectMap<SSObject, Object> storedObjectsWaitingToBePut;
	volatile protected Object storedObjectsMutex = new Object();
	volatile protected boolean storedObjectsToBeAdded = false;
	
	protected HashMap<Short, Class> installedClassIndexes = null;	// pre-installed classes and their corresponding indexes.  Note that this includes the particulkar SSObject's class and all of its SSObject super-classes.
	protected HashMap<Short, Class> readClassIndexes = null;		// classes and their corresponding indexes that have been written since this stream was created.  Doesn't include pre-installed SSObject classes.
	
	protected int objectsRead = 0;
	protected int nonSSRead = 0;
	
	protected SSObjectOutputStream ssout = null;
	public SSObjectOutputStream getSSOut(){
		return ssout;
	}
	// This method syncs the 2 streams storedObjects wih each other. It only needs to be called once, ie once you call a.syncStoredObjectsWith(b), you don't need to call b.syncStoredObjectsWith(a).
	@Override
	public void syncStoredObjectsWith(SSObjectStream stream){
		if (stream instanceof SSObjectOutputStream == false){
			throw new IllegalArgumentException(this.getClass().getSimpleName()+" can only be sync'ed with an SSObjectOutputStream, not an "+stream.getClass().getSimpleName());
		}
		if (ssout != stream){
			this.ssout = ((SSObjectOutputStream)stream);
			System.out.println(this.getClass().getSimpleName()+": stream sync'ed");
			stream.syncStoredObjectsWith(this);
		}
	}
	
	@Override
	public void setInstalledClasses(ArrayList<Class> classList){
		if (installedClassIndexes == null){
			installedClassIndexes = new HashMap<Short, Class>(classList.size()*2);
		}else{
			installedClassIndexes.clear();
		}
		short classIndex = CLASS_INDEX_START_INSTALL;
		for (int i = 0; i < classList.size(); i++){
			installedClassIndexes.put(classIndex, classList.get(i));
			classIndex++;
			fieldCache.ensureFieldsCached(classList.get(i));
			//System.out.println(this.getClass().getSimpleName()+": installed class "+i+" - "+classList.get(i).getSimpleName());
		}
	}
	
	/* the use of this stream:
	 *SSObjects are written to this stream on the server VM and their states are sent
	 *to the client VM. The client reads the data and updates the SSObjects if they already
	 *exist in the SSObjectInputStream's storedObjects map.  Otherwise new ones are
	 *created/
	 */
	// a convenience constructor which creates an unusable SSObjectInputStream.  Must have setInputStream(InputStream) called on it.
	public SSObjectInputStream() throws IOException{
		this(new ByteArrayInputStream(new byte[0]), 1000, 100);
	}
	public SSObjectInputStream(InputStream in) throws IOException{
		this(in, 1000, 100);
	}
	// note nonPreInstalledClassesSize should be zero if all classses are pre-installed
	public SSObjectInputStream(InputStream in, int storedObjectsMapSize, int nonPreInstalledClassesSize) throws IOException{
		super(in);
		
		int numInstalledClasses = 0;
		if (SSTools.preInstallSSClasses){
			numInstalledClasses = SSTools.getSSClassesToPreInstall().size();
		}
		fieldCache = new FieldCache(numInstalledClasses*2 + nonPreInstalledClassesSize, numInstalledClasses*2 + nonPreInstalledClassesSize);

		if (SSTools.preInstallSSClasses){
			setInstalledClasses(SSTools.getSSClassesToPreInstall());
		}
		
		readStatus = new HashMap<Integer, Boolean>(storedObjectsMapSize);
		storedObjects = new WeakSSObjectMap<SSObject, Object>(2000);//new HashMap<Integer, WeakReference<SSObject>>(storedObjectsMapSize);//storedObjects = new HashMap<Integer, SSObject>(storedObjectsMapSize);
		storedObjectsWaitingToBePut = new WeakSSObjectMap<SSObject, Object>(2000);
		readClassIndexes = new HashMap<Short, Class>(nonPreInstalledClassesSize);		// classes and their corresponding indexes that have been written since this stream was created.  Doesn't include pre-installed SSObject classes.
		//defaultReInitialiseValues = new HashMap<Class, ArrayList<Object>>(numInstalledClasses + nonPreInstalledClassesSize);
	}
	
	public void setInputStream(InputStream in) throws IOException{
		this.in = in;
	}
	
	/**
	 * Should be called when you've finished reading objects. It resets the readStatus
	 * of all SSObjects so that the next time you call readObject(), the objects 
	 * will be completely re-read from scratch instead of merely having their ssCode
	 * reference read from the stream.
	 * 
	 * Note that this method should only be called when the SSObjectOutputStream 
	 * used to write the output called writeDone().
	 */
	public void readDone(){
		readStatus.clear();
		objectsRead = 0;
		nonSSRead = 0;
	}
	
	// this reads in all objects that are members and sub-members of the first available
	// object written, and returns a reference to this object.
	public Object readObject() throws IOException{
		// add any SSObjects to storedObjects if necessary
		addStoredObjectsWaitingToBePut();
		
		int objectToBeReadCode = readInt();
		
		// first find out if any SSObjects should be ignored.
		if (objectToBeReadCode == IGNORE_THESE){
			while(true){
				int ssoToIgnoreCode = readInt();
				if (ssoToIgnoreCode == IGNORE_END){
					break;
				} else if (ssoToIgnoreCode < 0){	// ssoToIgnoreCode cannot be negative
					throw new IllegalArgumentException("Major error in bytes being read by SSObjectInputStream, byte array must have been corrupted. ssoToIgnoreCode cannot be negative");
				}
				readStatus.put(ssoToIgnoreCode, true);
			}
			objectToBeReadCode = readInt();	// re-initialise the first SSObject encode
		}
		// now read in the SSObjects
		if (objectToBeReadCode >= 0){
			SSObject sso = this.getStoredObject(objectToBeReadCode);
			//System.out.println("objectToBeReadCode == "+objectToBeReadCode+" sso == "+sso);
			if (readStatus.get(objectToBeReadCode) == null){
				// the SSObject hasn't been written yet, so read what class it is to create a new one
				short classIndex = readShort();
				// check that classIndex can possibly be valid
				if (classIndex > NULL_VALUE){
					throw new IllegalArgumentException("Major error in bytes being read by SSObjectInputStream, byte array must have been corrupted. classIndex ("+classIndex+") is > NULL_VALUE ("+NULL_VALUE+")");
				}
				if (classIndex == ((short)NULL_VALUE)){
					// the SSObject's class has never been written, so associate its class name with an index.
					String className = readUTF();
					classIndex = readShort();
					//System.out.println(this.getClass().getSimpleName()+": doing on-the-fly install of class "+className+" which has index "+classIndex);
					try{
						readClassIndexes.put(classIndex, Class.forName(className));
					}catch(ClassNotFoundException e){e.printStackTrace();}
				}
				
				
//				if (sso != null && installedClassIndexes.get(classIndex) != sso.getClass()){
//					System.err.println(this.getClass().getSimpleName()+": collision - installedClassIndexes.get(classIndex) == "+installedClassIndexes.get(classIndex)+", sso.getClass() == "+sso.getClass());
//					int ssCode = sso.getSSCode();
//					System.err.println(this.getClass().getSimpleName()+": sso.getSSCode() == "+ssCode + ", "+SSTools.decode(ssCode)[0] + ", "+SSTools.decode(ssCode)[1]);
//				}
				//System.err.println(this.getClass().getSimpleName()+": ");
				Class clazz = null;
				if (installedClassIndexes != null){
					clazz = installedClassIndexes.get(classIndex);
				}
				if (clazz == null){
					// the SSObject's class has not been written, but it may have been put in readClassIndexes in the above encode, so find the class.
					clazz = readClassIndexes.get(classIndex);
					if (clazz == null){
						// this encode should never execute. (may occur if UDP has been used and classes haven't been pre-installed and the class-with-string packet has been missed)
						System.err.println(this.getClass().getSimpleName()+": clazz == null in readObject(), must have missed a UDP packet containing its class index num or writeObject was called on an existing SSObjectOutputStream but readObject was called on a new SSObjectInputStream.  To stop this, preset installed classes using setInstalledClasses(). classIndex == "+classIndex+" readClassIndexes.size() == "+readClassIndexes.size());
						throw new IllegalArgumentException("Major error in bytes being read by SSObjectInputStream, byte array must have been corrupted.");
					}
				}
				if (sso == null || sso.getClass().equals(clazz) == false){
					// If sso != null, then sso.getClass() is not the same as clazz,
					// so we need to remove it from storedObjects.
					if (sso != null){
						synchronized (storedObjectsMutex){
							getStoredObjects().remove(sso);
						}
					}
					
					//System.out.println("\nmaking new object of class "+typeClass);
					try{
						sso = SSTools.newInstance(clazz);	// a new ssCode is made here, and then it is over-written so it's a waste of an ssCode, maybe I should fix it?
					}catch(Exception e){
						e.printStackTrace();
						return null;
					}
					SSCodeAllocator.setSSCodeForObject(sso, objectToBeReadCode);
					this.putStoredObject(sso);
					
//					System.err.println(this.getClass().getSimpleName()+": sso.getClass() == "+sso.getClass());
//					System.err.println(this.getClass().getSimpleName()+": this.getStoredObject(objectToBeReadCode).getClass() == "+this.getStoredObject(objectToBeReadCode).getClass().getSimpleName());
//					
//					System.err.println(this.getClass().getSimpleName()+": made a new instance of ("+classIndex+") "+sso.getClass());
//					int ssCode = sso.getSSCode();
//					System.err.println(this.getClass().getSimpleName()+": sso.getSSCode() == "+ssCode + ", "+SSTools.decode(ssCode)[0] + ", "+SSTools.decode(ssCode)[1]);
					
				}
				objectsRead++;
				readStatus.put(objectToBeReadCode, true);
				//System.out.print("reading a "+sso.getClass().getName());
				sso.readSS(this);
			}
			return sso;
		} else if (objectToBeReadCode == NULL_VALUE){
			return null;
		} else if (objectToBeReadCode > CLASS_INDEX_START_AUTO){	// the objectToBeReadCode cannot be less than CLASS_INDEX_START_AUTO.
			//System.out.print(", non-SS");
			Object obj = attemptReadNonSS(objectToBeReadCode);
			return obj;	// if its null there is a problem and an error will be thrown in readFields
		} else{
			throw new IllegalArgumentException("Major error in bytes being read by SSObjectInputStream, byte array must have been corrupted. objectToBeReadCode "+objectToBeReadCode+" doesn't match anything.");
		}
	}
	
	/**
	 * Reads in data written by SSObjectInputStream.writeFields(sso) and assigns the sso's fields. 
	 * @param sso
	 * @throws java.io.IOException
	 */
	public void readFields(SSObject sso) throws IOException{
		readFields(sso, sso.getClass());
	}
	
	/**
	 * Reads only the fields defined in clazz and above.  clazz must be sso's class or one of its super-classes.
	 * @param sso
	 * @param clazz
	 * @throws java.io.IOException
	 */
	protected void readFields(SSObject sso, Class clazz) throws IOException{
		fieldCache.ensureFieldsCached(clazz);
		Field[] objectFields = fieldCache.getStoredClassObjects().get(clazz);
		Field[] primitiveFields = fieldCache.getStoredClassPrimitives().get(clazz);
		
		//System.out.println(this.getClass().getSimpleName()+".readFields(): clazz: "+clazz.getName());
		for (int i = 0; i < objectFields.length; i++){
			Object fieldValue = null;
			try{
				//Object fieldValue = readObject();
				fieldValue = readObject();
				objectFields[i].set(sso, fieldValue);
			}catch(IllegalArgumentException e){
				System.err.println(this.getClass().getSimpleName()+": Problem assigning "+sso.getClass().getName()+"'s field "+objectFields[i].getName()+" which is a "+objectFields[i].getType().getName()+".");
//				if (fieldValue instanceof SSObject){
//					int ssCode = ((SSObject)fieldValue).getSSCode();
//					System.err.println(this.getClass().getSimpleName()+":((SSObject)fieldValue).getSSCode() == "+ssCode + ", "+SSTools.decode(ssCode)[0] + ", "+SSTools.decode(ssCode)[1]);
//				}
				throw e;
			}catch(IllegalAccessException e){
				System.err.println(this.getClass().getSimpleName()+": Problem assigning "+sso.getClass().getName()+"'s field "+objectFields[i].getName()+" which is a "+objectFields[i].getType().getName()+".");
				e.printStackTrace();
			}catch(NullPointerException e){
				System.err.println(this.getClass().getSimpleName()+": Problem assigning "+sso.getClass().getName()+"'s field "+objectFields[i].getName()+" which is a "+objectFields[i].getType().getName()+".");
				throw e;
			}
			//System.out.print(", done");
		}
		for (int i = 0; i < primitiveFields.length; i++){
			try{
				readPrimitiveField(sso, primitiveFields[i], primitiveFields[i].getType());
			}catch(IllegalAccessException e){
				e.printStackTrace();
			}
			//System.out.print(", done");
		}
	}
	
	void readPrimitiveField(SSObject sso, Field field, Class typeClass) throws IOException, IllegalAccessException{
		if (typeClass == Float.TYPE){
			field.setFloat(sso, readFloat());
		} else if (typeClass == Integer.TYPE){
			field.setInt(sso, readInt());
		} else if (typeClass == Boolean.TYPE){
			field.setBoolean(sso, readBoolean());
		} else if (typeClass == Long.TYPE){
			field.setLong(sso, readLong());
		} else if (typeClass == Double.TYPE){
			field.setDouble(sso, readDouble());
		} else if (typeClass == Byte.TYPE){
			field.setByte(sso, readByte());
		} else if (typeClass == Short.TYPE){
			field.setShort(sso, readShort());
		} else if (typeClass == Character.TYPE){
			field.setChar(sso, readChar());
		}
	}
	
	Object attemptReadNonSS(int objectToBeReadCode) throws IOException{
		nonSSRead++;
		if (objectToBeReadCode == STRING_VALUE){
			String objString = readUTF();
			//System.out.println("string");
			return objString;
		} else if (objectToBeReadCode == ARRAY_VALUE){
			//System.out.println("array");
			return readArrayField();
		} else if (objectToBeReadCode == POINT2D_FLOAT_VALUE){
			java.awt.geom.Point2D.Float point = new java.awt.geom.Point2D.Float(readFloat(), readFloat());	// don't have to make a new Point, can recycle
			return point;
		} else if (objectToBeReadCode == COLOR_VALUE){
			//System.out.println("colour");
			java.awt.Color objColor = new java.awt.Color(readInt());	// don't have to make a new Color, can recycle
			return objColor;
		} else if (objectToBeReadCode == FLOAT_VALUE){
			return readFloat();
		} else if (objectToBeReadCode == INT_VALUE){
			return readInt();
		} else if (objectToBeReadCode == BOOLEAN_VALUE){
			return readBoolean();
		} else if (objectToBeReadCode == LONG_VALUE){
			return readLong();
		} else if (objectToBeReadCode == DOUBLE_VALUE){
			return readDouble();
		} else if (objectToBeReadCode == SHORT_VALUE){
			return readShort();
		} else if (objectToBeReadCode == BYTE_VALUE){
			return readByte();
		} else if (objectToBeReadCode == CHARACTER_VALUE){
			return readChar();
		}
		return null;
	}
	
	Object readArrayField() throws IOException{
		int length = readInt();
		int typeInt = readInt();
		if (typeInt == FLOAT){
			float[] array = new float[length];
			for (int i = 0; i < length; i++){
				array[i] = readFloat();
			}
			return array;
		} else if (typeInt == INT){
			int[] array = new int[length];
			for (int i = 0; i < length; i++){
				array[i] = readInt();
			}
			return array;
		} else if (typeInt == BOOLEAN){
			boolean[] array = new boolean[length];
			for (int i = 0; i < length; i++){
				array[i] = readBoolean();
			}
			return array;
		} else if (typeInt == LONG){
			long[] array = new long[length];
			for (int i = 0; i < length; i++){
				array[i] = readLong();
			}
			return array;
		} else if (typeInt == DOUBLE){
			double[] array = new double[length];
			for (int i = 0; i < length; i++){
				array[i] = readDouble();
			}
			return array;
		} else if (typeInt == SHORT){
			short[] array = new short[length];
			for (int i = 0; i < length; i++){
				array[i] = readShort();
			}
			return array;
		} else if (typeInt == BYTE){
			byte[] array = new byte[length];
			for (int i = 0; i < length; i++){
				array[i] = readByte();
			}
			return array;
		} else if (typeInt == CHARACTER){
			char[] array = new char[length];
			for (int i = 0; i < length; i++){
				array[i] = readChar();
			}
			return array;
		} else{
			Object array = null;
			try{
				array = Array.newInstance(Class.forName(readUTF()), length);
			}catch(ClassNotFoundException e){
				e.printStackTrace();
			}
			for (int i = 0; i < length; i++){
				Array.set(array,i,readObject());
			}
			return array;
		}
	}
	protected WeakSSObjectMap<SSObject, Object> getStoredObjects(){
		return storedObjects;
	}
	@Override
	public SSObject getStoredObject(int ssCode){
		if (ssout != null){
			synchronized (storedObjectsMutex){
				return getStoredObjects().modifiedGet(ssCode);
			}
		}else{
			//System.out.println(this.getClass().getSimpleName()+" unsynch'ed getStoredObjects().modifiedGet(ssCode)!!!!!!!!!!!!!!!!!!!!");
			return getStoredObjects().modifiedGet(ssCode);
		}
	}
	
	@Override
	public void putStoredObject(SSObject sso){
		if (ssout != null){
			ssout.putStoredObjectSynchronized(sso);
		}
		//System.out.println(this.getClass().getSimpleName()+" unsynch'ed putStoredObject(sso)");
		this.putStoredObjectSynchronized(sso);
	}
	protected void putStoredObjectSynchronized(SSObject sso){
		synchronized (storedObjectsMutex){
			storedObjectsToBeAdded = true;
			storedObjectsWaitingToBePut.modifiedPut(sso);
		}
	}
	public void addStoredObjectsWaitingToBePut(){
		if (storedObjectsToBeAdded){
			synchronized (storedObjectsMutex){
				Set<SSObject> keySet = storedObjectsWaitingToBePut.keySet();
				for (SSObject sso : keySet){
					getStoredObjects().modifiedPut(sso);
				}
				storedObjectsWaitingToBePut.clear();
				storedObjectsToBeAdded = false;
			}
		}
	}

	@Override
	public FieldCache getFieldCache() {
		return fieldCache;
	}

	public void setFieldCache(FieldCache fieldCache) {
		this.fieldCache = fieldCache;
	}
	public int getNumStoredObjects(){
		return storedObjects.size();
	}
	// This can be used to find memory leaks:
	public void printStoredObjectSize(){
		System.out.println("this.storedObjects.size() == "+this.storedObjects.size()+", this.storedObjectsWaitingToBePut.size() == "+this.storedObjectsWaitingToBePut.size()+", this.ssout.storedObjects.size() == "+this.ssout.storedObjects.size()+", this.ssout.storedObjectsWaitingToBePut.size() == "+this.ssout.storedObjectsWaitingToBePut.size());
		if (this.storedObjects.size()%100 == 0){
			Set entries = this.storedObjects.keySet();
			Iterator iter = entries.iterator();
			int numClientWorldUpdates = 0;
			int numEventWrappers = 0;
			int numEvents = 0;
			while (iter.hasNext()){
				Object obj = iter.next();
				if (obj instanceof sydneyengine.ClientWorldUpdate){
					numClientWorldUpdates++;
					continue;
				}else if (obj instanceof sydneyengine.EventWrapper){
					numEventWrappers++;
					continue;
				}else if (obj instanceof sydneyengine.AbstractEvent){
					numEvents++;
					continue;
				}
				//System.out.println(""+obj);
			}
			System.out.println("numClientWorldUpdates == "+numClientWorldUpdates);
				System.out.println("numEventWrappers == "+numEventWrappers);
				System.out.println("numEvents == "+numEvents);
			
			//System.out.println("this.storedObjects.entrySet() == "+this.storedObjects.entrySet());
		}
	}
}