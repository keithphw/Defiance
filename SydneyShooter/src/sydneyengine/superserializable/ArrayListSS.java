package sydneyengine.superserializable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ArrayListSS<E> extends ArrayList<E> implements SSObject {

	int code;

	public ArrayListSS() {
		super();
		SSCodeAllocator.assignNextObjectCode(this);
	}

	public ArrayListSS(int capacity) {
		super(capacity);
		SSCodeAllocator.assignNextObjectCode(this);
	}

	@Override
	public int getSSCode() {
		return code;
	}

	@Override
	public void setSSCode(int code) {
		this.code = code;
	}

	@Override
	public void collectMemberSSObjects(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> ssObjects, HashMap<Object, Object> nonSSObjects) {
		for (E element : this) {
			if (element != null) {
				if (element instanceof SSObject) {
					SSObject memberSSO = (SSObject) element;
					if (ssObjects.modifiedGet(memberSSO.getSSCode()) == null) {
						ssObjects.modifiedPut(memberSSO);
						memberSSO.collectMemberSSObjects(fieldCache, ssObjects, nonSSObjects);
					}
				} else {
					nonSSObjects.put(element, element);
				}
			}
		}
	}

	/*public void collectMemberSSObjects(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> ssObjects){
	for (E element : this){
	if (element != null){
	if (element instanceof SSObject){
	SSObject memberSSO = (SSObject)element;
	if (ssObjects.modifiedGet(memberSSO.getSSCode()) == null){
	ssObjects.modifiedPut(memberSSO);
	memberSSO.collectMemberSSObjects(fieldCache, ssObjects);
	}
	}
	}
	}
	}
	public void collectMemberSSObjects(FieldCache fieldCache, java.util.Collection<SSObject> ssObjects){
	for (E element : this){
	if (element != null){
	if (element instanceof SSObject){
	SSObject memberSSO = (SSObject)element;
	if (!(ssObjects.contains(memberSSO))){
	ssObjects.add(memberSSO);
	memberSSO.collectMemberSSObjects(fieldCache, ssObjects);
	}
	}
	}
	}
	}
	public void collectMemberSSObjects(FieldCache fieldCache, java.util.Map<Integer, SSObject> ssObjects){
	for (E element : this){
	if (element != null){
	if (element instanceof SSObject){
	SSObject memberSSO = (SSObject)element;
	if (!(ssObjects.containsKey(memberSSO.getSSCode()))){
	ssObjects.put(memberSSO.getSSCode(), memberSSO);
	memberSSO.collectMemberSSObjects(fieldCache, ssObjects);
	}
	}
	}
	}
	}
	public void collectMemberSSObjects(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> ssObjects){
	for (E element : this){
	if (element != null){
	if (element instanceof SSObject){
	SSObject memberSSO = (SSObject)element;
	if (!(ssObjects.containsKey(memberSSO))){
	ssObjects.modifiedPut(memberSSO);
	memberSSO.collectMemberSSObjects(fieldCache, ssObjects);
	}
	}
	}
	}
	}*/
	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException {
		out.writeInt(code);
		out.writeInt(size());
		for (int i = 0; i < size(); i++) {
			Object tempObject = get(i);
			//System.out.print("\nwriting: "+tempObject.getClass().getName() + " " + getClass().getName() + ".get("+i+")");
			out.writeObject(tempObject);
		}
	}

	public void addReplace(int index, E element) {
		assert index <= size() : index+", "+size();
		if (size() > index) {
			set(index, element);
		} else {
			add(element);
		}
	}

	@Override
	public void readSS(SSObjectInputStream in) throws IOException {
		code = in.readInt();
		int numElements = in.readInt();
		ensureCapacity(numElements);
		if (numElements < size()) {
			removeRange(numElements, size());
		}
		//System.out.println("\nbegan reading in the ArrayListSS, numElements == "+numElements);
		for (int i = 0; i < numElements; i++) {
			E tempObject = (E) in.readObject();
			addReplace(i, tempObject);
		}
	/*
	for (int i = 0; i < size(); i++){
	if (get(i) == null){
	System.out.println(this.getClass().getSimpleName()+": get("+i+") == "+get(i));
	System.out.println(this.getClass().getSimpleName()+": size() == "+size());
	for (int j = 0; j < size(); j++){
	if (j == i){
	System.out.println(this.getClass().getSimpleName()+": get("+j+" == "+i+") == null ");
	continue;
	}
	if (get(j) == null){
	System.out.println(this.getClass().getSimpleName()+": get("+j+") == null too!");
	continue;
	}
	if (get(j) instanceof sydneyengine.demo.unreversible.UserEvent){
	sydneyengine.demo.unreversible.UserEvent jEvent = (sydneyengine.demo.unreversible.UserEvent)get(j);
	System.out.println(this.getClass().getSimpleName()+": get("+j+").getClass().getSimpleName() == "+get(j).getClass().getSimpleName()+", getTimeStampSeconds() == "+jEvent.getTimeStampSeconds()+", getCheckNum() == "+jEvent.getCheckNum());
	}
	}
	System.out.println(this.getClass().getSimpleName()+": c.size() == "+c.size());
	for (int j = 0; j < c.size(); j++){
	if (c.get(j) == null){
	System.out.println(this.getClass().getSimpleName()+": c.get("+j+") == null too!");
	continue;
	}
	System.out.println(this.getClass().getSimpleName()+": c.get("+j+").getClass().getSimpleName() == "+c.get(j).getClass().getSimpleName()+", getTimeStampSeconds() == "+c.get(j).getTimeStampSeconds()+", getCheckNum() == "+c.get(j).getCheckNum());
	}
	}
	}*/
	}

	public void reInitialise(SSObjectInputStream ssin) {
		clear();
	//maybe should do below if this object is recycled -
	// no, don't need to since this object does not use the SSObjectInputStream.defaultReInitialise() method!
	//encode = SSTools.assignNextObjectCode();
	}

	@Override
	public SSObject deepClone(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects) {
		return deepClone(fieldCache, alreadyProcessedObjects, null);
	}

	@Override
	public ArrayListSS deepClone(FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference) {
		ArrayListSS clone = (ArrayListSS) alreadyProcessedObjects.modifiedGet(this.getSSCode());
		if (clone != null) {
			// Since this object is already in alreadyProcessedObjects then it must have already been
			// deep cloned, so we just add its reference, no need to deep clone it again.
			return clone;
		}
		if (memberObjectsToCopyByReference != null && memberObjectsToCopyByReference.contains(this)) {
			return this;
		}

		clone = (ArrayListSS) super.clone();
		for (int j = 0; j < size(); j++) {
			if (get(j) == null) {
				clone.set(j, null);
				continue;
			}
			if (get(j) instanceof SSObject) {
				clone.set(j, ((SSObject) get(j)).deepClone(fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference));
			}else{
				// note: this method does not copy non-SSObjects (yet, but it should), they are copied by reference...
			}
		}
		assert diffCopies(this, clone);
		return clone;
	}
	protected static boolean diffCopies(ArrayListSS<?> one, ArrayListSS<?> two){
		assert one.size() == two.size() : "one.size() == "+one.size()+", two.size() == "+two.size();
		boolean ok = true;
		for (int i = 0; i < one.size(); i++){
			for (int j = 0; j < two.size(); j++){
				if (one.get(i) == null && two.get(j) == null){
					continue;
				}
				if (one.get(i) == two.get(j)){
					System.err.println(ArrayListSS.class.getSimpleName()+": "+one+".get("+i+") == "+two+".get("+j+"), one.size() == "+one.size()+", two.size() == "+two.size());
					ok = false;
				}
			}
		}
		return ok;
	}

	@Override
	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects) {
		makeEqualTo(model, fieldCache, alreadyProcessedObjects, null);
	}

	@Override
	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference) {
		WeakSSObjectMap<SSObject, Object> thisObjectsSSObjects = new WeakSSObjectMap<SSObject, Object>();
		HashMap<Object, Object> thisObjectsNonSSObjects = new HashMap<Object, Object>();
		this.collectMemberSSObjects(fieldCache, thisObjectsSSObjects, thisObjectsNonSSObjects);
		makeEqualTo(model, fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference, thisObjectsSSObjects, thisObjectsNonSSObjects);
	}

	@Override
	public void makeEqualTo(SSObject model, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> alreadyProcessedObjects, ArrayList<Object> memberObjectsToCopyByReference, WeakSSObjectMap<SSObject, Object> thisObjectsSSObjects, HashMap<Object, Object> thisObjectsNonSSObjects) {
		//this.setSSCode(model.getSSCode());
		SSCodeAllocator.setSSCodeForObject(this, model.getSSCode());

		SSObject thisObject = alreadyProcessedObjects.modifiedGet(this.getSSCode());
		if (thisObject != null) {
			// Since this object is already in alreadyProcessedObjects then it must have already had
			// makeEqualTo called on it.
			return;
		}
		alreadyProcessedObjects.modifiedPut(this);

		Class clazz = model.getClass();
		assert this.getClass() == clazz : "The model object is a " + model.getClass().getName() + " but this object is a " + this.getClass().getName() + ".";
		ArrayListSS<E> modelList = (ArrayListSS<E>) model;
		//System.out.println(this.getClass().getSimpleName()+": makeEqualTo, modelList.size() == "+modelList.size());
		int j = 0;
		for (; j < modelList.size(); j++) {
			//System.out.println(this.getClass().getSimpleName()+": modelList.get(j).getClass().getSimpleName() == "+modelList.get(j).getClass().getSimpleName());
			if (modelList.get(j) == null) {
				this.addReplace(j, null);
				continue;
			} else if (memberObjectsToCopyByReference != null && memberObjectsToCopyByReference.contains(modelList.get(j))) {
				this.addReplace(j, modelList.get(j));
			} else if (modelList.get(j) instanceof SSObject) {
				SSObject ssModelListElement = (SSObject) modelList.get(j);
				SSObject toBeMadeEqualToMemberOfThis = alreadyProcessedObjects.modifiedGet(ssModelListElement.getSSCode());
				if (toBeMadeEqualToMemberOfThis == null) {
					if (size() > j && get(j) instanceof SSObject && ssModelListElement.getSSCode() == ((SSObject) get(j)).getSSCode()) {
						SSObject ssThisListElement = (SSObject) get(j);
						ssThisListElement.makeEqualTo(ssModelListElement, fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference, thisObjectsSSObjects, thisObjectsNonSSObjects);
						continue;
					}
					toBeMadeEqualToMemberOfThis = thisObjectsSSObjects.modifiedGet(ssModelListElement.getSSCode());//((SSObject)memberOfModel).deepClone(fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference);
					if (toBeMadeEqualToMemberOfThis == null) {
						// Need to clone it the ssMemberOfModel.
						// Note that deepClone isn't the way to go because we don't necessarily 
						// want to deepClone all of ssMemberOfModel's fields.
						//System.err.println(this.getClass().getSimpleName() + ".makeEqualTo(...): doing clone of ssModelListElement == " + ssModelListElement);
						try {
							toBeMadeEqualToMemberOfThis = ssModelListElement.shallowCloneForMakeEqualTo(fieldCache);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					toBeMadeEqualToMemberOfThis.makeEqualTo(ssModelListElement, fieldCache, alreadyProcessedObjects, memberObjectsToCopyByReference, thisObjectsSSObjects, thisObjectsNonSSObjects);
				}
				//System.out.println(this.getClass().getSimpleName()+": this.addReplace(j, (E)ssModelListElement.deepClone(fieldCache, alreadyProcessedObjects));");
				this.addReplace(j, (E) toBeMadeEqualToMemberOfThis);
			} else {
				// not dealing with non-ssObjects here properly, maybe should clone them instead of just assigning them?...
				//System.out.println(this.getClass().getSimpleName()+".makeEqualTo(...): warning, an element isn't an SSObject, so assigning the model's field to this object's rather than doing a deep clone. this might be a problem. modelList.get(j).getClass().getName() == "+modelList.get(j).getClass().getName());
				this.addReplace(j, modelList.get(j));
			}
		}
		for (; j < this.size(); j++) {
			this.remove(j);
			j--;
		}
		assert ((ArrayListSS)model).size() == this.size();
	}

	@Override
	public SSObject shallowCloneForMakeEqualTo(FieldCache fieldCache){
		SSObject clone = new ArrayListSS();
		// makeEqualTo should fill clone's object refs back in with real objects, so we'll just return an empty list.
		return clone;
	}
}
