package model;

public class ReportSummary {
    private final double totalKg;
    private final double organicKg;
    private final double recyclableKg;
    private final double hazardousKg;
    private final int completedRecords;

    public ReportSummary(double totalKg, double organicKg, double recyclableKg,
                         double hazardousKg, int completedRecords) {
        this.totalKg = totalKg;
        this.organicKg = organicKg;
        this.recyclableKg = recyclableKg;
        this.hazardousKg = hazardousKg;
        this.completedRecords = completedRecords;
    }

    public double getTotalKg() { return totalKg; }
    public double getOrganicKg() { return organicKg; }
    public double getRecyclableKg() { return recyclableKg; }
    public double getHazardousKg() { return hazardousKg; }
    public int getCompletedRecords() { return completedRecords; }
}
