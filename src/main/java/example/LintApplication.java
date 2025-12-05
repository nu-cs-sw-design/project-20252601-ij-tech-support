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

/**
 * Application layer: orchestrates lint, UML, and advice flows.
 */
public class LintApplication {

	private final RuleRegistry ruleRegistry;
	private final Reporter reporter;
	private final PlantUmlGenerator umlGenerator;
	private final LLMDesignAdvisor advisor;

	public LintApplication(RuleRegistry ruleRegistry, Reporter reporter, PlantUmlGenerator umlGenerator,
			LLMDesignAdvisor advisor) {
		this.ruleRegistry = ruleRegistry;
		this.reporter = reporter;
		this.umlGenerator = umlGenerator;
		this.advisor = advisor;
	}

	public void run(Command command) throws IOException {
		if (command == null || command.getMode() == null) {
			return;
		}

		switch (command.getMode()) {
		case UML:
			runUml(command.getTargets());
			break;
		case ADVICE:
			runAdvice(command.getTargets());
			break;
		case LINT:
		default:
			runLint(command.getTargets());
			break;
		}
	}

	private void runLint(List<String> targets) throws IOException {
		for (String className : targets) {
			ClassNode classNode = readClassNode(className);

			LintResult result = new LintResult();
			for (LintRule rule : ruleRegistry.rules()) {
				List<Violation> violations = rule.check(classNode);
				result.addAll(violations);
			}

			reporter.report(className, result);
		}
	}

	private void runUml(List<String> targets) throws IOException {
		for (int i = 0; i < targets.size(); i++) {
			String className = targets.get(i);
			ClassNode classNode = readClassNode(className);
			String uml = umlGenerator.generateClassDiagram(classNode);
			System.out.println(uml);
			if (i < targets.size() - 1) {
				System.out.println();
			}
		}
	}

	private void runAdvice(List<String> targets) throws IOException {
		for (int i = 0; i < targets.size(); i++) {
			String className = targets.get(i);
			ClassNode classNode = readClassNode(className);
			String summary = buildDesignSummary(classNode);
			String feedback = advisor.analyzeDesign(summary);
			System.out.println(feedback);
			if (i < targets.size() - 1) {
				System.out.println();
			}
		}
	}

	private ClassNode readClassNode(String className) throws IOException {
		ClassNode classNode = new ClassNode();
		ClassReader reader = new ClassReader(className);
		reader.accept(classNode, ClassReader.EXPAND_FRAMES);
		return classNode;
	}

	@SuppressWarnings("unchecked")
	private String buildDesignSummary(ClassNode classNode) {
		StringBuilder sb = new StringBuilder();

		String name = Type.getObjectType(classNode.name).getClassName();
		sb.append("Class: ").append(name).append('\n');

		String superName = classNode.superName != null
				? Type.getObjectType(classNode.superName).getClassName()
				: "java.lang.Object";
		sb.append("Superclass: ").append(superName).append('\n');

		List<String> interfaces = ((List<String>) classNode.interfaces).stream()
				.map(it -> Type.getObjectType(it).getClassName())
				.collect(Collectors.toList());
		sb.append("Interfaces: ").append(interfaces.isEmpty() ? "None" : String.join(", ", interfaces)).append('\n');

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
				.collect(Collectors.toCollection(ArrayList::new));
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

	private String formatArgTypes(Type[] argTypes) {
		if (argTypes.length == 0) {
			return "";
		}
		return java.util.Arrays.stream(argTypes)
				.map(Type::getClassName)
				.collect(Collectors.joining(", "));
	}
}
