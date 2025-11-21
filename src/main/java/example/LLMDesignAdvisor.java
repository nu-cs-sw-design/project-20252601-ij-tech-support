package example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Basic LLM advisor that posts the design summary to an API backed by an
 * environment-provided key. Uses OpenAI's chat completions endpoint.
 */
public class LLMDesignAdvisor {

	private static final String ENV_API_KEY = "MY_LLM_API_KEY";
	private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
	private static final String MODEL = "gpt-4o-mini";

	private final HttpClient httpClient = HttpClient.newHttpClient();

	public String analyzeDesign(String designSummary) {
		String apiKey = System.getenv(ENV_API_KEY);
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException(
					"Environment variable " + ENV_API_KEY + " is required for LLM calls");
		}

		String requestBody = buildRequestBody(designSummary);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(OPENAI_URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + apiKey)
				.POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
				.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				return extractContentFromResponse(response.body());
			}
			throw new IllegalStateException(
					"LLM call failed with status " + response.statusCode() + ": " + response.body());
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("LLM call failed", e);
		}
	}

	private String buildRequestBody(String designSummary) {
		String escaped = escapeJson(designSummary);
		// Minimal JSON payload; no streaming or chat history
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"model\":\"").append(MODEL).append("\",");
		sb.append("\"messages\":[");
		sb.append("{\"role\":\"system\",\"content\":\"You are a Java design reviewer.\"},");
		sb.append("{\"role\":\"user\",\"content\":\"Analyze this class design, suggest potential improvements, and flag obvious issues. Design summary:\\n")
				.append(escaped).append("\"}");
		sb.append("],");
		sb.append("\"temperature\":0.2");
		sb.append("}");
		return sb.toString();
	}

	private String escapeJson(String text) {
		return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}

	/**
	 * Very small JSON extractor to pull the assistant message content from the
	 * OpenAI response without additional dependencies.
	 */
	private String extractContentFromResponse(String responseBody) {
		String directContent = extractStringContent(responseBody);
		if (directContent != null) {
			return directContent.trim();
		}

		String arrayContent = extractArrayContent(responseBody);
		if (arrayContent != null) {
			return arrayContent.trim();
		}

		// Fallback: return raw JSON if we cannot parse the content.
		return responseBody;
	}

	private String extractStringContent(String body) {
		int start = findStringValueStart(body, "\"content\"");
		return start >= 0 ? readJsonString(body, start) : null;
	}

	private String extractArrayContent(String body) {
		// Handles "content":[{"type":"text","text":"..."}]
		int start = findStringValueStart(body, "\"text\"");
		return start >= 0 ? readJsonString(body, start) : null;
	}

	private int findStringValueStart(String body, String key) {
		int keyIndex = body.indexOf(key);
		if (keyIndex < 0) {
			return -1;
		}
		int colonIndex = body.indexOf(':', keyIndex + key.length());
		if (colonIndex < 0) {
			return -1;
		}
		for (int i = colonIndex + 1; i < body.length(); i++) {
			char c = body.charAt(i);
			if (Character.isWhitespace(c)) {
				continue;
			}
			return c == '"' ? i + 1 : -1;
		}
		return -1;
	}

	private String readJsonString(String body, int start) {
		StringBuilder sb = new StringBuilder();
		boolean escaped = false;
		for (int i = start; i < body.length(); i++) {
			char c = body.charAt(i);
			if (escaped) {
				switch (c) {
				case 'n':
					sb.append('\n');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 't':
					sb.append('\t');
					break;
				case '"':
				case '\\':
				case '/':
					sb.append(c);
					break;
				default:
					sb.append('\\').append(c);
				}
				escaped = false;
				continue;
			}
			if (c == '\\') {
				escaped = true;
				continue;
			}
			if (c == '"') {
				break;
			}
			sb.append(c);
		}
		return sb.toString();
	}
}
