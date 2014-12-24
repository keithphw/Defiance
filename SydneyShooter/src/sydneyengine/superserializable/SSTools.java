package sydneyengine.superserializable;

//author: Keith Woodward
import com.grexengine.jgf.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * SSTools defines static utility methods. 
 * 
 * One of the most important is assignNextObjectCode(SSObject).  
 * It gives a unique ssCode. The ssCode has two components - the vmCode and the 
 * objectCode. The vmCode takes the lowest bits and the objectCode takes the highest bits. 
 * The number of bits that the vmCode takes is given by the variable shifts. 
 * So if shifts is 10, the maximum vmCode will be 2^6 - 1 which is 63.
 * The max objectCode will be 2^(31-6) which is 33,554,431. vmCodes must be unique 
 * to the java virtual machine that's participating in network communication with 
 * other vm's. This allows the vm to make its own unique ssCodes without
 * having to worry about what ssCodes other vm's are allocating. The objectCode 
 * is simply incremented by one with each call to assignNextObjectCode(SSObject) and then 
 * it's combined with the vmCode to get a unique ssCode. When the objectCode reaches 
 * the maximum it's reset to zero. To avoid assigning the same ssCode twice, the 
 * SSObject passed to the method assignNextObjectCode is stored in a WeakSSObjectMap
 * and before a new ssCode is allocated, it is checked that it doesn't already exist.  
 * If it does, the objectCode is again incremented by one, and the process repeats.
 * 
 * @author CommanderKeith
 */
public abstract class SSTools implements SSConstants {

	
	protected static int lastClassCode = CLASS_INDEX_START_AUTO;
	protected static boolean preInstallSSClasses = true;
	protected static ArrayList<Class> ssClassesToPreInstall = null;


// The below two methods are not thread safe with each other, it is the user's responsibility to make sure that there are no conflicts.
	public static ArrayList<Class> getSSClassesToPreInstall() {
		if (ssClassesToPreInstall == null) {
			setSSClassesToPreInstall(makeSortedSSClasses());
		}
		return ssClassesToPreInstall;
	}

	public static void setSSClassesToPreInstall(ArrayList<Class> newSSClassesToPreInstall) {
		ssClassesToPreInstall = newSSClassesToPreInstall;
	}

	

	public static ArrayList<Class> makeSortedSSClasses() {
		return makeSortedSSClasses(".*");
	}
	
	public static boolean checkAllClassesHaveNoArgConstructors(Class[] classes){
		boolean noProblems = true;
		for (Class clazz : classes){
			if (Modifier.isAbstract(clazz.getModifiers())){
				continue;
			}
			try{
				SSTools.newInstance(clazz);
			}catch(Exception e){
				e.printStackTrace();
				noProblems = false;
			}
		}
		return noProblems;
	}

	public static ArrayList<Class> makeSortedSSClasses(String regex) {
		ClassLocater classLocater = new ClassLocater();
		classLocater.addSkipPrefix("javax");
		Class[] classes = classLocater.getSubclassesOf(SSObject.class);
		// order them alphabetically
		boolean noChange = false;
		Class holder = null;
		while (noChange) {
			noChange = true;
			for (int i = 0; i < classes.length - 1; i++) {
				if (classes[i].getName().compareTo(classes[i + 1].getName()) > 0) {
					holder = classes[i];
					classes[i] = classes[i + 1];
					classes[i + 1] = holder;
					noChange = false;
				}
			}
		}
		
		assert checkAllClassesHaveNoArgConstructors(classes) : "Warning: either there is an exception in a constructor, or one or more classes is missing a public no-argument constructor. Insert a no-arg constructor and the SS streams will work.";

		System.out.println(SSTools.class.getSimpleName() + ".makeSortedSSClasses(): if you use the below code to make a list 'ssClassesToPreInstall' and\n" +
				"call SSTools.setSSClassesToPreInstall(ssClassesToPreInstall) then you won't have to use this method,\n" +
				"which adds a fair whack of time to start-up time and doesn't work when the program is launched using webstart (for some reason)..");
		ArrayList<Class> sortedSSClasses = new ArrayList<Class>(classes.length);
		for (Class clazz : classes) {
			// no need to add copies.
			if (sortedSSClasses.contains(clazz) == false) {
				//System.out.println(SSTools.class.getSimpleName()+".makeSortedSSClasses(): adding class "+clazz.getName());
				System.out.println("ssClassesToPreInstall.add(Class.forName(\"" + clazz.getName() + "\"));");
				sortedSSClasses.add(clazz);
			}
		}
		// Different VM's do different ordering so need to re-order alphabetically
		// bubble sort!
		boolean keepGoing = true;
		while (keepGoing) {
			keepGoing = false;
			for (int i = 0; i < sortedSSClasses.size() - 1; i++) {
				if (sortedSSClasses.get(i).getName().compareTo(sortedSSClasses.get(i + 1).getName()) > 0) {
					Class clazz = sortedSSClasses.get(i);
					sortedSSClasses.set(i, sortedSSClasses.get(i + 1));
					sortedSSClasses.set(i + 1, clazz);
					keepGoing = true;
				}
			}
		}
		System.out.println(SSTools.class.getSimpleName() + ".makeSortedSSClasses(): classes.length: " + classes.length + " sortedSSClasses.size(): " + sortedSSClasses.size());
		return sortedSSClasses;
	}

