package sydneyengine.superserializable;

//author: Keith Woodward

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

public class SSObjectOutputStream extends DataOutputStream implements SSObjectStream{
	
	protected FieldCache fieldCache = null;
	
	protected HashMap<Integer, Boolean> writtenStatus = new HashMap<Integer, Boolean>(1000);
	
	volatile protected WeakSSObjectMap<SSObject, Object> storedObjects;
	volatile protected WeakSSObjectMap<SSObject, Object> storedObjectsWaitingToBePut;
	volatile protected Object storedObjectsMutex = new Object();
	volatile protected boolean storedObjectsToBeAdded = false;
	
	protected HashMap<Class, Short> installedClassIndexes;// = new HashMap<Class, Short>(100);	// classes and their corresponding indexes that have been written since this stream was created
	protected HashMap<Class, Short> writtenClassIndexes;// = new HashMap<Class, Short>(100);	// classes and their corresponding indexes that have been written since this stream was created
	
	protected int objectsWritten = 0;
	protected int nonSSWritten = 0;
	
	protected SSObjectInputStream ssin = null;
	// This method syncs the 2 streams storedObjects wih each other. It only needs to be called once, ie once you call a.syncStoredObjectsWith(b), you don't need to call b.syncStoredObjectsWith(a).
	public void syncStoredObjectsWith(SSObjectStream stream){
		if (stream instanceof SSObjectInputStream == false){
			throw new IllegalArgumentException(this.getClass().getSimpleName()+" can only be sync'ed with an SSObjectInputStream, not an "+stream.getClass().getSimpleName());
		}
		if (ssin != stream){
			this.ssin = ((SSObjectInputStream)stream);
			System.out.println(this.getClass().getSimpleName()+": stream sync'ed");
			stream.syncStoredObjectsWith(this);
		}
	}

	
	// must be done if using the UDP protocol since then there is no stuff up if a crucial packet that binds a class to an automatically generated index goes missing.
	public void setInstalledClasses(ArrayList<Class> classList){
		if (installedClassIndexes == null){
			installedClassIndexes = new HashMap<Class, Short>(classList.size()*2);
		}else{
			installedClassIndexes.clear();
		}
		short classIndex = CLASS_INDEX_START_INSTALL;
		for (int i = 0; i < classList.size(); i++){
			installedClassIndexes.put(classList.get(i), classIndex);
			classIndex++;
			this.getFieldCache().ensureFieldsCached(classList.get(i));
			//System.out.println(this.getClass().getSimpleName()+": installed class "+i+" - "+classList.get(i).getSimpleName());
		}
	}
	
	private short lastClassIndexToAllocate = CLASS_INDEX_START_AUTO;
	public short createNewClassIndex(){
		lastClassIndexToAllocate--;
		return lastClassIndexToAllocate;
	}
	
	// a convenience constructor which creates an SSObjectOutputStream.  Should have setInputStream(InputStream) called on it.
	public SSObjectOutputStream() throws IOException{
		this(new ByteArrayOutputStream(), 1000, 100);
	}
	public SSObjectOutputStream(OutputStream out) throws IOException{
		this(out, 1000, 100);
	}
	public SSObjectOutputStream(OutputStream out, int storedObjectCodesMapSize, int nonPreInstalledClassesSize) throws IOException{
		super(out);
		
		int numInstalledClasses = 0;
		if (SSTools.preInstallSSClasses){
			numInstalledClasses = SSTools.getSSClassesToPreInstall().size();
		}
		installedClassIndexes = new HashMap<Class, Short>(numInstalledClasses);	// classes and their corresponding indexes that have been written since this stream was created
		writtenClassIndexes = new HashMap<Class, Short>(nonPreInstalledClassesSize);	// classes and their corresponding indexes that have been written since this stream was created
		fieldCache = new FieldCache(numInstalledClasses*2 + nonPreInstalledClassesSize, numInstalledClasses*2 + nonPreInstalledClassesSize);
		
		if (SSTools.preInstallSSClasses){
			setInstalledClasses(SSTools.getSSClassesToPreInstall());
		}
		
		
		writtenStatus = new HashMap<Integer, Boolean>(storedObjectCodesMapSize);	// null == false
		storedObjects = new WeakSSObjectMap<SSObject, Object>(2000);
		storedObjectsWaitingToBePut = new WeakSSObjectMap<SSObject, Object>(2000);
	}
	
	public void setOutputStream(OutputStream out) throws IOException{
		this.out = out;
	}
	
