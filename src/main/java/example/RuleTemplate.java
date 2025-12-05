package example;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;

/**
 * Simple template to centralize the check contract so concrete rules only
 * implement their violation collection logic.
 */
public abstract class RuleTemplate implements LintRule {

	@Override
	public final List<Violation> check(ClassNode node) {
		return collectViolations(node);
	}

	protected abstract List<Violation> collectViolations(ClassNode node);
}