	public static Field[] getSortedDeclaredFields(Class classLevel) {
		Field[] declaredFields = classLevel.getDeclaredFields();
		boolean noChange = false;
		Field holder = null;
		while (noChange) {
			noChange = true;
			for (int i = 0; i < declaredFields.length - 1; i++) {
				if (declaredFields[i].getName().compareTo(declaredFields[i + 1].getName()) > 0) {
					holder = declaredFields[i];
					declaredFields[i] = declaredFields[i + 1];
					declaredFields[i + 1] = holder;
					noChange = false;
				}
			}
		}
		return declaredFields;
	}

	/* Note that cacheFields does not cache the SSAdapter's int field 'code', nor does it cache Transient or Static fields.
	However SSObjects that do not extend SSAdapter will probably have their SS code cached since it will just be some int field of the object.
	 *
	 */
	public static void cacheFields(Class classLevel, ArrayList<Field> primitiveFields, ArrayList<Field> objectFields) {
		//System.out.println(classLevel.getName());
		if (classLevel.isInterface()) {
			// all fields in an interfaces are final (& non-static) so nothing to cache.
			return;
		}
		Field[] declaredFields = SSTools.getSortedDeclaredFields(classLevel);
		FieldLoop:
		for (int i = 0; i < declaredFields.length; i++) {
			int modifiers = declaredFields[i].getModifiers();
			if (((modifiers & Modifier.TRANSIENT) == Modifier.TRANSIENT)) {
				//System.out.println("transient field: " + classLevel.getName() + "." + declaredFields[i].getName() + ", skipped");
				continue;
			}
			if (((modifiers & Modifier.STATIC) == Modifier.STATIC)) {
				//System.out.println("static field: " + classLevel.getName() + "." + declaredFields[i].getName() + ", skipped");
				continue;
			}
			Class typeClass = declaredFields[i].getType();
			//System.out.println("caching: "+typeClass.getName() + " " + classLevel.getName() + "." + declaredFields[i].getName());
			if (typeClass.isPrimitive()) {
				primitiveFields.add(declaredFields[i]);
			} else {
				objectFields.add(declaredFields[i]);
			}
		}
		Class superClass = classLevel.getSuperclass();

		if (superClass == SSAdapter.class) {
			// no need to write SSAdapter's only field 'int encode' since that's done already in the SSObjectInputStream.
			return;
		} else if (SSObject.class.isAssignableFrom(superClass)) {
			cacheFields(superClass, primitiveFields, objectFields);
		} else {
			return;
		}
	}

	public static void collectMemberSSObjects(SSObject sso, FieldCache fieldCache, WeakSSObjectMap<SSObject, Object> ssObjects, HashMap<Object, Object> nonSSObjects) {
		Field[] objectFields = fieldCache.getStoredClassObjects().get(sso.getClass());
		if (objectFields == null) {
			//System.out.println(""+sso.getClass().getName()+" caching*****************");
			fieldCache.cacheFields(sso);
			objectFields = fieldCache.getStoredClassObjects().get(sso.getClass());
		}

		for (int i = 0; i < objectFields.length; i++) {
			Object memberObject = null;
			try {
				memberObject = objectFields[i].get(sso);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (memberObject != null) {
				if (memberObject instanceof SSObject) {
					SSObject memberSSO = (SSObject) memberObject;
					SSObject ssoFromCode = ssObjects.modifiedGet(memberSSO.getSSCode());
					if (ssoFromCode == null) {
						ssObjects.modifiedPut(memberSSO);
						memberSSO.collectMemberSSObjects(fieldCache, ssObjects, nonSSObjects);
					}else if (ssoFromCode != memberSSO){
						System.err.println("SSTools: Warning, the ssCode "+memberSSO.getSSCode()+" corresponds to two objects memberSSO ("+memberSSO.getClass()+"), and ssoFromCode ("+ssoFromCode.getClass()+").");
						Thread.dumpStack();
					}
				} else {
					nonSSObjects.put(memberObject, memberObject);
				}
			}
		}
	}

	public static SSObject newInstance(Class clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor constructor = null;
		try {
			constructor = clazz.getConstructor();
		} catch (NoSuchMethodException e) {
			System.err.println("The class " + clazz.getName() + " is missing a public no-argument constructor. Insert one and the SS streams will work");
			throw e;
		}
		constructor.setAccessible(true);
		SSObject sso = null;
		try {
			sso = (SSObject) constructor.newInstance();
		} catch (InstantiationException e) {
			System.err.println(SSTools.class.getSimpleName()+": Could not create a new " + clazz.getName() + ".  This may be because it is missing a public no-argument constructor.");
			throw e;
		} catch (IllegalAccessException e) {
			System.err.println(SSTools.class.getSimpleName()+": uh oh, problem with class: " + clazz.getName());
			throw e;
		} catch (InvocationTargetException e) {
			System.err.println(SSTools.class.getSimpleName()+": uh oh, problem with class: " + clazz.getName());
			throw e;
		} catch (NullPointerException e) {
			System.err.println("SSObjectInputStream: uh oh, problem with class: " + clazz.getName());
			throw e;
		}
		return sso;
	}
}
