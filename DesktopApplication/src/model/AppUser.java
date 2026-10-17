package model;

public class AppUser {
    private final String username;
    private final String displayName;
    private final String assignedArea;
    private final String role;

    public AppUser(String username, String displayName, String assignedArea, String role) {
        this.username = username;
        this.displayName = displayName;
        this.assignedArea = assignedArea;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getAssignedArea() { return assignedArea; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
