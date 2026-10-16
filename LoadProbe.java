import eternity.*;
public class LoadProbe {
  public static void main(String[] args) {
    int id = 2;
    StoreRuleManager rm = new StoreRuleManager();
    long t0 = System.currentTimeMillis();
    StoreCharData c = StoreCharManager.loadCharacter(id);
    System.out.println("raw load=" + (System.currentTimeMillis()-t0));
    if (c == null) {
      System.out.println("null character");
      return;
    }
    long t1 = System.currentTimeMillis();
    c.syncIdentityDerivedState(rm);
    System.out.println("syncIdentity=" + (System.currentTimeMillis()-t1));
    long t2 = System.currentTimeMillis();
    c.syncLevelBaseResources(rm);
    System.out.println("syncBaseResources=" + (System.currentTimeMillis()-t2));
    long t3 = System.currentTimeMillis();
    c.syncLevelCombatScalers(rm);
    System.out.println("syncCombatScalers=" + (System.currentTimeMillis()-t3));
    long t4 = System.currentTimeMillis();
    c.updateAll();
    System.out.println("updateAll=" + (System.currentTimeMillis()-t4));
    System.out.println("done level=" + c.getLevel() + " reminder=" + c.getPanelReminder());
  }
}
