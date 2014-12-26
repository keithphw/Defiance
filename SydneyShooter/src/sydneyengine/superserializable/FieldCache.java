/*
 * FieldCache.java
 *
 * Created on 15 November 2007, 12:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine.superserializable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author CommanderKeith
 */
public class FieldCache {
	
	protected HashMap<Class, Field[]> storedClassPrimitives = null;	// the above class's primitive fields
	protected HashMap<Class, Field[]> storedClassObjects = null;	// the above class's Object fields
	
	/** Creates a new instance of FieldCache */
	public FieldCache() {
		this(100, 100);
	}
	public FieldCache(int storedClassPrimitivesMapSize, int storedClassObjectMapSize) {
		storedClassPrimitives = new HashMap<Class, Field[]>(storedClassPrimitivesMapSize);	// the above class's primitive fields
		storedClassObjects = new HashMap<Class, Field[]>(storedClassObjectMapSize);	// the above class's Object fields
	}
	public void ensureFieldsCached(Class clazz){
		if (!storedClassObjects.containsKey(clazz)){
			//System.out.println(sso.getClass().getName()+" caching*****************");
			cacheFields(clazz);
		}
	}
	public void ensureFieldsCached(SSObject sso){
		ensureFieldsCached(sso.getClass());
	}
	
	public void cacheFields(SSObject sso){
		cacheFields(sso.getClass());
	}
	public void cacheFields(Class ssoClass){
		ArrayList<Field> primitiveFields = new ArrayList<Field>(30);
		ArrayList<Field> objectFields = new ArrayList<Field>(30);
		SSTools.cacheFields(ssoClass, primitiveFields, objectFields);
		
		Field[] primitiveFieldsArray = new Field[primitiveFields.size()];
		primitiveFields.toArray(primitiveFieldsArray);
		AccessibleObject.setAccessible(primitiveFieldsArray, true);
		storedClassPrimitives.put(ssoClass, primitiveFieldsArray);
		
		Field[] objectFieldsArray = new Field[objectFields.size()];
		objectFields.toArray(objectFieldsArray);
		AccessibleObject.setAccessible(objectFieldsArray, true);
		storedClassObjects.put(ssoClass, objectFieldsArray);
	}
	public HashMap<Class, Field[]> getStoredClassObjects(){
		return storedClassObjects;
	}
	public HashMap<Class, Field[]> getStoredClassPrimitives(){
		return storedClassPrimitives;
	}
}
