/*
 * Copyright (C) 2006, 2007, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * Copyright (C) 2013, 2014 Robert B. Colton
 * Copyright (C) 2014 egofree
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Sprite;
import org.lateralgm.subframes.InstantiableResourceFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.ResourceFrame.ResourceFrameFactory;
import org.lateralgm.subframes.RoomFrame;
import org.lateralgm.subframes.SubframeInformer;

public class ResNode extends DefaultNode implements Transferable,UpdateListener
	{
	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = -2072032756973999470L;

	public static DataFlavor DATA_FLAVOR;
	static
		{
		try
			{
			DATA_FLAVOR = Util.createJVMLocalDataFlavor(ResNode.class);
			}
		catch (ClassNotFoundException e)
			{
			LGM.showDefaultExceptionHandler(e);
			}
		}
	private static final DataFlavor[] flavors = { DATA_FLAVOR };

	public static final Map<Class<?>,ImageIcon> ICON;
	static
		{
		ICON = new HashMap<Class<?>,ImageIcon>();
		for (Entry<String,Class<? extends Resource<?,?>>> k : Resource.kindsByName3.entrySet())
			ICON.put(k.getValue(),LGM.getIconForKey("Resource." + k.getKey()));
		}

	public static final byte STATUS_PRIMARY = 1;
	public static final byte STATUS_GROUP = 2;
	public static final byte STATUS_SECONDARY = 3;
	/** One of PRIMARY, GROUP, or SECONDARY*/
	public byte status;
	/** What kind of Resource this is */
	public Class<?> kind;
	/**
	 * The <code>Resource</code> this node represents.
	 */
	private final ResourceReference<? extends Resource<?,?>> res;
	public ResourceFrame<?,?> frame = null;

	private final NameUpdater nameUpdater = new NameUpdater(this);
	private final UpdateTrigger trigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,trigger);
	public boolean newRes = false;

	@Override
	public Icon getIcon()
		{
		if (status == STATUS_SECONDARY)
			{
			if (kind == Sprite.class || kind == Background.class || kind == GmObject.class)
				{
				if (icon == null) updateIcon();
				return icon;
				}
			return ICON.get(kind);
			}
		if (Prefs.iconizeGroup && getChildCount() > 0)
			{
			ResNode n = (ResNode) getChildAt(0);
			if (n.status == STATUS_SECONDARY) return n.getIcon();
			}
		return null;
		}

	@Override
	public Icon getIconisedGroup() {
		if (status != ResNode.STATUS_PRIMARY && kind == Sprite.class || kind == Background.class || kind == GmObject.class)
			return getIcon();
		return null;
	}

	@Override
	public Icon getLeafIcon() {
		if (status == ResNode.STATUS_SECONDARY)
			return getIcon();
		return null;
	}

	// NOTE: DO NOT use this for tree graphics, format with HTML instead because there is a Java LNF bug that causes this not to work.
	public Font getFont(Font com) {
		if (Prefs.boldPrimaryNodes && status == ResNode.STATUS_PRIMARY) {
			com = com.deriveFont(Font.BOLD);
		} else {
			com = com.deriveFont(Font.PLAIN);
		}
		return com;
	}

	private void updateIcon()
		{
		icon = GmTreeGraphics.getResourceIcon(res);
		}

	public ResNode(String name, byte status, Class<?> kind,
			ResourceReference<? extends Resource<?,?>> res)
		{
		super(name);
		this.status = status;
		this.kind = kind;
		this.res = res;
		Resource<?,?> r = deRef();
		if (r != null)
			{
			r.setNode(this);
			res.updateSource.addListener(this);
			}
		}

	public ResNode(String name, byte status, Class<?> kind)
		{
		this(name,status,kind,null);
		}

	public ResNode addChild(String name, byte stat, Class<?> k)
		{
		ResNode b = new ResNode(name,stat,k,null);
		add(b);
		return b;
		}

	@Override
	public boolean getAllowsChildren()
		{
		if (status == STATUS_SECONDARY) return false;
		if (isRoot()) return false;
		return true;
		}

	public DataFlavor[] getTransferDataFlavors()
		{
		return flavors;
		}

	public boolean isDataFlavorSupported(DataFlavor flavor)
		{
		return flavor.equals(DATA_FLAVOR);
		}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
		if (flavor != DATA_FLAVOR) throw new UnsupportedFlavorException(flavor);
		return this;
		}

	@Override
	public void openFrame()
		{
		openFrame(false);
		}

	public void openFrame(boolean newRes)
		{
		this.newRes = newRes;
		Resource<?,?> r = deRef();
		if (r != null) {
			r.changed = newRes;
		}
		if (SubframeInformer.fireSubframeRequest(r,this)) return;
		ResourceFrame<?,?> rf = frame;
		boolean wasVisible = false;
		if (frame == null)
			{
			ResourceFrameFactory factory = ResourceFrame.factories.get(kind);
			rf = factory == null ? null : factory.makeFrame(r,this);
			if (rf != null)
				{
				frame = rf;
				if (rf instanceof InstantiableResourceFrame<?,?>) LGM.mdi.add(rf);
				}
			} else {
				wasVisible = frame.isVisible();
			}
		if (rf != null)
			{
			SubframeInformer.fireSubframeAppear(rf,wasVisible);
			rf.toTop();
			}
		}

	private static JMenuItem makeMenuItem(String command, ActionListener al, boolean setIcon)
		{
		JMenuItem menuItem = new JMenuItem(Messages.getString(command));
		menuItem.setActionCommand(command);
		menuItem.addActionListener(al);
		ImageIcon icon = LGM.getIconForKey(command);
		if (icon != null && setIcon)
			{
			menuItem.setIcon(icon);
			}
		//menuItem.addKeyListener(kl);
		menuItem.addKeyListener(null);
		return menuItem;
		}

	public void showMenu(MouseEvent e)
		{
		JPopupMenu popup = new JPopupMenu();
		ActionListener al = new Listener.NodeMenuListener(this);
		//KeyListener kl = new Listener.NodeKeyListener(this);

		if (!isInstantiable())
			{
			JMenuItem editItem = makeMenuItem("Listener.TREE_PROPERTIES",al,true);
			popup.add(editItem); //$NON-NLS-1$
			editItem.setFocusable(true);
			editItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("Listener.TREE_PROPERTIES")));
			popup.show(e.getComponent(),e.getX(),e.getY());
			return;
			}
		if (status == ResNode.STATUS_SECONDARY)
			{
			JMenuItem insertItem = makeMenuItem("Listener.TREE_INSERT_RESOURCE",al,true);
			insertItem.setFocusable(true);
			popup.add(insertItem); //$NON-NLS-1$
			insertItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("Listener.TREE_INSERT_RESOURCE")));
			JMenuItem duplicateItem = makeMenuItem("Listener.TREE_DUPLICATE_RESOURCE",al,true);
			duplicateItem.setFocusable(true);
			popup.add(duplicateItem); //$NON-NLS-1$
			duplicateItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("Listener.TREE_DUPLICATE_RESOURCE")));
			popup.add(makeMenuItem("Listener.TREE_INSERT_GROUP",al,true)); //$NON-NLS-1$
			}
		else
			{
			popup.add(makeMenuItem("Listener.TREE_CREATE_RESOURCE",al,true)); //$NON-NLS-1$
			popup.add(makeMenuItem("Listener.TREE_CREATE_GROUP",al,true)); //$NON-NLS-1$
			}
		if (status != ResNode.STATUS_SECONDARY) popup.add(makeMenuItem("Listener.TREE_SORT",al,false)); //$NON-NLS-1$
		if (status != ResNode.STATUS_PRIMARY)
			{
			popup.addSeparator();
			JMenuItem deleteItem = makeMenuItem("Listener.TREE_DELETE",al,true);
			deleteItem.setFocusable(true);
			deleteItem.requestFocus();
			popup.add(deleteItem); //$NON-NLS-1$
			// KeyStroke.getKeyStroke("BACK_SPACE"); for delete key on mac
			deleteItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("Listener.TREE_DELETE")));
			JMenuItem renameItem = makeMenuItem("Listener.TREE_RENAME",al,true);
			renameItem.setFocusable(true);
			popup.add(renameItem); //$NON-NLS-1$
			renameItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("Listener.TREE_RENAME")));
			}
		if (status == ResNode.STATUS_SECONDARY)
			{
			JMenuItem editItem = makeMenuItem("Listener.TREE_PROPERTIES",al,true);
			editItem.setFocusable(true);
			popup.add(editItem); //$NON-NLS-1$
			editItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("Listener.TREE_PROPERTIES")));
			}
		popup.show(e.getComponent(),e.getX(),e.getY());
		}

	public void add(MutableTreeNode arg0)
		{
		super.add(arg0);
		fireUpdate();
		}

	public void insert(MutableTreeNode newChild, int childIndex)
		{
		super.insert(newChild,childIndex);
		fireUpdate();
		}

	public void remove(int childIndex)
		{
		super.remove(childIndex);
		fireUpdate();
		}

	private void fireUpdate()
		{
		fireUpdate(trigger.getEvent());
		}

	private Resource<?,?> deRef()
		{
		return Util.deRef((ResourceReference<?>) res);
		}

	private void fireUpdate(UpdateEvent e)
		{
		trigger.fire(e);
		if (e != null && parent != null && parent instanceof ResNode) ((ResNode) parent).fireUpdate(e);
		}

	/**
	 * Recursively checks (from this node down) for a node with a res field
	 * referring to the same instance as res.
	 * @param res The resource to look for
	 * @return Whether the resource was found
	 */
	public boolean contains(ResourceReference<? extends Resource<?,?>> res)
		{
		if (this.res == res) return true; //Just in case
		if (children != null) for (Object obj : children)
			if (obj instanceof ResNode)
				{
				ResNode node = (ResNode) obj;
				if (node.isLeaf())
					{
					if (node.res == res) return true;
					}
				else
					{
					if (node.contains(res)) return true;
					}
				}
		return false;
		}

	//TODO: This should be generic if ResNode is ever changed to have
	//generic tree node children.
	public Vector<ResNode> getChildren()
		{
		return (Vector)children;
		}

	public ResourceReference<? extends Resource<?,?>> getRes()
		{
		return res;
		}

	public void updated(UpdateEvent e)
		{

		// If a sprite, a background, or an object has been udpated, reset the undo
		if (kind == Sprite.class || kind == Background.class || kind == GmObject.class)
			{
			for (JInternalFrame room : LGM.mdi.getAllFrames())
				{
				if (room instanceof RoomFrame) ((RoomFrame) room).resetUndoManager();
				}
			}

		if (status == STATUS_SECONDARY)
			{
			icon = null;
			Resource<?,?> r = deRef();
			if (r != null)
				{
				setUserObject(r.getName());
				Util.invokeOnceLater(nameUpdater);
				}
			else
				removeFromParent();
			}
		fireUpdate(e);
		}

	private class NameUpdater implements Runnable
		{
		ResNode node;

		public NameUpdater(ResNode resNode)
			{
			node = resNode;
			}

		public void run()
			{
			if (frame != null && frame instanceof InstantiableResourceFrame<?,?>)
				{
				InstantiableResourceFrame<?,?> resFrame = (InstantiableResourceFrame<?,?>) frame;
				Resource<?,?> r = deRef();
				if (r != null)
					{
					String n = r.getName();
					resFrame.setTitle(n);
					if (!resFrame.name.getText().equals(n)) resFrame.name.setText(n);
					}
				}

			if (LGM.tree != null)
				{
				//FIXME: Update the tree by having it listen to its root node instead of here
				LGM.tree.updateUI();

				// Never update the entire tree UI for a single node, just reload the node.
				DefaultTreeModel model = ((DefaultTreeModel) LGM.tree.getModel());
				model.reload(node);
				}
			}
		}

	public boolean isInstantiable()
		{
		return InstantiableResource.class.isAssignableFrom(kind);
		}

	public boolean isEditable()
		{
		return isInstantiable();
		}
	}
