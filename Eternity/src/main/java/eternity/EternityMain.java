// CHECKED

package eternity;

import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Main Eternity TTRPG Function
 */
public class EternityMain {
    public static void main(String[] args) {
        // Start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(EternityMain::startApp);
    }

    private static void startApp() {
        FrameFirst first = new FrameFirst();
        first.setVisible(true);

        new SwingWorker<ArrayList<StoreMetaChar>, Void>() {
            @Override
            protected ArrayList<StoreMetaChar> doInBackground() {
                ArrayList<StoreMetaChar> store = StoreMetaManager.loadCharStore();
                if (store == null) {
                    store = new ArrayList<>();
                }
                return store;
            }

            @Override
            protected void done() {
                try {
                    ArrayList<StoreMetaChar> store = get();
                    first.attachStartupStore(store);
                } catch (Exception e) {
                    first.showLoadError("Failed to initialize application: " + e.getMessage());
                }
            }
        }.execute();
    }

}
