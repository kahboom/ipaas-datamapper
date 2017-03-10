package com.mediadriver.atlas.java.v2;

public class AtlasJavaModelFactory {

	public static JavaClass createJavaClass() {
		JavaClass javaClass = new JavaClass();
		javaClass.setJavaEnumFields(new JavaEnumFields());
		javaClass.setJavaFields(new JavaFields());
		return javaClass;
	}
	
}
