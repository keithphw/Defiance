package sydneyengine.superserializable;

import java.util.ArrayList;
import java.util.HashMap;
//author: Keith Woodward

public interface SSObject extends SSConstants{
	// note that SS classes can not be inner classes... which is quite annoying!
	public int getSSCode();	// returns a number >= zero that is unique.
	public void setSSCode(int code);
	public void writeSS(SSObjectOutputStream out) throws java.io.IOException;
	public void readSS(SSObjectInputStream in) throws java.io.IOException;
	public void collectMemberSSObjects(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> ssObjects, HashMap<Object, Object> nonSSObjects);
	public SSObject deepClone(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference);
	public SSObject deepClone(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects);
	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference);
	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects);
	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference, WeakSSObjectMap<SSObject, Object> thisObjectsSSObjects, HashMap<Object, Object> thisObjectsNonSSObjects);
	public SSObject shallowCloneForMakeEqualTo(FieldCache fieldCache) throws Exception;
}