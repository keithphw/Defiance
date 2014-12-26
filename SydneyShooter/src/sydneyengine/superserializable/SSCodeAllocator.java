/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.superserializable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author CommanderKeith
 */
public class SSCodeAllocator {

	static HashMap<Class, Integer> map = new HashMap<Class, Integer>(200);
	protected final static int shifts = 10;
	public final static int MAX_VMCODE = (int) Math.pow(2, shifts) - 1;		// small number
	public final static int MAX_OBJECTCODE = (int) Math.pow(2, 31 - shifts);		// big number
	// The static maps hold SSObjects for the purpose of keeping track of which 
	// ssCodes are available and which are still being used. 
	// There are many because each ssCode is used by the head world object and its tail world copy,
	// of which there could be one or more that haven't been garbage collected.
	static ArrayList<WeakSSObjectMap<SSObject, Object>> ssObjectMaps = new ArrayList<WeakSSObjectMap<SSObject, Object>>();
	static{
		// need to add at least one WeakSSObjectMap to the ssObjectMaps list.
		ssObjectMaps.add(new WeakSSObjectMap<SSObject, Object>());
	}
	static int ssCodesIssuedBetweenPrintWarnings = Math.round(MAX_OBJECTCODE / 10);
	protected static int lastObjectCode = -1;
	private static Object lastObjectCodeMutex = new Object();
	protected static int vMCode = 0;
	
	public static int encode(int bigNum, int smallNum) {
		int ab = bigNum;
		ab = ab << shifts;
		ab += smallNum;
		return ab;
	}

	public static int[] decode(int ab) {
		int[] ints = new int[2];
		ints[0] = ab >> shifts;
		ints[1] = ab - (ints[0] << shifts);
		return ints;
	}

	public static int getVMCode() {
		return vMCode;
	}

	public static void setVMCode(int newVMCode) {
		if (vMCode > MAX_VMCODE) {
			throw new IllegalArgumentException("newVMCode (" + newVMCode + ") is greater than MAX_VM_NUM (" + MAX_VMCODE + ")!");
		}
		vMCode = newVMCode;
	}

	public static void assignNextObjectCode(SSObject sso) {
		synchronized (lastObjectCodeMutex) {
			// Uncomment this code and the code marked below if you want to 
			// see a print-out of how many SSObjects have been created for each class:
//			Integer numSSObjects = map.get(sso.getClass());
//			if (numSSObjects == null){
//				map.put(sso.getClass(), 1);
//			}else{
//				map.put(sso.getClass(), ++numSSObjects);
//			}

			int newCode = 0;
			MainLoop:
			while (true) {
				lastObjectCode++;
				if (lastObjectCode == MAX_OBJECTCODE) {
					System.out.println(SSTools.class.getSimpleName() + ": lastObjectCode == " + lastObjectCode + " out of a maximum of MAX_CODE_NUM (" + MAX_OBJECTCODE + "), so resetting to zero. ssObjectMaps.size() == "+ssObjectMaps.size());
					lastObjectCode = 0;
				}
				newCode = encode(lastObjectCode, vMCode);
				for (int i = 0; i < ssObjectMaps.size(); i++){
					if (ssObjectMaps.get(i).modifiedGet(newCode) != null){
						continue MainLoop;
					}
				}
				break MainLoop;
			}
			setSSCodeForObject(sso, newCode);
			
			//Uncomment this code and the code above if you want to see a 
			//print-out of how many SSObjects have been created for each class:
//			if (lastObjectCode % ssCodesIssuedBetweenPrintWarnings == 0) {
//				System.out.println(SSTools.class.getSimpleName() + ": lastObjectCodeToAllocate == " + lastObjectCode + " out of a maximum of MAX_CODE_NUM (" + MAX_OBJECTCODE + "). ssObjects.size() == "+ssObjectMaps.size());
//				Set<Map.Entry<Class, Integer>> entries = map.entrySet();
//				Iterator<Map.Entry<Class, Integer>> iter = entries.iterator();
//				while(iter.hasNext()){
//					Map.Entry<Class, Integer> entry = iter.next();
//					System.out.println(entry.getKey().getSimpleName()+": has "+entry.getValue());
//				}
//			}
		}
	}
	
	public static void setSSCodeForObject(SSObject sso, int ssCode){
		synchronized (lastObjectCodeMutex) {
			int oldSSCode = sso.getSSCode();
			// sso's old ssCode will be changed so we need to remove it from the maps.
			for (int i = 0; i < ssObjectMaps.size(); i++){
				if (ssObjectMaps.get(i).modifiedGet(oldSSCode) == sso){
					ssObjectMaps.get(i).remove(sso);
					// the object would only be stored in one map, so can break 
					// once it's been removed from that map.
					break;
				}
			}
			
			sso.setSSCode(ssCode);
			
			for (int i = 0; i < ssObjectMaps.size(); i++){
				SSObject mapsSSO = ssObjectMaps.get(i).modifiedGet(ssCode);
				if (mapsSSO == null){
					ssObjectMaps.get(i).modifiedPut(sso);
					break;
				}else if (mapsSSO == sso){
					break;
				}else if (i == ssObjectMaps.size()-1){
					// need to add another map...
					ssObjectMaps.add(new WeakSSObjectMap<SSObject, Object>());
					continue;
				}
			}
		}
	}
	
}
