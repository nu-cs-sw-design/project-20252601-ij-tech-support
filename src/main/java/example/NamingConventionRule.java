package example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Lint rule that enforces common Java naming conventions on classes, methods,
 * and fields using ASM tree nodes.
 */
public class NamingConventionRule implements LintRule {

	private static final Pattern PASCAL_CASE = Pattern.compile("[A-Z][A-Za-z0-9]*");
	private static final Pattern CAMEL_CASE = Pattern.compile("[a-z][A-Za-z0-9]*");
	private static final Pattern CONSTANT_CASE = Pattern.compile("[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*");

	@Override
	@SuppressWarnings("unchecked")
	public List<Violation> check(ClassNode node) {
		List<Violation> violations = new ArrayList<>();

		String classSimpleName = simpleClassName(node.name);
		if (!isPascalCase(classSimpleName)) {
			violations.add(new Violation(Violation.Target.CLASS, classSimpleName,
					"Class names should be PascalCase"));
		}

		for (FieldNode field : (List<FieldNode>) node.fields) {
			checkField(violations, classSimpleName, field);
		}

		for (MethodNode method : (List<MethodNode>) node.methods) {
			checkMethod(violations, classSimpleName, method);
		}

		return violations;
	}

	private void checkField(List<Violation> violations, String className, FieldNode field) {
		boolean isConstant = isConstant(field.access);
		boolean valid = isConstant ? isConstantCase(field.name) : isCamelCase(field.name);
		if (!valid) {
			String message = isConstant ? "Constant fields should be UPPER_SNAKE_CASE"
					: "Field names should be lowerCamelCase";
			violations.add(new Violation(Violation.Target.FIELD, className + "#" + field.name, message));
		}
	}

	private void checkMethod(List<Violation> violations, String className, MethodNode method) {
		if (method.name.startsWith("<") || method.name.startsWith("lambda$")) {
			return;
		}
		if (!isCamelCase(method.name)) {
			violations.add(new Violation(Violation.Target.METHOD, className + "#" + method.name,
					"Method names should be lowerCamelCase"));
		}
	}

	private boolean isConstant(int access) {
		return (access & Opcodes.ACC_STATIC) != 0 && (access & Opcodes.ACC_FINAL) != 0;
	}

	private String simpleClassName(String internalName) {
		if (internalName == null || internalName.isEmpty()) {
			return "";
		}
		int slash = internalName.lastIndexOf('/');
		int dollar = internalName.lastIndexOf('$');
		int lastSeparator = Math.max(slash, dollar);
		return lastSeparator >= 0 ? internalName.substring(lastSeparator + 1) : internalName;
	}

	private boolean isPascalCase(String name) {
		return PASCAL_CASE.matcher(name).matches();
	}

	private boolean isCamelCase(String name) {
		return CAMEL_CASE.matcher(name).matches();
	}

	private boolean isConstantCase(String name) {
		return CONSTANT_CASE.matcher(name).matches();
	}
}
