package eternity;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

/**
 * Main Eternity TTRPG Function
 */
public class EternityMain {
    public static void main(String[] args) {
        // Start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(EternityMain::startApp);
    }

    private static void startApp() {
        // Load Saved Characters (fallback to empty on failure)
        ArrayList<CharStore> store = CharacterDataManager.loadCharStore();
        if (store == null) {
            store = new ArrayList<>();
        }

        // Generate Character Sheet
        FrameSheet sheetFrame = new FrameSheet(store);

        // Show the welcome screen (user chooses New or Load)
        FrameFirst first = new FrameFirst(sheetFrame);
        first.setVisible(true);
    }
}
