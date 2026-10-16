package eternity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataList {
    @JsonProperty private String list;
    @JsonProperty private String name;
    @JsonProperty private String description;

    public DataList() {
        this.list = "";
        this.name = "";
        this.description = "";
    }

    public DataList(String list, String name, String description) {
        this.list = safe(list);
        this.name = safe(name);
        this.description = safe(description);
    }

    public String getList() { return list; }
    public void setList(String list) { this.list = safe(list); }

    public String getName() { return name; }
    public void setName(String name) { this.name = safe(name); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = safe(description); }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
