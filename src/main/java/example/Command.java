package example;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Parsed CLI command.
 */
public class Command {

	private final Mode mode;
	private final List<String> targets;

	public Command(Mode mode, List<String> targets) {
		this.mode = mode;
		this.targets = targets == null ? Collections.emptyList() : targets;
	}

	public Mode getMode() {
		return mode;
	}

	public List<String> getTargets() {
		return targets;
	}

	public static Command of(Mode mode, String... targets) {
		return new Command(mode, targets == null ? Collections.emptyList() : Arrays.asList(targets));
	}
}
