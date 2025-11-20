package example;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Utility for generating PlantUML class diagrams from ASM ClassNode data.
 */
public final class PlantUmlGenerator {

	private PlantUmlGenerator() {
	}

	@SuppressWarnings("unchecked")
	public static String generateClassDiagram(ClassNode node) {
		String simpleName = simpleClassName(node.name);
		StringBuilder sb = new StringBuilder();
		sb.append("@startuml\n");
		sb.append("class ").append(simpleName).append(" {\n");

		for (FieldNode field : (List<FieldNode>) node.fields) {
			String type = Type.getType(field.desc).getClassName();
			sb.append("  ")
					.append(visibilitySymbol(field.access))
					.append(field.name)
					.append(" : ")
					.append(type)
					.append("\n");
		}

		for (MethodNode method : (List<MethodNode>) node.methods) {
			if (method.name.startsWith("<")) {
				continue;
			}
			String args = formatArguments(Type.getArgumentTypes(method.desc));
			String returnType = Type.getReturnType(method.desc).getClassName();
			sb.append("  ")
					.append(visibilitySymbol(method.access))
					.append(method.name)
					.append("(")
					.append(args)
					.append(") : ")
					.append(returnType)
					.append("\n");
		}

		sb.append("}\n");

		if (node.superName != null && !"java/lang/Object".equals(node.superName)) {
			sb.append(simpleName)
					.append(" --|> ")
					.append(simpleClassName(node.superName))
					.append("\n");
		}

		for (String iface : (List<String>) node.interfaces) {
			sb.append(simpleName)
					.append(" ..|> ")
					.append(simpleClassName(iface))
					.append("\n");
		}

		sb.append("@enduml");
		return sb.toString();
	}

	private static String visibilitySymbol(int access) {
		if ((access & Opcodes.ACC_PUBLIC) != 0) {
			return "+";
		}
		if ((access & Opcodes.ACC_PRIVATE) != 0) {
			return "-";
		}
		if ((access & Opcodes.ACC_PROTECTED) != 0) {
			return "#";
		}
		return "~";
	}

	private static String formatArguments(Type[] argTypes) {
		List<String> args = new ArrayList<>();
		for (int i = 0; i < argTypes.length; i++) {
			Type type = argTypes[i];
			args.add("arg" + i + " : " + type.getClassName());
		}
		return String.join(", ", args);
	}

	private static String simpleClassName(String internalName) {
		int lastSlash = internalName.lastIndexOf('/');
		return lastSlash >= 0 ? internalName.substring(lastSlash + 1) : internalName;
	}
}
