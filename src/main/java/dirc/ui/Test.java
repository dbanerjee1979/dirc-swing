package dirc.ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import dirc.core.message.IrcMessage;
import dirc.core.message.TextStyle;
import dirc.core.message.TextStyle.Style;
import dirc.core.net.IrcConnection;
import dirc.core.net.ThreadedSocketIrcConnection;
import dirc.core.server.IrcServer;
import dirc.ui.event.IrcEvent;
import dirc.ui.event.IrcEventListener;
import dirc.ui.event.QuitEvent;

public class Test {
    public static class IrcTableModel extends AbstractTableModel implements IrcEventListener {
        private static final long serialVersionUID = 1L;

        private List<String> messages;
        
        public IrcTableModel() {
            messages = new ArrayList<String>();
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
                int start = 0;
                StringBuilder sb = new StringBuilder("<html>");
                for (TextStyle s : message.getTextStyles()) {
                    sb.append(text.substring(start, s.getStart()));
                    if(s.is(Style.Bold)) {
                        sb.append("<b>");
                    }
                    else if(s.is(Style.Underlined)) {
                        sb.append("<u>");
                    }
                    sb.append(text.substring(s.getStart(), s.getEnd()));
                    if(s.is(Style.Bold)) {
                        sb.append("</b>");
                    }
                    else if(s.is(Style.Underlined)) {
                        sb.append("</u>");
                    }
                    start = s.getEnd();
                }
                sb.append(text.substring(start, text.length()));
                messages.add(sb.toString());
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

        public void handleEvent(IrcEvent ev) {
            int r = messages.size();
            messages.add(ev.getMessage());
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
                    new TextStyle(0, 5).toggle(TextStyle.Style.Bold),
                    new TextStyle(6, 11).toggle(TextStyle.Style.Underlined)
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
