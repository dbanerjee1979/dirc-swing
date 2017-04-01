package dirc.core.message;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public class TextStyle {
    public enum Style {
        Bold, Italic, Underlined, Color
    }

    public enum Color {
        White, Black, Blue, Green, Red, Brown, Purple, Orange, Yellow, 
        LightGreen, Teal, LightCyan, LightBlue, Pink, Grey, LightGrey
    }

    private int start;
    private int end;
    private EnumMap<Style, Object> styles;
    
    public TextStyle(int start, int end) {
        this.start = start;
        this.end = end;
        this.styles = new EnumMap<Style, Object>(Style.class);
    }
    
    public TextStyle(TextStyle style) {
        this.start = style.start;
        this.end = style.end;
        this.styles = new EnumMap<Style, Object>(style.styles);
    }

    public TextStyle(int start, int end, TextStyle style) {
        this(style);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("textstyle: ");
        sb.append("[ start: ").append(start);
        sb.append(", end: ").append(end);
        sb.append(", styles: ").append(styles);
        sb.append("]");
        return sb .toString();
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
    
    public boolean is(Style style) {
        return styles.containsKey(style);
    }

    public boolean isPlain() {
        return styles.isEmpty();
    }

    public Color getForeground() {
        return getColor().get(0);
    }

    public Color getBackground() {
        return getColor().get(1);
    }

    @SuppressWarnings("unchecked")
    private List<Color> getColor() {
        List<Color> colors = (List<Color>) styles.get(Style.Color);
        return colors != null ? colors : Arrays.<Color> asList(null, null);
    }
    
    public void clear() {
        styles.clear();
    }

    public TextStyle toggle(Style style) {
        if(!styles.containsKey(style)) {
            styles.put(style, Boolean.TRUE);
        }
        else {
            styles.remove(style);
        }
        return this;
    }

    public TextStyle setColors(Color foreground, Color background) {
        styles.put(Style.Color, Arrays.asList(foreground, background));
        return this;
    }

    public TextStyle trimRange(int end) {
        this.end = end;
        return this;
    }

    public int length() {
        return end - start;
    }

    public void reverseColors() {
        Color foreground = getForeground();
        Color background = getBackground();
        setColors(background != null ? background : Color.White, foreground != null ? foreground : Color.Black);
    }
}

