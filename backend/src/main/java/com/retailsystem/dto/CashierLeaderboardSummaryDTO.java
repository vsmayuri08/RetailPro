package com.retailsystem.dto;

import java.time.LocalDate;
import java.util.List;

public class CashierLeaderboardSummaryDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String topPerformerName;
    private List<CashierLeaderboardEntryDTO> entries;

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getTopPerformerName() { return topPerformerName; }
    public void setTopPerformerName(String topPerformerName) { this.topPerformerName = topPerformerName; }
    public List<CashierLeaderboardEntryDTO> getEntries() { return entries; }
    public void setEntries(List<CashierLeaderboardEntryDTO> entries) { this.entries = entries; }
}
