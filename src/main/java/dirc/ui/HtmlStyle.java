package dirc.ui;

import java.util.EnumMap;

import dirc.core.message.TextStyle;
import dirc.core.message.TextStyle.Color;
import dirc.core.message.TextStyle.Style;

public enum HtmlStyle {
    Bold(Style.Bold, "b"), 
    Italic(Style.Italic, "i"), 
    Underline(Style.Underlined, "u"),
    Color(Style.Color, "font") {
        private EnumMap<Color, String> colorCodes;
        {
            colorCodes = new EnumMap<Color, String>(Color.class);
            colorCodes.put(TextStyle.Color.White, "#ffffff");
            colorCodes.put(TextStyle.Color.Black, "#000000");
            colorCodes.put(TextStyle.Color.Blue, "#0000aa");
            colorCodes.put(TextStyle.Color.Green, "#00aa00");
            colorCodes.put(TextStyle.Color.Red, "#aa0000");
            colorCodes.put(TextStyle.Color.Brown, "#aa5500");
            colorCodes.put(TextStyle.Color.Purple, "#aa00aa");
            colorCodes.put(TextStyle.Color.Orange, "#ff5555");
            colorCodes.put(TextStyle.Color.Yellow, "#ffff55");
            colorCodes.put(TextStyle.Color.LightGreen, "#55ff55");
            colorCodes.put(TextStyle.Color.Teal, "#00aaaa");
            colorCodes.put(TextStyle.Color.LightCyan, "#55ffff");
            colorCodes.put(TextStyle.Color.LightBlue, "#5555ff");
            colorCodes.put(TextStyle.Color.Pink, "#ff55ff");
            colorCodes.put(TextStyle.Color.Grey, "#555555");
            colorCodes.put(TextStyle.Color.LightGrey, "#aaaaaa");
        }
        
        @Override
        public String attrs(TextStyle ts) {
            String foreground = colorCodes.get(ts.getForeground());
            String background = colorCodes.get(ts.getBackground());
            return " style='" + 
                   (foreground != null ? "color: " + foreground + ";" : "") +
                   (background != null ? "background: " + background + ";" : "") +
                   "' ";
        }
    };
    
    private Style s;
    private String tag;
    
    HtmlStyle(Style s, String tag) {
        this.s = s;
        this.tag = tag;
    }
    
    public String attrs(TextStyle ts) {
        return "";
    }
    
    public void insert(StringBuilder sb, int i, TextStyle ts) {
        if(ts.is(s)) {
            sb.insert(i, "<" + tag + attrs(ts) + ">");
            sb.append("</" + tag + ">");
        }
    }
}