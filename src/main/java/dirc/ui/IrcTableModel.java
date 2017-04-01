package dirc.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import dirc.core.event.IrcEvent;
import dirc.core.event.IrcEventListener;
import dirc.core.event.MotD;
import dirc.core.event.MotDEnd;
import dirc.core.event.MotDStart;
import dirc.core.event.NoticeEvent;
import dirc.core.message.TextStyle;

public class IrcTableModel extends AbstractTableModel implements IrcEventListener {
    private static final long serialVersionUID = 1L;

    private String nickname;
    private List<String> recipients;
    private List<String> messages;
    
    public IrcTableModel(String nickname) {
        this.nickname = nickname;
        recipients = new ArrayList<String>();
        messages = new ArrayList<String>();
    }
    
    public int getRowCount() {
        return messages.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int r, int c) {
        switch(c) {
            case 0: return recipients.get(r);
            case 1: return messages.get(r);
            default: return null;
        }
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

    public void handleEvent(final IrcEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int r = messages.size();
                String text = ev.getMessage();
                if(ev instanceof MotD || ev instanceof MotDStart || ev instanceof MotDEnd || ev instanceof NoticeEvent) {
                    text = convertToHtml(text, ev.getTextStyles());
                }
                recipients.add(nickname.equals(ev.getRecipient()) ? "-" : ev.getRecipient());
                messages.add(text);
                fireTableRowsInserted(r, r);
            }
        });
    }
}