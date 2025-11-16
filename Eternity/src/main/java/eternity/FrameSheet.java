package eternity;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFrame;

public class FrameSheet extends JFrame {
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 1000;
    private static final Path DATA_DIR = Paths.get("Data");

    private final DataQuery dataQuery;
    private final ArrayList<CharStore> charStore;

    private FrameNew newFrame;

    public FrameSheet(ArrayList<CharStore> charStore) {
        dataQuery = new DataQuery();
    	this.charStore = charStore;

        setupFrame();
    }

    private void setupFrame() {
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("No Character Loaded");
        setVisible(true);
    }

    // === UI EVENT HANDLERS ===

    public void onNewPressed() {
        if (newFrame == null) {
            newFrame = new FrameNew(this, dataQuery, charStore);
            newFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        newFrame.setVisible(true);
    }
}
