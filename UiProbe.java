import eternity.*;
public class UiProbe {
  public static void main(String[] args) {
    System.setProperty("java.awt.headless", "true");
    StoreRuleManager rm = new StoreRuleManager();
    StoreCharData c = StoreCharManager.loadCharacter(2);
    c.syncIdentityDerivedState(rm);
    c.syncLevelBaseResources(rm);
    c.syncLevelCombatScalers(rm);
    c.updateAll();
    long t0 = System.currentTimeMillis();
    PanelChar panel = new PanelChar(rm, null);
    System.out.println("construct PanelChar=" + (System.currentTimeMillis()-t0));
    long t1 = System.currentTimeMillis();
    panel.updateCharacter(c);
    System.out.println("panel.updateCharacter=" + (System.currentTimeMillis()-t1));
  }
}
