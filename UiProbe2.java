import eternity.*;
public class UiProbe2 {
  public static void main(String[] args) {
    System.setProperty("java.awt.headless", "true");
    StoreRuleManager rm = new StoreRuleManager();
    StoreCharData c = StoreCharManager.loadCharacter(2);
    c.syncIdentityDerivedState(rm);
    c.syncLevelBaseResources(rm);
    c.syncLevelCombatScalers(rm);
    c.updateAll();
    PanelChar panel = new PanelChar(rm, null);
    panel.updateCharacter(c);
    long t = System.currentTimeMillis();
    panel.setInteractive(true);
    System.out.println("setInteractive=" + (System.currentTimeMillis()-t));
    PanelImage img = new PanelImage(null, c);
    long t2 = System.currentTimeMillis();
    img.updateCharacter(c);
    System.out.println("image.updateCharacter=" + (System.currentTimeMillis()-t2));
  }
}
