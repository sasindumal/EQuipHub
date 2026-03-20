package com.equiphub.api.controller;

import com.equiphub.api.service.AiIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "*") // Update to specific Next.js/Mobile App origins in production
public class AiController {

    private final AiIntegrationService aiService;

    @Autowired
    public AiController(AiIntegrationService aiService) {
        this.aiService = aiService;
    }

    /**
     * @param studentId Query param
     * @param equipmentId The current equipment's ID
     * @return 200 OK with recommended item IDs
     */
    @GetMapping("/recommendations/{equipmentId}")
    public ResponseEntity<List<String>> getRecommendations(
            @RequestParam String studentId, 
            @PathVariable String equipmentId) {
        return ResponseEntity.ok(aiService.getEquipmentRecommendations(studentId, equipmentId));
    }

    /**
     * @param query The natural language search term (e.g. "for recording audio outside")
     * @return 200 OK with list of IDs matching the semantic search
     */
    @GetMapping("/search")
    public ResponseEntity<List<String>> semanticSearch(@RequestParam String query) {
        return ResponseEntity.ok(aiService.semanticSearch(query));
    }

    /**
     * @param request A simple JSON taking a "message" and "userId"
     * @return Virtual Assistant response based on RAG workflow
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithAssistant(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String userId = request.get("userId");
        String response = aiService.getChatbotResponse(message, userId);
        return ResponseEntity.ok(Map.of("reply", response));
    }

    /**
     * Called by Technical Officer to analyze an image of returned gear against the baseline.
     * @param request JSON with returnImageUrl and baselineImageUrl
     * @return Damage assessment score and specifics
     */
    @PostMapping("/assess-damage")
    public ResponseEntity<Map<String, Object>> assessReturnDamage(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(aiService.assessDamage(
                request.get("returnImageUrl"), 
                request.get("baselineImageUrl")));
    }

    /**
     * Called by a Lecturer to get a risk score to decide on a student request faster
     * @param request JSON with studentId and list of requestedItemIds
     * @return Risk factor, reasons, and a recommendation
     */
    @PostMapping("/evaluate-risk")
    public ResponseEntity<Map<String, Object>> evaluateRequestRisk(@RequestBody Map<String, Object> request) {
        String studentId = (String) request.get("studentId");
        List<String> requestedItemIds = (List<String>) request.get("requestedItemIds");
        return ResponseEntity.ok(aiService.evaluateRequestRisk(studentId, requestedItemIds));
    }
}
