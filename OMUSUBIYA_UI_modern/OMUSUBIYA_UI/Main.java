import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UITheme.applyGlobalDefaults();
            new LoginFrame().setVisible(true);
        });
    }
}
