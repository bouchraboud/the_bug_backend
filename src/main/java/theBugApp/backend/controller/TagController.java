package theBugApp.backend.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.dto.SimpleTagDTO;
import theBugApp.backend.dto.FullTagDTO;
import theBugApp.backend.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<FullTagDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FullTagDTO>> getPopularTags() {
        return ResponseEntity.ok(tagService.getPopularTags());
    }

    @GetMapping("/{tagName}/questions")
    public ResponseEntity<?> getQuestionsByTag(@PathVariable String tagName) {
        return ResponseEntity.ok(tagService.getQuestionsByTagName(tagName));
    }
    @GetMapping("/{tagName}")
    public ResponseEntity<FullTagDTO> getTagByName(@PathVariable String tagName) {
        return ResponseEntity.ok(tagService.getTagByName(tagName));
    }

}