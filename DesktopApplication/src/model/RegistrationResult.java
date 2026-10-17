package model;

public class RegistrationResult {
    private final AppUser user;
    private final String temporaryPassword;

    public RegistrationResult(AppUser user, String temporaryPassword) {
        this.user = user;
        this.temporaryPassword = temporaryPassword;
    }

    public AppUser getUser() { return user; }
    public String getTemporaryPassword() { return temporaryPassword; }
}
