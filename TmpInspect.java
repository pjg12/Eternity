import java.nio.file.*;
import com.fasterxml.jackson.databind.*;
import eternity.*;
public class TmpInspect {
  public static void main(String[] args) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    StoreCharData c = mapper.readValue(Files.readString(Path.of("Characters/8.json")), StoreCharData.class);
    System.out.println("before domains=" + c.getTraining().getDomains());
    System.out.println("before domain affinities=" + c.getTraining().getDomainAffinities());
    System.out.println("before shareable=" + c.hasShareableDomainStatusEffects());
    c.updateAll();
    System.out.println("after domains=" + c.getTraining().getDomains());
    System.out.println("after domain affinities=" + c.getTraining().getDomainAffinities());
    System.out.println("after shareable=" + c.hasShareableDomainStatusEffects());
    System.out.println("macro=" + c.buildDomainStatusMacro());
    for (DataStatus s : c.getTraining().getDomainStatusEffects()) {
      System.out.println(s.getName()+"|"+s.getAttribute()+"|"+s.getSeverity());
    }
  }
}
