/*
 * Copyright (C) 2007, 2008, 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import static org.lateralgm.main.Util.deRef;

import java.awt.Desktop;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Position;
import javax.swing.tree.TreePath;

import org.lateralgm.components.AboutBox;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.ResourceList;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;

public class Listener extends TransferHandler implements ActionListener,CellEditorListener
	{
	private static final long serialVersionUID = 1L;
	MListener mListener = new MListener();
	public FileChooser fc = new FileChooser();

	private Listener()
		{
		}

	private static final class LazyHolder
		{
		public static final Listener INSTANCE = new Listener();
		}
	
	public static Listener getInstance()
		{
		return LazyHolder.INSTANCE;
		}

	public static class ResourceAdder implements ActionListener
		{
		public boolean insert;
		public Class<?> k;

		public ResourceAdder(boolean insert, Class<?> k)
			{
			this.insert = insert;
			this.k = k;
			}

		public void actionPerformed(ActionEvent e)
			{
			if (insert)
				Listener.insertResource(LGM.tree,k);
			else
				Listener.addResource(LGM.tree,k);
			}
		}

	public static void addResource(JTree tree, Class<?> r)
		{
		addResource(tree,r,null);
		}

	protected static void addResource(JTree tree, Class<?> r, Resource<?,?> res)
		{
		ResNode node = (ResNode) tree.getLastSelectedPathComponent();
		if (node == null) return;
		ResNode parent;
		int pos;
		if (node.getAllowsChildren())
			{
			parent = node;
			pos = parent.getChildCount();
			}
		else
			{
			parent = (ResNode) node.getParent();
			pos = parent.getIndex(node) + 1;
			}
		putNode(tree,node,parent,r,pos,res);
		}

	public static void insertResource(JTree tree, Class<?> r)
		{
		insertResource(tree,r,null);
		}

	protected static void insertResource(JTree tree, Class<?> r, Resource<?,?> res)
		{
		ResNode node = (ResNode) tree.getLastSelectedPathComponent();
		if (node == null) return;
		ResNode parent = (ResNode) node.getParent();
		if (parent.isRoot())
			{
			addResource(tree,r,res);
			return;
			}
		int pos = parent.getIndex(node);
		putNode(tree,node,parent,r,pos,res);
		}

	public static void putNode(JTree tree, ResNode node, ResNode parent, Class<?> r, int pos,
			Resource<?,?> res)
		{
		if (r == null)
			{
			String msg = Messages.getString("Listener.INPUT_GROUPNAME"); //$NON-NLS-1$
			String name = JOptionPane.showInputDialog(msg,
					Messages.getString("Listener.DEFAULT_GROUPNAME")); //$NON-NLS-1$
			if (name == null || name.isEmpty()) return;
			ResNode g = new ResNode(name,ResNode.STATUS_GROUP,parent.kind);
			parent.insert(g,pos);
			tree.expandPath(new TreePath(parent.getPath()));
			tree.setSelectionPath(new TreePath(g.getPath()));
			tree.updateUI();
			return;
			}

		if (node.kind != r)
			{
			parent = getPrimaryParent(r);
			pos = parent.getChildCount();
			}

		Resource<?,?> resource = res == null ? LGM.currentFile.resMap.get(parent.kind).getResource()
				: res;
		ResNode g = new ResNode(resource.getName(),ResNode.STATUS_SECONDARY,parent.kind,
				resource.reference);
		parent.insert(g,pos);
		tree.expandPath(new TreePath(parent.getPath()));
		tree.setSelectionPath(new TreePath(g.getPath()));
		tree.updateUI();
		g.openFrame(true);
		}

	/** Deletes the given resource nodes, including groups, and 
	 * returns the index (row) of the last node that was deleted.
	 * @param resources An array of resource nodes to delete.
	 */
	protected static int deleteResources(Object[] resources, JTree tree) 
	{
		HashSet<Resource<?,?>> rs = new HashSet<Resource<?,?>>();
		int last = -1;
		for (int i = 0; i < resources.length; i++)
		{
			ResNode node = (ResNode) resources[i];
			if (node.status == ResNode.STATUS_SECONDARY)
			{
				if (node.frame != null) node.frame.dispose();
				Resource<?,?> res = deRef((ResourceReference<?>) node.getRes());
				if (res != null) rs.add(res);
				((ResourceList<?>) LGM.currentFile.resMap.get(node.kind)).remove(res);
				last = tree.getRowForPath(new TreePath(node));
			} else if (node.status == ResNode.STATUS_GROUP) {
				node.removeFromParent();
				last = tree.getRowForPath(new TreePath(node));
			}
		}
		for (Resource<?,?> r : rs)
			r.dispose();
		return last;
	}
		
	
	protected static void deleteSelectedResources(JTree tree)
		{
		String msg = Messages.getString("Listener.CONFIRM_DELETERESOURCE"); //$NON-NLS-1$
		if (JOptionPane.showConfirmDialog(null,msg,
				Messages.getString("Listener.CONFIRM_DELETERESOURCE_TITLE"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION) == 0)
			{
			TreePath[] selections = tree.getSelectionPaths();
			
			//NOTE: Must be obtained before the for loop deletes the path.
			int row = -1;
					
			for (int i = 0; i < selections.length; i++) {
				row = deleteResources(selections[i].getPath(), tree);
			}
			
			if (row != -1) {
				tree.setSelectionPath(tree.getNextMatch("",row,Position.Bias.Forward));
			}			
			tree.updateUI();
			}
		}

	public void actionPerformed(ActionEvent e)
	{
		JTree tree = LGM.tree;
		String[] args = e.getActionCommand().split(" "); //$NON-NLS-1$
		String com = args[0];
		if (com.endsWith(".NEW")) //$NON-NLS-1$
		{
			String title = Messages.getString("Listener.CONFIRM_NEW_TITLE"); //$NON-NLS-1$
			String message = Messages.getString("Listener.CONFIRM_NEW"); //$NON-NLS-1$
			int opt = JOptionPane.showConfirmDialog(LGM.frame,message,title,JOptionPane.YES_NO_OPTION);
			//I'd love to default to "No", but apparently that's not an option.
			if (opt == JOptionPane.YES_OPTION) fc.newFile();
			return;
		}
		if (com.endsWith(".OPEN")) //$NON-NLS-1$
		{
			try
				{
					fc.open(args.length > 1 ? new URI(args[1]) : null); 
				}
			catch (URISyntaxException e1)
				{
				e1.printStackTrace();
				}
			return;
		}
		if (com.endsWith(".OPENRECENT")) //$NON-NLS-1$
		{
			try
				{
				URI path = new URI("");
				if (args.length > 1) {
					path = new URI(args[1]);
				}
				File f = new File(path);
				if(f.exists()) { 
					fc.open(path); 
				} else {
					JOptionPane.showMessageDialog(null,path.getPath(), "Error! File does not exist.", JOptionPane.ERROR_MESSAGE);
				}
				
				}
			catch (URISyntaxException e1)
				{
				e1.printStackTrace();
				}
			return;
		}
		if (com.endsWith(".SAVE")) //$NON-NLS-1$
		{
			fc.save(LGM.currentFile.uri,LGM.currentFile.format);
			return;
		}
		if (com.endsWith(".PREFERENCES")) //$NON-NLS-1$
		{
			LGM.showPreferences();
			return;
		}
		if (com.endsWith(".GMI")) //$NON-NLS-1$
		{
		  LGM.showGameInformation();
			return;
		}
		if (com.endsWith(".GMS")) //$NON-NLS-1$
		{
		  LGM.showGameSettings();
			return;
		}
		if (com.endsWith(".EXT")) //$NON-NLS-1$
		{
		  LGM.showGameExtensions();
			return;
		}
		if (com.endsWith(".SAVEAS")) //$NON-NLS-1$
		{
			fc.saveNewFile();
			return;
		}
		if (com.endsWith(".EVENT_BUTTON")) //$NON-NLS-1$
		{
			Object o = e.getSource();
			if (o instanceof JToggleButton) LGM.showEventPanel();
			return;
		}
		if (com.endsWith(".EXIT")) //$NON-NLS-1$
		{
		  LGM.onMainFrameClosed();
			
			return;
		}
		if (com.contains(".INSERT_") || com.contains(".ADD_")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			if (com.endsWith("GROUP")) //$NON-NLS-1$
				{
				if (com.contains(".INSERT_"))
					insertResource(tree,null);
				else
					addResource(tree,null);
				return;
				}
			//we no longer do it this way for resources
			throw new UnsupportedOperationException(com);
		}
		if (com.endsWith(".RENAME")) //$NON-NLS-1$
		{
			if (tree.getCellEditor().isCellEditable(null))
				tree.startEditingAtPath(tree.getLeadSelectionPath());
			return;
		}
		if (com.endsWith(".DELETE")) //$NON-NLS-1$
		{
			deleteSelectedResources(tree);
			return;
		}
		if (com.endsWith(".DEFRAGIDS")) //$NON-NLS-1$
		{
			String msg = Messages.getString("Listener.CONFIRM_DEFRAGIDS"); //$NON-NLS-1$
			if (JOptionPane.showConfirmDialog(LGM.frame,msg,
					Messages.getString("Listener.CONFIRM_DEFRAGIDS_TITLE"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION) == 0) LGM.currentFile.defragIds();
		}
		if (com.endsWith(".EXPAND")) //$NON-NLS-1$
		{
			for (int m = 0; m < tree.getRowCount(); m++)
				tree.expandRow(m);
			return;
		}
		if (com.endsWith(".COLLAPSE")) //$NON-NLS-1$
		{
			for (int m = tree.getRowCount() - 1; m >= 0; m--)
				tree.collapseRow(m);
			return;
		}
		if (com.endsWith(".MANUAL")) //$NON-NLS-1$
		{
	    try {
        // Auto detects if path is web url or local file
        String path = Prefs.manualPath;
        if (path.startsWith("http://") || path.startsWith("https://")) {
          Desktop.getDesktop().browse(java.net.URI.create(path));
        } else {
          Desktop.getDesktop().open(new File(path));
        }
      }
      catch (java.io.IOException ioe) {
        System.out.println(ioe.getMessage());
      }
			return;
		}
		if (com.endsWith(".ABOUT")) //$NON-NLS-1$
		{
			new AboutBox(LGM.frame).setVisible(true);
			return;
		}
	}

	public static ResNode getPrimaryParent(Class<?> kind)
	{
		for (int i = 0; i < LGM.root.getChildCount(); i++)
			if (((ResNode) LGM.root.getChildAt(i)).kind == kind) return (ResNode) LGM.root.getChildAt(i);
		return null;
	}

	protected Transferable createTransferable(JComponent c)
	{
		ResNode n = (ResNode) ((JTree) c).getLastSelectedPathComponent();

		if (n.status == ResNode.STATUS_PRIMARY) return null;
		if (!n.isInstantiable()) return null;
		return n;
	}

	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}

	public boolean canImport(TransferHandler.TransferSupport support)
	{
		if (!support.isDataFlavorSupported(ResNode.NODE_FLAVOR)) return false;
		// the above method uses equals(), which does not work as expected
		for (DataFlavor f : support.getDataFlavors())
			if (f != ResNode.NODE_FLAVOR) return false;
		TreePath drop = ((JTree.DropLocation) support.getDropLocation()).getPath();
		if (drop == null) return false;
		ResNode dropNode = (ResNode) drop.getLastPathComponent();
		ResNode dragNode = (ResNode) ((JTree) support.getComponent()).getLastSelectedPathComponent();
		if (dragNode == dropNode) return false;
		if (dragNode.isNodeDescendant(dropNode)) return false;
		if (Prefs.groupKind && dropNode.kind != dragNode.kind) return false;
		if (dropNode.status == ResNode.STATUS_SECONDARY) return false;
		return true;
	}

	public boolean importData(TransferHandler.TransferSupport support)
	{
		if (!canImport(support)) return false;
		JTree.DropLocation drop = (JTree.DropLocation) support.getDropLocation();
		int dropIndex = drop.getChildIndex();
		ResNode dropNode = (ResNode) drop.getPath().getLastPathComponent();
		ResNode dragNode = (ResNode) ((JTree) support.getComponent()).getLastSelectedPathComponent();
		if (dropIndex == -1)
		{
			dropIndex = dropNode.getChildCount();
		}
		if (dropNode == dragNode.getParent() && dropIndex > dragNode.getParent().getIndex(dragNode))
			dropIndex--;
		dropNode.insert(dragNode,dropIndex);
		LGM.tree.expandPath(new TreePath(dropNode.getPath()));
		LGM.tree.updateUI();
		return true;
	}

	public static class NodeMenuListener implements ActionListener
	{
		ResNode node;

		public NodeMenuListener(ResNode node)
		{
			this.node = node;
		}

		public void actionPerformed(ActionEvent e)
		{
			JTree tree = LGM.tree;
			String com = e.getActionCommand().substring(e.getActionCommand().lastIndexOf('_') + 1);
			if (com.equals("EDIT")) //$NON-NLS-1$
			{
				if (node.status == ResNode.STATUS_SECONDARY) node.openFrame();
				return;
			}
			if (com.equals("DELETE")) //$NON-NLS-1$
			{
				deleteSelectedResources(tree);
				return;
			}
			if (com.equals("RENAME")) //$NON-NLS-1$
			{
				if (tree.getCellEditor().isCellEditable(null))
					tree.startEditingAtPath(tree.getLeadSelectionPath());
				return;
			}
			if (com.equals("GROUP")) //$NON-NLS-1$
			{
				if (node.status == ResNode.STATUS_SECONDARY)
					insertResource(tree,null);
				else
					addResource(tree,null);
				return;
			}
			if (com.equals("INSERT")) //$NON-NLS-1$
			{
				insertResource(tree,node.kind);
				return;
			}
			if (com.equals("ADD")) //$NON-NLS-1$
			{
				addResource(tree,node.kind);
				return;
			}
			if (com.equals("DUPLICATE")) //$NON-NLS-1$
			{
				ResourceList<?> rl = (ResourceList<?>) LGM.currentFile.resMap.get(node.kind);
				if (node.frame != null) node.frame.commitChanges();
				Resource<?,?> resource = rl.duplicate(node.getRes().get());
				Listener.addResource(tree,node.kind,resource);
				return;
			}
		}
	}

	private static class MListener extends MouseAdapter
		{
		public MListener()
			{
			super();
			}

		public void mousePressed(MouseEvent e)
			{
			int selRow = LGM.tree.getRowForLocation(e.getX(),e.getY());
			TreePath selPath = LGM.tree.getPathForLocation(e.getX(),e.getY());
			
			TreePath[] paths = LGM.tree.getSelectionPaths();
			boolean inpath = false;
			
			if (paths != null) {
				for (int i = 0; i < paths.length; i++) {
					if (paths[i].equals(selPath)) {
						inpath = true;
					}
				}
			}
			
			if (selRow != -1 && e.getModifiers() == InputEvent.BUTTON3_MASK && !inpath)
				LGM.tree.setSelectionPath(selPath);
			}

		public void mouseReleased(MouseEvent e)
			{
			TreePath p = LGM.tree.getPathForLocation(e.getX(), e.getY());
			if (e.getX() >= LGM.tree.getWidth() && e.getY() >= LGM.tree.getHeight() || p == null)
				return;
			ResNode node = (ResNode) p.getLastPathComponent();
			if(node == null)
				return;
			//Isn't Java supposed to handle ctrl+click for us? For some reason it doesn't.
			//TODO: Yes, some components let you call setComponentPopupMenu() which will handle it for you.
			//So this code should probably be moved.
			if (e.getModifiers() == InputEvent.BUTTON3_MASK && e.getClickCount() == 1)
				{
				node.showMenu(e);
				return;
				}
			if (e.getModifiers() == InputEvent.BUTTON1_MASK && e.getClickCount() == 2)
				{
				// kind must be a Resource kind
				if (node.status != ResNode.STATUS_SECONDARY) return;
				node.openFrame();
				return;
				}
			}
		}

	public void editingCanceled(ChangeEvent e)
		{ //Unused
		}

	public void editingStopped(ChangeEvent e)
		{
		ResNode node = (ResNode) LGM.tree.getLastSelectedPathComponent();
		if (node.status == ResNode.STATUS_SECONDARY && node.isEditable())
			{
			String txt = ((String) node.getUserObject()).replaceAll("\\W","").replaceAll("^([0-9]+)",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			Resource<?,?> r = deRef((ResourceReference<?>) node.getRes());
			if (r != null) r.setName(txt);
			}
		}
	}
