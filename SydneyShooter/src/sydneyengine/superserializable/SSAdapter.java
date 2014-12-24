package sydneyengine.superserializable;

//author: Keith Woodward
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.security.*;

public abstract class SSAdapter implements SSObject, Cloneable {

	int code;

	public SSAdapter() {
		SSCodeAllocator.assignNextObjectCode(this);
	}

	public int getSSCode() {
		return code;
	}

	public void setSSCode(int code) {
		this.code = code;
	}

	public int hashCode() {
		return code;
	}

	public boolean equals(Object object) {
		if (object instanceof SSObject) {
			if (this.code == ((SSObject) object).getSSCode()) {
				return true;
			}
		}
		return false;
	}

	public void collectMemberSSObjects(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> ssObjects, HashMap<Object, Object> nonSSObjects) {
		SSTools.collectMemberSSObjects(this, fieldCache, ssObjects, nonSSObjects);
	}
	/**
	 * Simply calls out.writeFields(this).
	 * @param out
	 * @throws java.io.IOException
	 */
	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		out.writeFields(this);
	}
	/**
	 * Simply calls in.readFields(this).
	 * @param in
	 * @throws java.io.IOException
	 */
	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		in.readFields(this);
	}
	/*
	protected void finalize() throws Throwable{
	SSTools.recycleSSCode(this.getSSCode());
	}*/

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public SSObject deepClone(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects) {
		return deepClone(fieldCache, alreadyProcessedObjects, null);
	}

	public SSObject deepClone(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference) {
		SSObject clone = (SSObject) alreadyProcessedObjects.modifiedGet(this.getSSCode());
		if (clone != null) {
			// Since this object is already in alreadyProcessedObjects then it must have already been 
			// deep cloned, so we just add its reference, no need to deep clone it again.
			return clone;
		}
		if (memberObjectsToCopyByReference != null && memberObjectsToCopyByReference.contains(this)) {
			return this;
		}
		try {
			clone = (SSAdapter) super.clone();
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		alreadyProcessedObjects.modifiedPut(clone);
		Class clazz = clone.getClass();
		fieldCache.ensureFieldsCached(clazz);
		// Only have to get the Objects and not Primitives of the class since the primitives will have been copied by the Object.clone() method.
		Field[] objectFields = fieldCache.getStoredClassObjects().get(clazz);
		// Now we iterate thru the clones' Object fields, calling deepClone if those fields are SSObjects
		//System.out.println("clazz: "+clazz.getName());
		for (int i = 0; i < objectFields.length; i++) {
			try {
				//System.out.println("objectFields[i].getName(): "+objectFields[i].getName());
				// The object field of this object was cloned by the default Object class, but it was probably only a reference 
				// copy unless the Object was immutable (like a String), not a deep copy. So we will deepClone that object too.
				Object memberOfThis = objectFields[i].get(this);
				if (memberOfThis == null) {
					continue;
				}
				if (memberOfThis instanceof SSObject) {
					SSObject ssMemberOfThis = (SSObject) memberOfThis;
					objectFields[i].set(clone, ssMemberOfThis.deepClone(fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference));
				} else {
				//System.out.println(this.getClass().getSimpleName()+".deepClone(...): warning, a field isn't an SSObject, this might be a problem. objectFields[i].getName() == "+objectFields[i].getName());
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		//System.out.print(", done");
		}
		return clone;
	}

	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects) {
		makeEqualTo(model, fieldCache, alreadyProcessedObjects, null);
	}

	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference) {
		WeakSSObjectMap<SSObject, Object> thisObjectsSSObjects = new WeakSSObjectMap<SSObject, Object>();
		HashMap<Object, Object> thisObjectsNonSSObjects = new HashMap<Object, Object>();
		this.collectMemberSSObjects(fieldCache, thisObjectsSSObjects, thisObjectsNonSSObjects);
		//System.err.println(this.getClass().getSimpleName()+": thisObjectsSSObjects.size() == "+thisObjectsSSObjects.size()+", thisObjectsNonSSObjects.size() == "+thisObjectsNonSSObjects.size());
		makeEqualTo(model, fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference, thisObjectsSSObjects, thisObjectsNonSSObjects);
		//thisObjectsNonSSObjects = null;
		//thisObjectsSSObjects = null;
	}

	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference, WeakSSObjectMap<SSObject, Object> thisObjectsSSObjects, HashMap<Object, Object> thisObjectsNonSSObjects) {
		//this.setSSCode(model.getSSCode());
		SSCodeAllocator.setSSCodeForObject(this, model.getSSCode());

		SSObject thisObject = (SSObject) alreadyProcessedObjects.modifiedGet(this.getSSCode());
		if (thisObject != null) {
			// Since this object is already in alreadyProcessedObjects then it must have already had 
			// makeEqualTo called on it.
			return;
		}
		alreadyProcessedObjects.modifiedPut(this);
		Class clazz = model.getClass();
		assert this.getClass() == clazz : "The model object is a " + model.getClass().getName() + " ("+model.getSSCode()+")"+" but this object is a " + this.getClass().getName()+" ("+this.getSSCode()+")"+" .";
		fieldCache.ensureFieldsCached(clazz);

		Field[] objectFields = fieldCache.getStoredClassObjects().get(clazz);
		Field[] primitiveFields = fieldCache.getStoredClassPrimitives().get(clazz);

		//System.out.println(this.getClass().getSimpleName()+":clazz: "+clazz.getName());
		for (int i = 0; i < objectFields.length; i++) {
			//System.out.println(this.getClass().getSimpleName()+": this == "+this+", objectFields[i].getName() == "+objectFields[i].getName());
			try {
				//System.out.println(this.getClass().getSimpleName()+": objectFields[i].getName(): "+objectFields[i].getName());
				Object memberOfModel = objectFields[i].get(model);
				Object memberOfThis = objectFields[i].get(this);

				if (memberOfThis == null && memberOfModel == null) {
					continue;
				} else if (memberOfModel == null) {
					objectFields[i].set(this, null);
				} else {
					// Since we're here, memberOfModel is not null but memberOfThis may or may not be null.
					if (memberObjectsToCopyByReference != null && memberObjectsToCopyByReference.contains(memberOfModel)) {
						//System.err.println(this.getClass().getSimpleName()+".makeEqualTo(...): memberObjectsToCopyByReference.contains(memberOfModel) == true so doing reference copy for objectFields[i].getName() == "+objectFields[i].getName());
						objectFields[i].set(this, memberOfModel);
						continue;
					}
					if (memberOfModel instanceof SSObject) {
						SSObject ssMemberOfModel = (SSObject) memberOfModel;
						SSObject toBeMadeEqualToMemberOfThis = (SSObject) alreadyProcessedObjects.modifiedGet(ssMemberOfModel.getSSCode());
						if (toBeMadeEqualToMemberOfThis == null) {
							if (memberOfThis != null && memberOfThis instanceof SSObject && ((SSObject) memberOfThis).getSSCode() == ssMemberOfModel.getSSCode()) {
								((SSObject) memberOfThis).makeEqualTo(ssMemberOfModel, fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference, thisObjectsSSObjects, thisObjectsNonSSObjects);
								// No need to do objectFields[i].set(this, toBeMadeEqualToMemberOfThis) since 
								// memberOfThis already exists and is the same as the memberOfModel (since SS codes are the same).
								continue;
							}
							toBeMadeEqualToMemberOfThis = thisObjectsSSObjects.modifiedGet(ssMemberOfModel.getSSCode());
							if (toBeMadeEqualToMemberOfThis == null) {
								// Need to clone the ssMemberOfModel.
								// Note that deepClone isn't the way to go because we don't necessarily 
								// want to deepClone all of ssMemberOfModel's fields, 
								// we may just want to over-write the existing objects' primitives with new ones.
								//System.err.println(this.getClass().getSimpleName() + ".makeEqualTo(...): doing clone for objectFields[i].getName() == " + objectFields[i].getName());
								try{
									toBeMadeEqualToMemberOfThis = (SSObject) ssMemberOfModel.shallowCloneForMakeEqualTo(fieldCache);
								}catch(Exception e){
									e.printStackTrace();
									return;
								}
							}
							toBeMadeEqualToMemberOfThis.makeEqualTo(ssMemberOfModel, fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference, thisObjectsSSObjects, thisObjectsNonSSObjects);
						}
						objectFields[i].set(this, toBeMadeEqualToMemberOfThis);
					} else {
						// should do work here, because as is this obj's fields are just made == the model's fields.
						objectFields[i].set(this, memberOfModel);
					}
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		//System.out.print(", done");
		}
		for (int i = 0; i < primitiveFields.length; i++) {
			try {
				Field field = primitiveFields[i];
				Class typeClass = primitiveFields[i].getType();
				SSObject sso = this;
				if (typeClass == Float.TYPE) {
					field.setFloat(sso, field.getFloat(model));
				} else if (typeClass == Integer.TYPE) {
					field.setInt(sso, field.getInt(model));
				} else if (typeClass == Boolean.TYPE) {
					field.setBoolean(sso, field.getBoolean(model));
				} else if (typeClass == Long.TYPE) {
					field.setLong(sso, field.getLong(model));
				} else if (typeClass == Double.TYPE) {
					field.setDouble(sso, field.getDouble(model));
				} else if (typeClass == Byte.TYPE) {
					field.setByte(sso, field.getByte(model));
				} else if (typeClass == Short.TYPE) {
					field.setShort(sso, field.getShort(model));
				} else if (typeClass == Character.TYPE) {
					field.setChar(sso, field.getChar(model));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		//System.out.print(", done");
		}

	}


	public SSObject shallowCloneForMakeEqualTo(FieldCache fieldCache) throws Exception {
		SSObject clone = (SSObject) this.clone();
		//SSObject clone = SSTools.newInstance(this.getClass());
		
		// Must now set all object refs in this clone to null since they will 
		// be refs to this object's objects, and that isn't necessarily what we want.
		// makeEqualTo should fill clone's object refs back in with real objects.
		Class ssMemberOfModelClass = this.getClass();
		fieldCache.ensureFieldsCached(ssMemberOfModelClass);
		Field[] ssMemberOfModelObjectFields = fieldCache.getStoredClassObjects().get(ssMemberOfModelClass);
		for (int h = 0; h < ssMemberOfModelObjectFields.length; h++) {
			ssMemberOfModelObjectFields[h].set(clone, null);
		}
		return clone;
	}
}
