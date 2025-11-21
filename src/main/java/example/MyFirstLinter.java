package example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class MyFirstLinter {

	public static void main(String[] args) throws IOException {
		if (args.length > 0 && "--llm".equals(args[0])) {
			runLlmMode(args);
			return;
		}
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

	private static void runLlmMode(String[] args) throws IOException {
		if (args.length <= 1) {
			System.out.println("No classes provided for --llm");
			return;
		}

		LLMDesignAdvisor advisor = new LLMDesignAdvisor();
		for (int i = 1; i < args.length; i++) {
			String className = args[i];
			ClassNode classNode = new ClassNode();
			ClassReader reader = new ClassReader(className);
			reader.accept(classNode, ClassReader.EXPAND_FRAMES);

			String summary = buildDesignSummary(classNode);
			String feedback = advisor.analyzeDesign(summary);
			System.out.println(feedback);

			if (i < args.length - 1) {
				System.out.println();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static String buildDesignSummary(ClassNode classNode) {
		StringBuilder sb = new StringBuilder();

		String className = Type.getObjectType(classNode.name).getClassName();
		sb.append("Class: ").append(className).append('\n');

		String superName = classNode.superName != null
				? Type.getObjectType(classNode.superName).getClassName()
				: "java.lang.Object";
		sb.append("Superclass: ").append(superName).append('\n');

		String interfaces = ((List<String>) classNode.interfaces).stream()
				.map(name -> Type.getObjectType(name).getClassName())
				.collect(Collectors.joining(", "));
		sb.append("Interfaces: ").append(interfaces.isEmpty() ? "None" : interfaces).append('\n');

		sb.append("Fields:\n");
		List<FieldNode> fields = (List<FieldNode>) classNode.fields;
		if (fields.isEmpty()) {
			sb.append("  - None\n");
		} else {
			for (FieldNode field : fields) {
				String fieldType = Type.getType(field.desc).getClassName();
				sb.append("  - ").append(field.name).append(" : ").append(fieldType).append('\n');
			}
		}

		sb.append("Methods:\n");
		List<MethodNode> methods = (List<MethodNode>) classNode.methods;
		List<MethodNode> userMethods = methods.stream()
				.filter(m -> !m.name.startsWith("<"))
				.collect(Collectors.toList());
		if (userMethods.isEmpty()) {
			sb.append("  - None\n");
		} else {
			for (MethodNode method : userMethods) {
				String returnType = Type.getReturnType(method.desc).getClassName();
				String argTypes = formatArgTypes(Type.getArgumentTypes(method.desc));
				sb.append("  - ").append(method.name).append("(").append(argTypes).append(")")
						.append(" : ").append(returnType).append('\n');
			}
		}

		return sb.toString();
	}

	private static String formatArgTypes(Type[] argTypes) {
		if (argTypes.length == 0) {
			return "";
		}
		return java.util.Arrays.stream(argTypes)
				.map(Type::getClassName)
				.collect(Collectors.joining(", "));
	}
}
