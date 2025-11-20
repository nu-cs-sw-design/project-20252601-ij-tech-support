package example;

/**
 * Represents a lint finding for a class, method, or field.
 */
public class Violation {

	public enum Target {
		CLASS,
		FIELD,
		METHOD
	}

	private final Target target;
	private final String name;
	private final String message;

	public Violation(Target target, String name, String message) {
		this.target = target;
		this.name = name;
		this.message = message;
	}

	public Target getTarget() {
		return target;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return target + " '" + name + "': " + message;
	}
}
