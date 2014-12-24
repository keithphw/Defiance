/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.superserializable;

import java.io.*;
/**
 *
 * @author woodwardk
 */
public class TestSon extends TestDad{
	int two = 2;
	
	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
		//out.writeFields(this);
	}
	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
		//in.readFields(this);
	}
	
	public static class InnerClass extends SSAdapter{
		public InnerClass(){
		}
	}
	
	public static void main(String[] args){
		InnerClass inner = new InnerClass();
		try{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			SSObjectOutputStream ssout = new SSObjectOutputStream(bout);
			ssout.writeObject(inner);
			ssout.writeDone();
			SSObjectInputStream ssin = new SSObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
			InnerClass testInner = (InnerClass)ssin.readObject();
			ssin.readDone();
			System.out.println("inner == "+inner+", testInner == "+testInner);
		}catch(IOException e){
			e.printStackTrace();
		}
		
		TestSon son = new TestSon();
		try{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			SSObjectOutputStream ssout = new SSObjectOutputStream(bout);
			ssout.writeObject(son);
			ssout.writeDone();
			SSObjectInputStream ssin = new SSObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
			TestSon testSon = (TestSon)ssin.readObject();
			ssin.readDone();
			System.out.println("son == "+son+", son.two == "+son.two);
		}catch(IOException e){
			e.printStackTrace();
		}
	}


}
