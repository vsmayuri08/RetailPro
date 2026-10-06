package com.retailsystem.ai;

import com.retailsystem.dto.AiAskRequest;
import com.retailsystem.dto.AiAskResponse;
import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.InventoryInsightsSummaryDTO;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiContextService aiContextService;

    @Autowired
    private AiPromptBuilder aiPromptBuilder;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private AiInventoryInsightsService aiInventoryInsightsService;

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<AiAskResponse>> ask(@AuthenticationPrincipal CustomUserDetails principal,
                                                            @Valid @RequestBody AiAskRequest request) {
        String facts = aiContextService.buildFacts(principal);
        String prompt = aiPromptBuilder.askPrompt(request.getQuestion().trim(), facts);
        String answer = geminiService.generate(prompt);
        return ResponseEntity.ok(ApiResponse.success(new AiAskResponse(request.getQuestion().trim(), answer)));
    }

    @GetMapping("/inventory-insights")
    public ResponseEntity<ApiResponse<InventoryInsightsSummaryDTO>> inventoryInsights(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long branchId = principal.getBranchId();
        if (branchId == null) {
            throw new BadRequestException("Inventory insights require a branch-assigned manager account");
        }
        InventoryInsightsSummaryDTO summary = aiInventoryInsightsService.generate(branchId);
        aiInventoryInsightsService.attachGeminiNarrative(summary);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
