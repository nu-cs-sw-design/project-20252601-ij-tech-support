package example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate lint outcomes for a class.
 */
public class LintResult {

	private final List<Violation> violations = new ArrayList<>();

	public void addAll(List<Violation> items) {
		if (items != null) {
			violations.addAll(items);
		}
	}

	public List<Violation> getViolations() {
		return Collections.unmodifiableList(violations);
	}

	public boolean hasErrors() {
		return !violations.isEmpty();
	}
}
