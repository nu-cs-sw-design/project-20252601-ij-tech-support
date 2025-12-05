# Project: Java Linter - Option 4

## Contributors
Ishaan Jalan, Viraj Agarwala

## Dependencies
- Java 17+ (JDK); build uses the bundled Gradle wrapper (Gradle 7.4)
- ASM 9.7 (asm, asm-tree, asm-analysis) for bytecode inspection
- Testing: JUnit 5.8.1, EasyMock 4.2

## Build & Test
- `./gradlew clean build` - compile and run the full test suite
- `./gradlew test` - run tests only
- Artifacts land in `build/libs/project.jar` (non-fat JAR; run via the wrapper for dependencies)

## Running the CLI
- Lint classes: `./gradlew run --args "example.TestBadClass example.GoodClass"`
- PlantUML for a class: `./gradlew run --args "--uml example.TestBadClass"`
- LLM design advice (requires API key; see below): `./gradlew run --args "--llm example.TestBadClass"`
- Pass fully qualified class names that are on the build output classpath (e.g., classes under `example`).

## LLM design advice & secret API key
The advisor posts a design summary to OpenAI’s Chat Completions API (`gpt-4o-mini`) using the environment variable `MY_LLM_API_KEY`.

How to obtain and use the key:
1. Sign in at https://platform.openai.com/api-keys and create a new secret key.
2. Store it locally (keep it out of version control):
   - macOS/Linux: `export MY_LLM_API_KEY="sk-..."` in your shell or profile.
   - Windows PowerShell: `setx MY_LLM_API_KEY "sk-..."` then restart the shell.
   - One-off run: `MY_LLM_API_KEY="sk-..." ./gradlew run --args "--llm example.TestBadClass"`.
3. Run the advisor with `--llm`; the CLI will fail fast if the env var is missing.
4. The call needs internet access; quota and billing are managed in your OpenAI account.
