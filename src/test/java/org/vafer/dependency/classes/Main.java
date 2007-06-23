package org.vafer.dependency.classes;

public final class Main implements Runnable {
	
	public void run () {
		
		final Reference ref = new Reference();

		{
			if(ref.getClass().getClassLoader().getResourceAsStream("org/vafer/dependency/classes/Reference.class") == null) {
				throw new RuntimeException("failed to load org/vafer/dependency/classes/Reference.class");
			}
		}
		
		{
			final String name = "org/vafer/dependency/classes/Reference.class";
			if(ref.getClass().getClassLoader().getResourceAsStream(name) == null) {
				throw new RuntimeException("failed to load " + name);
			}
		}

		{
			final Class clazz = this.getClass();
			final String clazzName = clazz.getName(); 
			final String resourceName = clazzName.replace('.', '/') + ".class";
			if(ref.getClass().getClassLoader().getResourceAsStream(resourceName) == null) {
				throw new RuntimeException("failed to load " + resourceName);
			}
		}

		{
			final Class clazz = Reference.class;
			final String clazzName = clazz.getName(); 
			final String resourceName = clazzName.replace('.', '/') + ".class";
			if(ref.getClass().getClassLoader().getResourceAsStream(resourceName) == null) {
				throw new RuntimeException("failed to load " + resourceName);
			}
		}
		
		System.out.println("Class " + ref.getClass().getName() + " is OK");
	}
}
