package com.retailsystem.ai;

import org.springframework.stereotype.Component;

@Component
public class AiPromptBuilder {

    public static final String NOT_ENOUGH = "I don't have enough data to answer that.";

    public String askPrompt(String question, String facts) {
        return """
                You are RetailPro AI, an assistant for a multi-branch retail manager.

                RULES:
                - Answer using ONLY the FACTS block. Those numbers come from the live RetailPro database.
                - Never invent sales, inventory, prices, revenue, customers, branches, or suppliers.
                - Informal questions are allowed. Map them to FACTS:
                  lowest/least stock -> CURRENT_STOCK (sorted low to high) and LOWEST_STOCK
                  highest stock -> CURRENT_STOCK and HIGHEST_STOCK
                  reorder -> REORDER_SUGGESTIONS and LOW_VS_CATALOG_REORDER_LEVEL
                  run out / stockout -> STOCKOUT_RISK
                  top selling -> TOP_SELLERS_BY_REVENUE
                  slow / dead stock -> SLOW_OR_DEAD_STOCK
                  expiry -> NEAR_EXPIRY_OR_EXPIRED
                  best branch -> BRANCH_PERFORMANCE
                  today summary -> SALES_TODAY and ORG_SALES_TODAY
                  sales trend / next month revenue / growth -> SALES_TREND and PREDICTED_NEXT_MONTH_REVENUE_RS
                - Only reply with exactly this sentence when the needed FACTS list is missing or every row is (none):
                %s
                - Do not refuse just because the question is short, informal, or a follow-up.
                - Plain text only. No markdown, no asterisks.
                - Short bullets. Money in FACTS is rupees (Rs.).

                FACTS:
                %s

                QUESTION:
                %s
                """.formatted(NOT_ENOUGH, facts, question);
    }

    public String insightsNarrativePrompt(String computedFacts) {
        return """
                You are RetailPro AI. Write 2-4 short sentences summarizing the inventory situation.
                Use ONLY these backend-computed FACTS. Do not invent or change any number.
                If the list is empty, reply with exactly: %s
                Do not mention these rules.

                FACTS:
                %s
                """.formatted(NOT_ENOUGH, computedFacts);
    }
}
