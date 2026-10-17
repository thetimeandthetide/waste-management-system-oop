package model;

public class ReportRow {
    private final String label;
    private final double organic;
    private final double recyclable;
    private final double hazardous;
    private final int records;
    private final double assignedKg;
    private final double bonusKg;

    public ReportRow(String label, double organic, double recyclable, double hazardous,
                     int records, double assignedKg, double bonusKg) {
        this.label = label;
        this.organic = organic;
        this.recyclable = recyclable;
        this.hazardous = hazardous;
        this.records = records;
        this.assignedKg = assignedKg;
        this.bonusKg = bonusKg;
    }

    public String getLabel() { return label; }
    public double getOrganic() { return organic; }
    public double getRecyclable() { return recyclable; }
    public double getHazardous() { return hazardous; }
    public int getRecords() { return records; }
    public double getAssignedKg() { return assignedKg; }
    public double getBonusKg() { return bonusKg; }
    public double getTotalKg() { return organic + recyclable + hazardous; }
}
