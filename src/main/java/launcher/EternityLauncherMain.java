package launcher;

import javax.swing.SwingUtilities;

public class EternityLauncherMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LauncherFrame frame = new LauncherFrame();
            frame.setVisible(true);
            frame.beginUpdateFlow();
        });
    }
}
