package eternity;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores Color Information
 */
public class DataColor {
	@JsonProperty private String title;
	@JsonProperty private int backRed;
	@JsonProperty private int backGreen;
	@JsonProperty private int backBlue;
	@JsonProperty private int foreRed;
	@JsonProperty private int foreGreen;
	@JsonProperty private int foreBlue;

    // --- Constructors ---
    
    public DataColor() {}
    public DataColor(DataColor src) { this(src.title, src.backRed, src.backGreen, src.backBlue, src.foreRed, src.foreGreen, src.foreBlue); }

    public DataColor(String title, int backRed, int backGreen, int backBlue, int foreRed, int foreGreen, int foreBlue) {
        this.title = title;
        this.backRed = backRed;
        this.backGreen = backGreen;
        this.backBlue = backBlue;
        this.foreRed = foreRed;
        this.foreGreen = foreGreen;
        this.foreBlue = foreBlue;
    }

    // Convenience: 
    // return HEX codes
    @JsonIgnore public String getBackHex() { return String.format("#%02X%02X%02X", backRed, backGreen, backBlue); }
    @JsonIgnore public String getForeHex() { return String.format("#%02X%02X%02X", foreRed, foreGreen, foreBlue); }
    
    // return Color object    
    @JsonIgnore public Color getForeColor() { return new Color(getForeRed(), getForeGreen(), getForeBlue()); }
    @JsonIgnore public Color getBackColor() { return new Color(getBackRed(), getBackGreen(), getBackBlue()); }

    // --- Getters / Setters ---
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getBackRed() { return backRed; }
    public void setBackRed(int backRed) { this.backRed = backRed; }

    public int getBackGreen() { return backGreen; }
    public void setBackGreen(int backGreen) { this.backGreen = backGreen; }

    public int getBackBlue() { return backBlue; }
    public void setBackBlue(int backBlue) { this.backBlue = backBlue; }

    public int getForeRed() { return foreRed; }
    public void setForeRed(int foreRed) { this.foreRed = foreRed; }

    public int getForeGreen() { return foreGreen; }
    public void setForeGreen(int foreGreen) { this.foreGreen = foreGreen; }

    public int getForeBlue() { return foreBlue; }
    public void setForeBlue(int foreBlue) { this.foreBlue = foreBlue; }
    
    // --- Helpers ---
    
    @Override
    public String toString() {
        return "DataStatus{" + "backRed='" + backRed + '\'' + ", backGreen='" + backGreen + '\'' + ", backBlue='" + backBlue + '\'' +
            ", foreRed='" + foreRed + '\'' + ", foreGreen=" + foreGreen + '\'' + ", foreBlue='" + foreBlue + '\'' + '}';
    }
}