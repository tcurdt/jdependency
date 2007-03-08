package org.vafer.dependency.asm;

public interface ClassTransformer {
	
	byte[] transform( final byte[] clazz );

}
