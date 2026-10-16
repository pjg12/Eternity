// CHECKED

package eternity;

import javax.swing.SwingUtilities;

/**
 * Main Class for Eternity
 */
public class EternityMain {
    // ---------------------------------------------------------
    // Main Eternity Method
    // ---------------------------------------------------------

    public static void main(String[] args) {
        // Start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(EternityMain::startApp);
    }

    private static void startApp() {
        FrameSheet sheetFrame = new FrameSheet();
        sheetFrame.setVisible(true);
        
        FrameFirst first = new FrameFirst(sheetFrame);
        first.setVisible(true);
    }
}
