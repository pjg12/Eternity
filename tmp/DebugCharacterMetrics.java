import eternity.StoreCharData;
import eternity.StoreCharManager;

public class DebugCharacterMetrics {
    public static void main(String[] args) {
        StoreCharData character = StoreCharManager.loadCharacter(12);
        if (character == null) {
            System.out.println("character=null");
            return;
        }
        System.out.println("loaded.maxHp=" + character.getResources().calcMaxHP());
        System.out.println("loaded.maxAura=" + character.getResources().calcMaxAura());
        System.out.println("loaded.currentAura=" + character.getResources().calcCurrentAura());
        character.updateAll();
        System.out.println("updated.maxHp=" + character.getResources().calcMaxHP());
        System.out.println("updated.maxAura=" + character.getResources().calcMaxAura());
        System.out.println("updated.currentAura=" + character.getResources().calcCurrentAura());
        System.out.println("updated.mainOcc=" + character.getResources().getMainOccupiedAura());
        System.out.println("updated.grantOcc=" + character.getResources().getGrantOccupiedAura());
    }
}
