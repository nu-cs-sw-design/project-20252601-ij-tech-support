package example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class MyFirstLinter {

	public static void main(String[] args) throws IOException {
		List<LintRule> lintRules = new ArrayList<>();
		lintRules.add(new NamingConventionRule());
		lintRules.add(new EqualsHashCodeRule());

		for (String className : args) {
			ClassNode classNode = new ClassNode();
			ClassReader reader = new ClassReader(className);
			reader.accept(classNode, ClassReader.EXPAND_FRAMES);

			List<Violation> violations = new ArrayList<>();
			for (LintRule rule : lintRules) {
				List<Violation> ruleViolations = rule.check(classNode);
				if (ruleViolations != null) {
					violations.addAll(ruleViolations);
				}
			}

			if (violations.isEmpty()) {
				System.out.println("No violations for " + className);
			} else {
				for (Violation violation : violations) {
					System.out.println(violation);
				}
			}
		}
	}
}
