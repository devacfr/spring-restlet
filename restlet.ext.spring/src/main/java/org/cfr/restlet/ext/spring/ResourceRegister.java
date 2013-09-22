package org.cfr.restlet.ext.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.pmi.restlet.mapping.ClassDescriptor;

public class ResourceRegister {

	private Set<ClassDescriptor> classDescriptors = new HashSet<ClassDescriptor>();
	

	public ClassDescriptor addClassDescriptor(ClassDescriptor classDescriptor) {
		classDescriptors.add(classDescriptor);
		return classDescriptor;
	}
	
	public ClassDescriptor addClass(Class<?> clazz) {
		return addClass(clazz, null);
	}
	
	public ClassDescriptor addClass(Class<?> clazz, String name) {
		ClassDescriptor cl = new ClassDescriptor(clazz, name);
		classDescriptors.add(cl);
		return cl;
	}
	
	
	
	public boolean isEmpty() {
		return classDescriptors.isEmpty();
	}
	
	public void clear() {
		classDescriptors.clear();
	}

	public Collection<ClassDescriptor> getClassDescriptors() {
		return Collections.unmodifiableCollection(classDescriptors);
	}
	
	public Class<?>[] getTargetClasses() {
		// TODO to optimize
		ArrayList<Class<?>> list = new ArrayList<Class<?>>();
		for (ClassDescriptor cl : classDescriptors) {
			list.add(cl.getTargetClass());
		}
		return list.toArray(new Class<?>[list.size()]);
	}

}
