/*
 * Copyright (C) 2006, 2007, 2009 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.Util;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.Resource.Kind;
import org.lateralgm.subframes.BackgroundFrame;
import org.lateralgm.subframes.FontFrame;
import org.lateralgm.subframes.GmObjectFrame;
import org.lateralgm.subframes.PathFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.RoomFrame;
import org.lateralgm.subframes.ScriptFrame;
import org.lateralgm.subframes.SoundFrame;
import org.lateralgm.subframes.SpriteFrame;
import org.lateralgm.subframes.SubframeInformer;
import org.lateralgm.subframes.TimelineFrame;

public class ResNode extends DefaultMutableTreeNode implements Transferable,UpdateListener
	{
	public static final Map<Kind,ImageIcon> ICON;
	static
		{
		Map<Kind,ImageIcon> m = new EnumMap<Kind,ImageIcon>(Kind.class);
		for (Kind k : Kind.values())
			m.put(k,LGM.getIconForKey("Resource." + k.name()));
		ICON = java.util.Collections.unmodifiableMap(m);
		}

	private static final long serialVersionUID = 1L;
	public static final DataFlavor NODE_FLAVOR = new DataFlavor(
			DataFlavor.javaJVMLocalObjectMimeType,"Node");
	private DataFlavor[] flavors = { NODE_FLAVOR };
	public static final byte STATUS_PRIMARY = 1;
	public static final byte STATUS_GROUP = 2;
	public static final byte STATUS_SECONDARY = 3;
	/** One of PRIMARY, GROUP, or SECONDARY*/
	public byte status;
	/** What kind of Resource this is */
	public Resource.Kind kind;
	/**
	 * The <code>Resource</code> this node represents.
	 */
	private final ResourceReference<? extends Resource<?,?>> res;
	public ResourceFrame<?,?> frame = null;
	private Icon icon;
	private final NameUpdater nameUpdater = new NameUpdater();
	private final UpdateTrigger trigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,trigger);
	public boolean newRes = false;

	public Icon getIcon()
		{
		if (status == STATUS_SECONDARY) switch (kind)
			{
			case SPRITE:
			case BACKGROUND:
			case OBJECT:
				if (icon == null) updateIcon();
				return icon;
			default:
				return ICON.get(kind);
			}
		if (Prefs.iconizeGroup && getChildCount() > 0)
			{
			ResNode n = (ResNode) getChildAt(0);
			if (n.status == STATUS_SECONDARY) return n.getIcon();
			}
		return null;
		}

	private void updateIcon()
		{
		icon = GmTreeGraphics.getResourceIcon(res);
		}

	public ResNode(String name, byte status, Resource.Kind kind,
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

	public ResNode(String name, byte status, Kind kind)
		{
		this(name,status,kind,null);
		}

	public ResNode addChild(String name, byte stat, Kind k)
		{
		ResNode b = new ResNode(name,stat,k,null);
		add(b);
		return b;
		}

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
		return flavor == NODE_FLAVOR;
		}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
		if (flavor != NODE_FLAVOR) throw new UnsupportedFlavorException(flavor);
		return this;
		}

	public void openFrame()
		{
		openFrame(false);
		}

	public void openFrame(boolean newRes)
		{
		this.newRes = newRes;
		Resource<?,?> r = deRef();
		if (SubframeInformer.fireSubframeRequest(r,this)) return;
		if (frame == null)
			{
			MDIFrame rf = null;
			switch (kind)
				{
				case SPRITE:
					rf = new SpriteFrame((Sprite) r,this);
					break;
				case SOUND:
					rf = new SoundFrame((Sound) r,this);
					break;
				case BACKGROUND:
					rf = new BackgroundFrame((Background) r,this);
					break;
				case PATH:
					rf = new PathFrame((Path) r,this);
					break;
				case SCRIPT:
					rf = new ScriptFrame((Script) r,this);
					break;
				case FONT:
					rf = new FontFrame((Font) r,this);
					break;
				case TIMELINE:
					rf = new TimelineFrame((Timeline) r,this);
					break;
				case OBJECT:
					rf = new GmObjectFrame((GmObject) r,this);
					break;
				case ROOM:
					rf = new RoomFrame((Room) r,this);
					break;
				case GAMEINFO:
					rf = LGM.getGameInfo();
					SubframeInformer.fireSubframeAppear(rf);
					rf.toTop();
					return;
				case GAMESETTINGS:
					rf = LGM.getGameSettings();
					SubframeInformer.fireSubframeAppear(rf);
					rf.toTop();
					return;
				case EXTENSIONS:
					//TODO: Create Extensions front-end
					return;
				}
			if (rf != null)
				{
				frame = (ResourceFrame<?,?>) rf;
				LGM.mdi.add(rf);
				}
			}
		if (frame != null)
			{
			SubframeInformer.fireSubframeAppear(frame);
			frame.toTop();
			}
		}

	private JMenuItem makeMenuItem(String command, ActionListener al)
		{
		JMenuItem menuItem = new JMenuItem(Messages.getString(command));
		menuItem.setActionCommand(command);
		menuItem.addActionListener(al);
		return menuItem;
		}

	public void showMenu(MouseEvent e)
		{
		JPopupMenu popup = new JPopupMenu();
		ActionListener al = new Listener.NodeMenuListener(this);
		switch (kind)
			{
			case GAMESETTINGS:
			case GAMEINFO:
			case EXTENSIONS:
				popup.add(makeMenuItem("Listener.TREE_EDIT",al)); //$NON-NLS-1$
				popup.show(e.getComponent(),e.getX(),e.getY());
				return;
			}
		if (status == ResNode.STATUS_SECONDARY)
			{
			popup.add(makeMenuItem("Listener.TREE_EDIT",al)); //$NON-NLS-1$
			popup.addSeparator();
			popup.add(makeMenuItem("Listener.TREE_INSERT",al)); //$NON-NLS-1$
			popup.add(makeMenuItem("Listener.TREE_DUPLICATE",al)); //$NON-NLS-1$
			}
		else
			popup.add(makeMenuItem("Listener.TREE_ADD",al)); //$NON-NLS-1$
		popup.addSeparator();
		popup.add(makeMenuItem("Listener.TREE_GROUP",al)); //$NON-NLS-1$
		if (status != ResNode.STATUS_SECONDARY) popup.add(makeMenuItem("Listener.TREE_SORT",al)); //$NON-NLS-1$
		if (status != ResNode.STATUS_PRIMARY)
			{
			popup.addSeparator();
			popup.add(makeMenuItem("Listener.TREE_DELETE",al)); //$NON-NLS-1$
			popup.add(makeMenuItem("Listener.TREE_RENAME",al)); //$NON-NLS-1$
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

	public ResourceReference<? extends Resource<?,?>> getRes()
		{
		return res;
		}

	public void updated(UpdateEvent e)
		{
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
		public void run()
			{
			if (frame != null)
				{
				Resource<?,?> r = deRef();
				if (r != null)
					{
					String n = r.getName();
					frame.setTitle(n);
					if (!frame.name.getText().equals(n)) frame.name.setText(n);
					}
				}
			//FIXME: Update the tree by having it listen to its root node instead of here
			LGM.tree.updateUI();
			}
		}
	}
