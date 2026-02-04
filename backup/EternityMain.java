package eternity;

import java.util.ArrayList;

/**
 * Main Eternity TTRPG Function
 */
public class EternityMain {
    public static void main(String[] args) {
    	// Load Saved Characters
        ArrayList<CharStore> store = CharacterDataManager.loadCharStore();
        
        // Generate Character Sheet
        FrameSheet sheetFrame = new FrameSheet(store);
        
        // Get Last Modified Character
        CharStore last = CharacterDataManager.getLastLoaded(store);
        //CharStore last = null;

        if (last != null) {
            // Auto-load the most recently used character
            System.out.println("Auto-loading last character: " + last.getName());
            sheetFrame.loadConfirmed(last.getIndex());
        } else {
            // Show first-choice frame
            FrameFirst first = new FrameFirst(sheetFrame);
            first.setVisible(true);
        }
    }
}