package theBugApp.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class LexicalContentProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractPlainText(String lexicalJson) {
        try {
            JsonNode root = objectMapper.readTree(lexicalJson);
            JsonNode rootNode = root.get("root");

            if (rootNode != null && rootNode.has("children")) {
                StringBuilder plainText = new StringBuilder();
                extractTextFromChildren(rootNode.get("children"), plainText);
                return plainText.toString().trim();
            }

            return "";
        } catch (Exception e) {
            // If it's not valid JSON or not Lexical format, treat as plain text
            return lexicalJson;
        }
    }

    private void extractTextFromChildren(JsonNode children, StringBuilder plainText) {
        if (children.isArray()) {
            for (JsonNode child : children) {
                if (child.has("text")) {
                    plainText.append(child.get("text").asText());
                }
                if (child.has("children")) {
                    extractTextFromChildren(child.get("children"), plainText);
                    // Add line break for paragraph nodes
                    if ("paragraph".equals(child.get("type").asText())) {
                        plainText.append("\n");
                    }
                }
            }
        }
    }

    public boolean isLexicalJson(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            return root.has("root") && root.get("root").has("children");
        } catch (Exception e) {
            return false;
        }
    }
}
