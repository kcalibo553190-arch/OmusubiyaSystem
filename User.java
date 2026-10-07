public class User {
    public enum Role { ADMIN, CASHIER, INVENTORY_CLERK, STOCK_RECEIVING }

    private final String username;
    private final String password;
    private final Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean matches(String u, String p) { return username.equals(u) && password.equals(p); }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
}
