/**
 * 
 */
package org.vafer.dependency.resources.buildtime;

import java.util.Iterator;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

final class BuildtimeResourceResolvingClassAdapter extends RemappingClassAdapter implements Opcodes {

	private final Remapper mapper;
	
	public BuildtimeResourceResolvingClassAdapter(ClassVisitor cv, Remapper pMapper) {
		super(cv, pMapper);
		mapper = pMapper;
	}

	protected MethodVisitor createRemappingMethodAdapter( int access, String newDesc, MethodVisitor mv ) {
		final MethodVisitor rmv = super.createRemappingMethodAdapter( access, newDesc, mv );

    // TODO make sure you aren't analyzing an abstract methods (no code)
		return new MethodNode(access, null, newDesc, null, null) {
			public void visitEnd() {
				final Analyzer an = new Analyzer(new SourceInterpreter());
				try {
					final Frame[] frames = an.analyze(className, this);
					// Elements of the frames array now contains info for each instruction
					// from the analyzed method.

					for (int i = 0; i < frames.length; i++) {
						//final Frame frame = frames[i];
						final AbstractInsnNode insn = instructions.get(i);

						if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
							final MethodInsnNode minsn = (MethodInsnNode) insn;

							if (minsn.name.equals( "getResource" ) && minsn.owner.equals( "java/lang/Class" )) {
								// now check where params come from (top of the stack)
								final SourceValue value = (SourceValue) frames[i].getStack(0);
								final Set sources = value.insns;  // instructions that produced this value
								for ( Iterator it = sources.iterator(); it.hasNext(); ) {
									final AbstractInsnNode source = (AbstractInsnNode) it.next();
									if (source.getOpcode() == Opcodes.LDC) {
										final LdcInsnNode constant = (LdcInsnNode) source;
										// can change constant.cst value here
										System.out.println("resource constant:" + constant.cst);
									} else {
										// can log something about value that came not from the constant
										System.out.println("resource source:" + source);
									}
								}
							}
						}
					}
					// got your mapping. can remap now. replaying recorded method
					accept(rmv);
				} catch (AnalyzerException ex) {
					ex.printStackTrace();
				}
			}
		};
	}		
}