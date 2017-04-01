package dirc.ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import dirc.core.event.IrcEvent;
import dirc.core.event.IrcEventListener;
import dirc.core.event.MotD;
import dirc.core.event.MotDEnd;
import dirc.core.event.MotDStart;
import dirc.core.event.QuitEvent;
import dirc.core.message.IrcMessage;
import dirc.core.message.TextStyle;
import dirc.core.message.TextStyle.Color;
import dirc.core.message.TextStyle.Style;
import dirc.core.net.IrcConnection;
import dirc.core.net.ThreadedSocketIrcConnection;
import dirc.core.server.IrcServer;

public class Test {
    public static class IrcTableModel extends AbstractTableModel implements IrcEventListener {
        private static final long serialVersionUID = 1L;

        private List<String> messages;
        
        private EnumMap<Color, String> colorCodes;

        private enum HtmlStyle {
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
        
        public IrcTableModel() {
            messages = new ArrayList<String>();
            
            colorCodes = new EnumMap<Color, String>(Color.class);
            colorCodes.put(Color.White, "#ffffff");
            colorCodes.put(Color.Black, "#000000");
            colorCodes.put(Color.Blue, "#0000aa");
            colorCodes.put(Color.Green, "#00aa00");
            colorCodes.put(Color.Red, "#aa0000");
            colorCodes.put(Color.Brown, "#aa5500");
            colorCodes.put(Color.Purple, "#aa00aa");
            colorCodes.put(Color.Orange, "#ff5555");
            colorCodes.put(Color.Yellow, "#ffff55");
            colorCodes.put(Color.LightGreen, "#55ff55");
            colorCodes.put(Color.Teal, "#00aaaa");
            colorCodes.put(Color.LightCyan, "#55ffff");
            colorCodes.put(Color.LightBlue, "#5555ff");
            colorCodes.put(Color.Pink, "#ff55ff");
            colorCodes.put(Color.Grey, "#555555");
            colorCodes.put(Color.LightGrey, "#aaaaaa");
        }
        
        public int getRowCount() {
            return messages.size();
        }

        public int getColumnCount() {
            return 1;
        }

        public Object getValueAt(int r, int c) {
            return messages.get(r);
        }

        public void receivedMessage(IrcMessage message) {
            int r = messages.size();
            
            if(!message.getTextStyles().isEmpty()) {
                String text = message.getParameters().get(message.getParameters().size() - 1);
                List<TextStyle> textStyles = message.getTextStyles();
                messages.add(convertToHtml(text, textStyles));
            }
            else {
                StringBuilder sb = new StringBuilder();
                List<String> ps = message.getParameters();
                for (String p : ps) {
                    sb.append(p).append(" ");
                }
                messages.add(sb.toString());
            }
            fireTableRowsInserted(r, r);
        }
        
        private String convertToHtml(String text, List<TextStyle> textStyles) {
            int start = 0;
            StringBuilder sb = new StringBuilder("<html>");
            for (TextStyle s : textStyles) {
                sb.append(text.substring(start, s.getStart()));
                int i = sb.length();
                sb.append(text.substring(s.getStart(), s.getEnd()));
                for (HtmlStyle hs : HtmlStyle.values()) {
                    hs.insert(sb, i, s);
                }
                start = s.getEnd();
            }
            sb.append(text.substring(start, text.length()));
            return sb.toString();
        }

        public void handleEvent(IrcEvent ev) {
            int r = messages.size();
            String text = ev.getMessage();
            if(ev instanceof MotD || ev instanceof MotDStart || ev instanceof MotDEnd) {
                text = convertToHtml(text, ev.getTextStyles());
            }
            messages.add(text);
            fireTableRowsInserted(r, r);
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        final IrcConnection c = new ThreadedSocketIrcConnection("irc.freenode.net", 6667, Charset.forName("UTF-8"));
        final IrcServer s = new IrcServer(c);
        final CountDownLatch shutdown = new CountDownLatch(1);
        s.addEventListener(new IrcEventListener() {
            public void handleEvent(IrcEvent ev) {
                if(ev instanceof QuitEvent && "sh0rug0ru".equals(((QuitEvent) ev).getNickname())) {
                    c.close();
                    shutdown.countDown();
                }
            }
        });
        final IrcTableModel tm = new IrcTableModel();
        s.addEventListener(tm);
        tm.receivedMessage(new IrcMessage(null, null, null, null, null, 
                Arrays.asList("Hello World"),
                Arrays.asList(
                    new TextStyle(0, 5).toggle(Style.Bold).setColors(Color.Blue, Color.LightCyan),
                    new TextStyle(6, 11).toggle(Style.Underlined).toggle(Style.Italic)
                )));
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame("dIRC");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                final JTable table = new JTable(tm);
                table.setShowHorizontalLines(false);
                f.add(new JScrollPane(table));
                tm.addTableModelListener(new TableModelListener() {
                    public void tableChanged(final TableModelEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                table.scrollRectToVisible(table.getCellRect(e.getFirstRow(), 0, true));
                            }
                        });
                    }
                });
                f.setSize(800, 600);
                f.setVisible(true);
            }
        });
        
        s.connect();
        s.nickname("sh0rug0ru");
        s.username("guest", "Duke");
        s.quit();
        shutdown.await();
        c.close();
    }
}