	public void writeObject(Object obj) throws IOException{
		writeObject(obj, null);
	}
	// all SSObjects in objectsToIgnore are not touched - ie they are not written and any SSObjects that have fields which contain them are left untouched when they are read back (thru SSObjectInputStream.readSS()).
	public void writeObject(Object obj, ArrayList<SSObject> ssObjectsToIgnore) throws IOException{	// all SSObject writes go thru this method. this is the method you call when you want to actually write an object to send it somewhere.
		// add any SSObjects to storedObjects if necessary
		addStoredObjectsWaitingToBePut();
		// write the SSObjects that will be ignored.
		if (ssObjectsToIgnore != null){
			writeInt(IGNORE_THESE);
			for(int i = 0; i < ssObjectsToIgnore.size(); i++){
				int ssoToIgnoreCode = ssObjectsToIgnore.get(i).getSSCode();
				writtenStatus.put(ssoToIgnoreCode, true);
				writeInt(ssoToIgnoreCode);
			}
			writeInt(IGNORE_END);	// signifies that there are no more SSObjects to ignore.
		}
		if (obj == null){
			writeInt(NULL_VALUE);
		} else if (obj instanceof SSObject){
			//System.out.println(this.getClass().getSimpleName()+": writeObject() on: "+obj.getClass());
			SSObject sso = (SSObject)obj;
			//System.out.println("sso.getSSCode() == "+sso.getSSCode()+" sso == "+sso);
			writeInt(sso.getSSCode());	// write the object's encode which is its 'reference' for the purposes of the SS streams
			if (writtenStatus.get(sso.getSSCode()) == null){
				// the object hasn't been written yet so write what class it is in case it needs to be created and then write its fields.
				// the object only needs to be written once, after that we just write its SS encode to reference it.
				objectsWritten++;
				
				Class ssoClass = sso.getClass();
				
				Short classIndex = installedClassIndexes.get(ssoClass);
				if (classIndex != null){
					writeShort(classIndex.shortValue());
				} else{
					// this else block should never be executed if the byte packets are transported using UDP.  You should do setInstalledClasses() since UDP is lossy and the packet needed to initialise the class indexes could go missing.
					classIndex = writtenClassIndexes.get(ssoClass);
					if (classIndex != null){
						writeShort(classIndex.shortValue());
					} else{
						// the following only needs doing once for each new class encountered by this stream, after this, only an int is sent
						short shortClassIndex = createNewClassIndex();
						System.err.println("on-the-fly install of: "+ssoClass.getName()+" with index "+shortClassIndex);
						writtenClassIndexes.put(ssoClass, shortClassIndex);
						writeShort((short)NULL_VALUE);	// to signify that a new class is being linked to an index
						writeUTF(ssoClass.getName());	// the class
						writeShort(shortClassIndex);	// and its new index
					}
				}
				writtenStatus.put(sso.getSSCode(), true);	// to signify that the object has been written already
				
				SSObject oldSSO = storedObjects.modifiedGet(sso.getSSCode());
//				if (oldSSO != null && oldSSO.getClass() != sso.getClass()){
//					System.err.println(this.getClass().getSimpleName()+": **collision - oldSSO.getClass() == "+oldSSO.getClass()+", sso.getClass() == "+sso.getClass());
//					int ssCode = sso.getSSCode();
//					int oldSSOssCode = oldSSO.getSSCode();
//					System.err.println(this.getClass().getSimpleName()+": **sso.getSSCode() == "+ssCode + ", "+SSTools.decode(ssCode)[0] + ", "+SSTools.decode(ssCode)[1]);
//					System.err.println(this.getClass().getSimpleName()+": **oldSSOssCode.getSSCode() == "+oldSSOssCode + ", "+SSTools.decode(oldSSOssCode)[0] + ", "+SSTools.decode(oldSSOssCode)[1]);
//					java.awt.Toolkit.getDefaultToolkit().beep();
//				}
				
				putStoredObject(sso);
				sso.writeSS(this);
			}
		} else{
			if (attemptWriteNonSS(obj) == false){
				//System.out.print(", failed\n");
				throw new IllegalArgumentException("Can not super-serialize "+obj.getClass().getName()+" since neither it nor its super classes implement SSObject.");//classLevel.getName() + "." + declaredFields[i].getName() + " is not an SSObject, it is a " + typeClass.getName());
			} else{
				//System.out.print(", non-SS");
			}
		}
	}
	
