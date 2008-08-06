/*
 * Copyright (C) 2006, 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import static org.lateralgm.main.Util.refsAreEqual;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.lang.ref.WeakReference;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
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

public class ResNode extends DefaultMutableTreeNode implements Transferable
	{
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
	public byte kind;
	/**
	 * The <code>Resource</code> this node represents.
	 */
	private WeakReference<? extends Resource<?>> res;
	public ResourceFrame<?> frame = null;
	private EventListenerList listenerList;
	private ChangeEvent changeEvent;
	private Icon icon;

	public Icon getIcon()
		{
		if (status == STATUS_SECONDARY) switch (kind)
			{
			case Resource.SPRITE:
			case Resource.BACKGROUND:
			case Resource.GMOBJECT:
				if (icon == null) updateIcon();
				return icon;
			default:
				return Resource.ICON[kind];
			}
		if (Prefs.iconizeGroup && getChildCount() > 0)
			{
			ResNode n = (ResNode) getChildAt(0);
			if (n.status == STATUS_SECONDARY) return n.getIcon();
			}
		return null;
		}

	@SuppressWarnings("unchecked")
	public void updateIcon()
		{
		switch (kind)
			{
			case Resource.SPRITE:
				icon = GmTreeGraphics.getSpriteIcon((WeakReference<Sprite>) res);
				break;
			case Resource.BACKGROUND:
				icon = GmTreeGraphics.getBackgroundIcon((WeakReference<Background>) res);
				break;
			case Resource.GMOBJECT:
				WeakReference<Sprite> r = ((WeakReference<GmObject>) res).get().sprite;
				icon = GmTreeGraphics.getSpriteIcon(r);
				break;
			}
		fireStateChanged(null);
		LGM.tree.repaint();
		}

	public ResNode(String name, byte status, byte kind, WeakReference<? extends Resource<?>> res)
		{
		super(name);
		this.status = status;
		this.kind = kind;
		setRes(res);
		}

	public ResNode(String name, byte status, byte kind)
		{
		this(name,status,kind,null);
		}

	public ResNode addChild(String name, byte stat, byte type)
		{
		ResNode b = new ResNode(name,stat,type,null);
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
		if (SubframeInformer.fireSubframeRequest(res.get(),this)) return;
		if (frame == null)
			{
			ResourceFrame<?> rf = null;
			switch (kind)
				{
				case Resource.SPRITE:
					rf = new SpriteFrame((Sprite) res.get(),this);
					break;
				case Resource.SOUND:
					rf = new SoundFrame((Sound) res.get(),this);
					break;
				case Resource.BACKGROUND:
					rf = new BackgroundFrame((Background) res.get(),this);
					break;
				case Resource.PATH:
					rf = new PathFrame((Path) res.get(),this);
					break;
				case Resource.SCRIPT:
					rf = new ScriptFrame((Script) res.get(),this);
					break;
				case Resource.FONT:
					rf = new FontFrame((Font) res.get(),this);
					break;
				case Resource.TIMELINE:
					rf = new TimelineFrame((Timeline) res.get(),this);
					break;
				case Resource.GMOBJECT:
					rf = new GmObjectFrame((GmObject) res.get(),this);
					break;
				case Resource.ROOM:
					rf = new RoomFrame((Room) res.get(),this);
					break;
				}
			if (rf != null)
				{
				frame = rf;
				LGM.mdi.add(rf);
				}
			}
		if (frame != null)
			{
			SubframeInformer.fireSubframeAppear(frame);
			frame.setVisible(true);
			frame.toTop();
			}
		}

	public void updateFrame()
		{
		if (status == STATUS_SECONDARY)
			{
			String txt = (String) getUserObject();
			res.get().setName(txt);
			if (frame != null)
				{
				frame.setTitle(txt);
				frame.name.setText(txt);
				}
			}
		}

	public void add(MutableTreeNode arg0)
		{
		super.add(arg0);
		fireStateChanged();
		}

	public void insert(MutableTreeNode newChild, int childIndex)
		{
		super.insert(newChild,childIndex);
		fireStateChanged();
		}

	public void remove(int childIndex)
		{
		super.remove(childIndex);
		fireStateChanged();
		}

	public void setUserObject(Object obj)
		{
		super.setUserObject(obj);
		fireStateChanged(null);
		}

	private EventListenerList getListenerList()
		{
		if (listenerList == null) listenerList = new EventListenerList();
		return listenerList;
		}

	/**
	 * Adds the specified ChangeListener.
	 * Note that if a null ChangeEvent is provided to <code>l</code>,
	 * this denotes a change in the userObject or icon.
	 * Otherwise, there has been a change in the structure of the tree. 
	 * @param l The ChangeListener to add
	 */
	public void addChangeListener(ChangeListener l)
		{
		getListenerList().add(ChangeListener.class,l);
		}

	public void removeChangeListener(ChangeListener l)
		{
		getListenerList().remove(ChangeListener.class,l);
		}

	protected void fireStateChanged()
		{
		// Lazily create the event:
		if (changeEvent == null) changeEvent = new ChangeEvent(this);
		fireStateChanged(changeEvent);
		}

	protected void fireStateChanged(ChangeEvent e)
		{
		if (listenerList != null)
			{
			Object[] list = listenerList.getListenerList();
			for (int i = list.length - 2; i >= 0; i -= 2)
				if (list[i] == ChangeListener.class) ((ChangeListener) list[i + 1]).stateChanged(e);
			}
		// Propogate structure changes up the tree
		if (e != null && parent != null && parent instanceof ResNode)
			((ResNode) parent).fireStateChanged(e);
		}

	/**
	 * Recursively checks (from this node down) for a node with a res field
	 * referring to the same instance as res.
	 * @param res The resource to look for
	 * @return Whether the resource was found
	 */
	public boolean contains(WeakReference<? extends Resource<?>> res)
		{
		if (refsAreEqual(this.res,res)) return true; //Just in case
		if (children != null) for (Object obj : children)
			if (obj instanceof ResNode)
				{
				ResNode node = (ResNode) obj;
				if (node.isLeaf())
					{
					if (refsAreEqual(node.res,res)) return true;
					}
				else
					{
					if (node.contains(res)) return true;
					}
				}
		return false;
		}

	public void setRes(WeakReference<? extends Resource<?>> res)
		{
		if (this.res != null) this.res.get().setNode(null);
		this.res = res;
		if (res != null) res.get().setNode(this);
		}

	public WeakReference<? extends Resource<?>> getRes()
		{
		return res;
		}
	}
