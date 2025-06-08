package theBugApp.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class LexicalContentProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isLexicalJson(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            return root.has("root") && root.get("root").has("children");
        } catch (Exception e) {
            return false;
        }
    }
}
