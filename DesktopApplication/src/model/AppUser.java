package model;

public class AppUser {
    private final String username;
    private final String displayName;
    private final String assignedArea;
    private final String role;
    private final String dateOfBirth;
    private final String registerNumber;
    private final String department;

    public AppUser(String username, String displayName, String assignedArea, String role) {
        this(username, displayName, assignedArea, role, null, null, null);
    }

    public AppUser(String username, String displayName, String assignedArea, String role,
                   String dateOfBirth, String registerNumber, String department) {
        this.username = username;
        this.displayName = displayName;
        this.assignedArea = assignedArea;
        this.role = role;
        this.dateOfBirth = dateOfBirth;
        this.registerNumber = registerNumber;
        this.department = department;
    }

    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getAssignedArea() { return assignedArea; }
    public String getRole() { return role; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getRegisterNumber() { return registerNumber; }
    public String getDepartment() { return department; }
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
