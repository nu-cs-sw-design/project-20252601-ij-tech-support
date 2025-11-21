package example;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Flags classes that cannot be publicly constructed.
 */
public class NonPublicConstructorRule implements LintRule {

	@Override
	public List<Violation> check(ClassNode node) {
		List<Violation> violations = new ArrayList<>();

		List<MethodNode> constructors = findConstructors(node);

		if (constructors.isEmpty() || !hasPublicConstructor(constructors)) {
			String className = Type.getObjectType(node.name).getClassName();
			violations.add(new Violation(Violation.Target.CLASS, className,
					"Class cannot be publicly constructed"));
		}

		return violations;
	}

	@SuppressWarnings("unchecked")
	private List<MethodNode> findConstructors(ClassNode node) {
		List<MethodNode> constructors = new ArrayList<>();
		for (MethodNode method : (List<MethodNode>) node.methods) {
			if ("<init>".equals(method.name)) {
				constructors.add(method);
			}
		}
		return constructors;
	}

	private boolean hasPublicConstructor(List<MethodNode> constructors) {
		for (MethodNode ctor : constructors) {
			if ((ctor.access & Opcodes.ACC_PUBLIC) != 0) {
				return true;
			}
		}
		return false;
	}
}
