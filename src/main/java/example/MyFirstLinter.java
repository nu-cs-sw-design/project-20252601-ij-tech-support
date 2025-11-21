package example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class MyFirstLinter {

	public static void main(String[] args) throws IOException {
		if (args.length > 0 && "--uml".equals(args[0])) {
			runUmlMode(args);
			return;
		}

		List<LintRule> lintRules = new ArrayList<>();
		lintRules.add(new NamingConventionRule());
		lintRules.add(new EqualsHashCodeRule());
		lintRules.add(new RedundantInterfaceRule());
		lintRules.add(new NonPublicConstructorRule());

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

	private static void runUmlMode(String[] args) throws IOException {
		for (int i = 1; i < args.length; i++) {
			String className = args[i];
			ClassNode classNode = new ClassNode();
			ClassReader reader = new ClassReader(className);
			reader.accept(classNode, ClassReader.EXPAND_FRAMES);

			String plantUml = PlantUmlGenerator.generateClassDiagram(classNode);
			System.out.println(plantUml);

			if (i < args.length - 1) {
				System.out.println();
			}
		}
	}
}
