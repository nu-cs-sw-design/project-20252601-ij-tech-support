package example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Lint rule that flags interfaces a class implements without overriding any of
 * their abstract methods.
 */
public class RedundantInterfaceRule implements LintRule {

	@Override
	@SuppressWarnings("unchecked")
	public List<Violation> check(ClassNode node) {
		List<Violation> violations = new ArrayList<>();

		List<String> interfaces = (List<String>) node.interfaces;
		if (interfaces == null || interfaces.isEmpty()) {
			return violations;
		}
		List<MethodNode> classMethods = (List<MethodNode>) node.methods;

		for (String interfaceName : interfaces) {
			ClassNode interfaceNode = loadInterface(interfaceName);
			if (interfaceNode == null) {
				continue;
			}

			List<MethodNode> abstractMethods = collectAbstractMethods(interfaceNode);
			if (abstractMethods.isEmpty()) {
				continue;
			}

			if (!overridesAny(classMethods, abstractMethods)) {
				String simpleName = simpleClassName(node.name);
				String readableInterface = interfaceName.replace('/', '.');
				violations.add(new Violation(Violation.Target.CLASS, simpleName,
						"Interface " + readableInterface
								+ " is implemented but no interface methods are overridden."));
			}
		}

		return violations;
	}

	private ClassNode loadInterface(String interfaceName) {
		ClassNode interfaceNode = new ClassNode();
		try {
			ClassReader reader = new ClassReader(interfaceName);
			reader.accept(interfaceNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			return interfaceNode;
		} catch (IOException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private List<MethodNode> collectAbstractMethods(ClassNode interfaceNode) {
		List<MethodNode> abstractMethods = new ArrayList<>();
		for (MethodNode method : (List<MethodNode>) interfaceNode.methods) {
			if ((method.access & Opcodes.ACC_ABSTRACT) != 0) {
				abstractMethods.add(method);
			}
		}
		return abstractMethods;
	}

	private boolean overridesAny(List<MethodNode> classMethods, List<MethodNode> interfaceMethods) {
		for (MethodNode interfaceMethod : interfaceMethods) {
			for (MethodNode classMethod : classMethods) {
				if ((classMethod.access & Opcodes.ACC_STATIC) != 0) {
					continue;
				}
				if (interfaceMethod.name.equals(classMethod.name) && interfaceMethod.desc.equals(classMethod.desc)) {
					return true;
				}
			}
		}
		return false;
	}

	private String simpleClassName(String internalName) {
		int lastSlash = internalName.lastIndexOf('/');
		return lastSlash >= 0 ? internalName.substring(lastSlash + 1) : internalName;
	}
}