	/**
	 * Should be called when you've finished writing objects. It resets the writtenStatus
	 * of all SSObjects so that the next time you call writeObject(), the objects 
	 * will be completely re-written from scratch instead of merely having their ssCode
	 * reference written to the stream.
	 * 
	 * Note that the SSObjectInputStream used to read the output should call 
	 * readDone after it has finished reading objects.
	 * @throws java.io.IOException
	 */
	public void writeDone() throws IOException{
		writtenStatus.clear();
		
		flush();
		
		nonSSWritten = 0;
		objectsWritten = 0;
	}
	
	/**
	 * Writes all fields defined in sso's class and all of its super-classes up 
	 * to SSObject. 
	 * Note: if a class calls this method in its writeObject method, 
	 * and it has a sub-class whose writeObject method calls super.writeObject(), 
	 * then all fields of the sub-class and above will be written, even though 
	 * the writeFields method is called in thge super-class.  
	 * So sub-classes should avoid calling readFields if one of their super-classes 
	 * has already called it, since otherwise the objects fields will be written twice.
	 * @param sso
	 * @throws java.io.IOException
	 */
	public void writeFields(SSObject sso) throws IOException{
		writeFields(sso, sso.getClass());
	}
	
	/**
	 * Writes only the fields defined in clazz and above.  clazz must be sso's class or one of its super-classes.
	 * @param sso
	 * @param clazz
	 * @throws java.io.IOException
	 */
	protected void writeFields(SSObject sso, Class clazz) throws IOException{
		getFieldCache().ensureFieldsCached(clazz);
		Field[] objectFields = getFieldCache().getStoredClassObjects().get(clazz);
		Field[] primitiveFields = getFieldCache().getStoredClassPrimitives().get(clazz);
		
		//System.out.println("clazz: "+clazz.getName());
		for (int i = 0; i < objectFields.length; i++){
			try{
				//System.out.println("objectFields[i].getName(): "+objectFields[i].getName());
				Object tempObject = objectFields[i].get(sso);
				writeObject(tempObject);
			}catch(IllegalAccessException e){
				System.err.println(this.getClass().getSimpleName()+": Error in field "+objectFields[i].getName()+" of class "+clazz.getName()+" in object "+sso);
				e.printStackTrace();
			}catch(IOException e){
				System.err.println(this.getClass().getSimpleName()+": Error in field "+objectFields[i].getName()+" of class "+clazz.getName()+" in object "+sso);
				throw e;
			}catch(IllegalArgumentException e){
				System.err.println(this.getClass().getSimpleName()+": Error in field "+objectFields[i].getName()+" of class "+clazz.getName()+" in object "+sso);
				throw e;
			}
			//System.out.print(", done");
		}
		for (int i = 0; i < primitiveFields.length; i++){
			try{
				writePrimitiveField(sso, primitiveFields[i], primitiveFields[i].getType());
			}catch(IllegalAccessException e){
				e.printStackTrace();
			}
			//System.out.print(", done");
		}
	}
	
	void writePrimitiveField(SSObject sso, Field field, Class typeClass) throws IOException, IllegalAccessException{
		if (typeClass == Float.TYPE){
			writeFloat(field.getFloat(sso));
		} else if (typeClass == Integer.TYPE){
			writeInt(field.getInt(sso));
		} else if (typeClass == Boolean.TYPE){
			writeBoolean(field.getBoolean(sso));
		} else if (typeClass == Long.TYPE){
			writeLong(field.getLong(sso));
		} else if (typeClass == Double.TYPE){
			writeDouble(field.getDouble(sso));
		} else if (typeClass == Byte.TYPE){
			writeByte(field.getByte(sso));
		} else if (typeClass == Short.TYPE){
			writeShort(field.getShort(sso));
		} else if (typeClass == Character.TYPE){
			writeChar(field.getChar(sso));
		}
	}
	
