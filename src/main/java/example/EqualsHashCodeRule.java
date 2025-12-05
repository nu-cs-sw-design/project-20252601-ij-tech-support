package example;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Lint rule that ensures classes implement equals and hashCode consistently.
 */
public class EqualsHashCodeRule extends RuleTemplate {

	@Override
	@SuppressWarnings("unchecked")
	protected List<Violation> collectViolations(ClassNode node) {
		List<Violation> violations = new ArrayList<>();
		boolean definesEquals = false;
		boolean definesHashCode = false;

		for (MethodNode method : (List<MethodNode>) node.methods) {
			if ((method.access & Opcodes.ACC_STATIC) != 0) {
				continue;
			}
			if ("equals".equals(method.name) && "(Ljava/lang/Object;)Z".equals(method.desc)) {
				definesEquals = true;
			} else if ("hashCode".equals(method.name) && "()I".equals(method.desc)) {
				definesHashCode = true;
			}
		}

		String simpleName = simpleClassName(node.name);
		if (definesEquals && !definesHashCode) {
			violations.add(new Violation(Violation.Target.CLASS, simpleName,
					"Class " + simpleName + " defines equals(Object) but not hashCode()."));
		} else if (definesHashCode && !definesEquals) {
			violations.add(new Violation(Violation.Target.CLASS, simpleName,
					"Class " + simpleName + " defines hashCode() but not equals(Object)."));
		}

		return violations;
	}

	private String simpleClassName(String internalName) {
		int lastSlash = internalName.lastIndexOf('/');
		return lastSlash >= 0 ? internalName.substring(lastSlash + 1) : internalName;
	}
}
