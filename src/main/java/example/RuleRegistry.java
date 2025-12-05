package example;

import java.util.List;

/**
 * Provides access to the set of lint rules to apply.
 */
public interface RuleRegistry {

	List<LintRule> rules();
}