	boolean attemptWriteNonSS(Object obj) throws IOException{
		nonSSWritten++;
		if (obj instanceof String){
			String objString = ((String)obj);
			writeInt(STRING_VALUE);
			writeUTF(objString);
			return true;
			/*String objString = ((String)obj);
			writeInt(STRING_VALUE);
			writeInt(objString.length());
			for (int i = 0; i < objString.length; i++){
				writeChar(objString.charAt(i));
			}
			return true;*/
		} else if (obj.getClass().isArray()){
			writeInt(ARRAY_VALUE);
			writeArrayField(obj);
			return true;
		} else if (obj instanceof java.awt.geom.Point2D.Float){
			java.awt.geom.Point2D.Float point = ((java.awt.geom.Point2D.Float)obj);
			writeInt(POINT2D_FLOAT_VALUE);
			writeFloat(point.x);
			writeFloat(point.y);
			return true;
		} else if (obj instanceof java.awt.Color){
			java.awt.Color objColor = ((java.awt.Color)obj);
			writeInt(COLOR_VALUE);
			writeInt(objColor.getRGB());
			return true;
		} else if (obj instanceof Float){
			writeInt(FLOAT_VALUE);
			writeFloat((Float)obj);
			return true;
		} else if (obj instanceof Integer){
			writeInt(INT_VALUE);
			writeInt((Integer)obj);
			return true;
		} else if (obj instanceof Boolean){
			writeInt(BOOLEAN_VALUE);
			writeBoolean((Boolean)obj);
			return true;
		} else if (obj instanceof Long){
			writeInt(LONG_VALUE);
			writeLong((Long)obj);
			return true;
		} else if (obj instanceof Double){
			writeInt(DOUBLE_VALUE);
			writeDouble((Double)obj);
			return true;
		} else if (obj instanceof Short){
			writeInt(SHORT_VALUE);
			writeShort((Short)obj);
			return true;
		} else if (obj instanceof Byte){
			writeInt(BYTE_VALUE);
			writeByte((Byte)obj);
			return true;
		} else if (obj instanceof Character){
			writeInt(CHARACTER_VALUE);
			writeChar((Character)obj);
			return true;
		}
		return false;
	}
	
	void writeArrayField(Object arrayObj) throws IOException{
		try{
			int length = Array.getLength(arrayObj);
			writeInt(length);
			Class typeClass = arrayObj.getClass().getComponentType();
			if (typeClass.isPrimitive()){
				if (typeClass == Float.TYPE){
					writeInt(FLOAT);
					for (int i = 0; i < length; i++){
						writeFloat(Array.getFloat(arrayObj, i));
					}
				} else if (typeClass == Integer.TYPE){
					writeInt(INT);
					for (int i = 0; i < length; i++){
						writeInt(Array.getInt(arrayObj, i));
					}
				} else if (typeClass == Boolean.TYPE){
					writeInt(BOOLEAN);
					for (int i = 0; i < length; i++){
						writeBoolean(Array.getBoolean(arrayObj, i));
					}
				} else if (typeClass == Long.TYPE){
					writeInt(LONG);
					for (int i = 0; i < length; i++){
						writeLong(Array.getLong(arrayObj, i));
					}
				} else if (typeClass == Double.TYPE){
					writeInt(DOUBLE);
					for (int i = 0; i < length; i++){
						writeDouble(Array.getDouble(arrayObj, i));
					}
				} else if (typeClass == Short.TYPE){
					writeInt(SHORT);
					for (int i = 0; i < length; i++){
						writeShort(Array.getShort(arrayObj, i));
					}
				} else if (typeClass == Byte.TYPE){
					writeInt(BYTE);
					for (int i = 0; i < length; i++){
						writeByte(Array.getByte(arrayObj, i));
					}
				} else if (typeClass == Character.TYPE){
					writeInt(CHARACTER);
					for (int i = 0; i < length; i++){
						writeChar(Array.getChar(arrayObj, i));
					}
				}
			} else{
				writeInt(ARRAY_VALUE);	// note that here I write any old int, ARRAY_VALUE is arbitrary.  Must write an int so the readArrayField can do readInt().
				writeUTF(typeClass.getName());
				for (int i = 0; i < length; i++){
					writeObject(Array.get(arrayObj, i));
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	protected WeakSSObjectMap<SSObject, Object> getStoredObjects(){
		return storedObjects;
	}
	public SSObject getStoredObject(int ssCode){
		if (ssin != null){
			synchronized (storedObjectsMutex){
				return getStoredObjects().modifiedGet(ssCode);
			}
		}else{
			//System.out.println(this.getClass().getSimpleName()+" unsynch'ed getStoredObjects().modifiedGet(ssCode)!!!!!!!!!!!!!!!!!!!!");
			return getStoredObjects().modifiedGet(ssCode);
		}
	}
	public void putStoredObject(SSObject sso){
		if (ssin != null){
			ssin.putStoredObjectSynchronized(sso);
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
		System.out.println("this.storedObjects.size() == "+this.storedObjects.size()+", this.storedObjectsWaitingToBePut.size() == "+this.storedObjectsWaitingToBePut.size()+", this.ssin.storedObjects.size() == "+this.ssin.storedObjects.size()+", this.ssin.storedObjectsWaitingToBePut.size() == "+this.ssin.storedObjectsWaitingToBePut.size());
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