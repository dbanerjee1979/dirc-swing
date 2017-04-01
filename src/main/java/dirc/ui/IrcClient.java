package dirc.ui;

import java.awt.CardLayout;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import dirc.core.event.NoticeEvent;
import dirc.core.message.TextStyle;
import dirc.core.message.TextStyle.Color;
import dirc.core.message.TextStyle.Style;

public class IrcClient extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel container;
    private CardLayout containerLayout;
    private IrcConnectionTreeModel treeModel;
    private JTree tree;
    private JSplitPane splitter;

    public IrcClient(IrcConnectionTreeModel treeModel) {
        super("dIRC");
        this.treeModel = treeModel;

        splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setDividerLocation(150);
        add(splitter);

        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        splitter.add(new JScrollPane(tree));

        containerLayout = new CardLayout();
        container = new JPanel(containerLayout);
        splitter.add(container, JSplitPane.RIGHT);
        
        treeModel.addTreeModelListener(new TreeModelListener() {
            public void treeStructureChanged(TreeModelEvent ev) {
            }

            public void treeNodesRemoved(TreeModelEvent ev) {
            }

            public void treeNodesInserted(final TreeModelEvent ev) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        for (Object c : ev.getChildren()) {
                            if (c instanceof IrcServerNode) {
                                addServer((IrcServerNode) c);
                            }
                        }
                    }
                });
            }

            public void treeNodesChanged(TreeModelEvent ev) {
            }
        });

        setSize(800, 600);
    }

    private void addServer(final IrcServerNode s) {
        s.getServerConsoleModel().handleEvent(new NoticeEvent("sh0rug0ru", "Hello World",
                        Arrays.asList(new TextStyle(0, 5).toggle(Style.Bold).setColors(Color.Blue, Color.LightCyan),
                                new TextStyle(6, 11).toggle(Style.Underlined).toggle(Style.Italic))));
        tree.expandPath(new TreePath(treeModel.getPathToRoot(s.getParent())));
        final JTable table = new JTable(s.getServerConsoleModel());
        table.setTableHeader(null);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(true);
        s.getServerConsoleModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(final TableModelEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        table.scrollRectToVisible(table.getCellRect(e.getFirstRow(), 0, true));
                    }
                });
            }
        });

        table.createDefaultColumnsFromModel();
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(50);
        column.setMaxWidth(50);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.TRAILING);
        column.setCellRenderer(renderer);

        container.add(new JScrollPane(table), s.getHostname());
        containerLayout.last(container);
    }
}
