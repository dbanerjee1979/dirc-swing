package dirc.ui;

import java.io.IOException;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import dirc.core.config.IrcNetwork;
import dirc.core.config.IrcServerInfo;
import dirc.core.net.IrcConnection;
import dirc.core.net.ThreadedSocketIrcConnection;
import dirc.core.server.IrcServer;

public class IrcConnectionTreeModel implements TreeModel {
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;

    public IrcConnectionTreeModel() {
        root = new DefaultMutableTreeNode();
        treeModel = new DefaultTreeModel(root);
    }
    
    public Object getRoot() {
        return treeModel.getRoot();
    }

    public Object getChild(Object parent, int index) {
        return treeModel.getChild(parent, index);
    }

    public int getChildCount(Object parent) {
        return treeModel.getChildCount(parent);
    }

    public boolean isLeaf(Object node) {
        return treeModel.isLeaf(node);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        treeModel.valueForPathChanged(path, newValue);
    }

    public int getIndexOfChild(Object parent, Object child) {
        return treeModel.getIndexOfChild(parent, child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        treeModel.addTreeModelListener(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        treeModel.removeTreeModelListener(l);
    }
    
    public TreeNode[] getPathToRoot(TreeNode node) {
        return treeModel.getPathToRoot(node);
    }

    public IrcServerNode connect(final IrcNetwork network) throws IOException {
        IrcServerInfo sc = network.getServer();
        final IrcConnection c = new ThreadedSocketIrcConnection(sc);
        IrcServer s = new IrcServer(c);
        
        final IrcServerNode serverNode = new IrcServerNode(s, sc.getHostname(), network.getNickname());
        treeModel.insertNodeInto(serverNode, root, root.getChildCount());

        s.connect();
        s.nickname(network.getNickname());
        s.username(network.getUsername(), network.getRealName());
        
        return serverNode;
    }
}