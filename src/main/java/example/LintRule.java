package example;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;

/**
 * Contract for lint rules that inspect ASM ClassNode structures.
 */
public interface LintRule {

	/**
	 * Inspect the given ClassNode and return any violations found.
	 */
	List<Violation> check(ClassNode node);
}
