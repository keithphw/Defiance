package sydneyengine.superserializable;

import java.util.ArrayList;

public interface SSObjectStream extends SSConstants{
	public void setInstalledClasses(ArrayList<Class> classList);
	public FieldCache getFieldCache();
	//public WeakSSObjectMap<SSObject, Object> getStoredObjects();
	public SSObject getStoredObject(int ssCode);	// all code should interact with the storedObjects using this method.
	public void putStoredObject(SSObject sso);		// all code should interact with the storedObjects using this method.
	public void syncStoredObjectsWith(SSObjectStream ssStream);	// this can only be set once and should be done just after both streams are constructed. You only need to call this method on one stream and both are sync'ed'
	//public void putStoredObjectSynchronized(SSObject sso);	// this is called by putStoredObject() if there is a non-null sync-stream (which is set using syncStoredObjectsWith())
	
	
}