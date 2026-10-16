import eternity.*;
public class SpecProbe {
  public static void main(String[] args) {
    StoreRuleManager rm = new StoreRuleManager();
    StoreCharData c = StoreCharManager.loadCharacter(2);
    c.syncIdentityDerivedState(rm);
    c.syncLevelBaseResources(rm);
    c.syncLevelCombatScalers(rm);
    c.updateAll();
    System.out.println("CLASS SPECS");
    for (DataSpecialty s : c.getSpecials().getClassSpecialties()) {
      System.out.println(s.getId() + " | " + s.getName() + " | " + s.getType());
    }
  }
}
