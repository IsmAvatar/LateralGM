/*
 * Copyright (C) 2006, 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.Resource;
import org.lateralgm.subframes.BackgroundFrame;
import org.lateralgm.subframes.FontFrame;
import org.lateralgm.subframes.GmObjectFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.RoomFrame;
import org.lateralgm.subframes.ScriptFrame;
import org.lateralgm.subframes.SoundFrame;
import org.lateralgm.subframes.SpriteFrame;
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
	public byte status;
	public byte kind;
	public ResId resourceId;
	public ResourceFrame<?> frame = null;

	public ResNode(String name, byte status, byte kind, ResId res)
		{
		super(name);
		this.status = status;
		this.kind = kind;
		resourceId = res;
		}

	public ResNode(String name, int status, int kind, ResId res)
		{
		this(name,(byte) status,(byte) kind,res);
		}

	public ResNode(String name, int status, int kind)
		{
		this(name,status,kind,null);
		}

	public ResNode addChild(String name, byte stat, byte type)
		{
		ResNode b = new ResNode(name,stat,type);
		add(b);
		return b;
		}

	public ResNode addChild(String name, int stat, int type)
		{
		return addChild(name,(byte) stat,(byte) type);
		}

	public boolean getAllowsChildren()
		{
		if (status == STATUS_SECONDARY) return false;
		if (Prefs.protectRoot && this == LGM.root) return false;
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
		if (frame == null)
			{
			ResourceFrame<?> rf = null;
			switch (kind)
				{
				case Resource.SPRITE:
					rf = new SpriteFrame(LGM.currentFile.sprites.get(resourceId),this);
					break;
				case Resource.SOUND:
					rf = new SoundFrame(LGM.currentFile.sounds.get(resourceId),this);
					break;
				case Resource.BACKGROUND:
					rf = new BackgroundFrame(LGM.currentFile.backgrounds.get(resourceId),this);
					break;
				case Resource.SCRIPT:
					rf = new ScriptFrame(LGM.currentFile.scripts.get(resourceId),this);
					break;
				case Resource.FONT:
					rf = new FontFrame(LGM.currentFile.fonts.get(resourceId),this);
					break;
				case Resource.TIMELINE:
					rf = new TimelineFrame(LGM.currentFile.timelines.get(resourceId),this);
					break;
				case Resource.GMOBJECT:
					rf = new GmObjectFrame(LGM.currentFile.gmObjects.get(resourceId),this);
					break;
				case Resource.ROOM:
					rf = new RoomFrame(LGM.currentFile.rooms.get(resourceId),this);
					break;
				default:
					rf = null;
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
			frame.setVisible(true);
			frame.toTop();
			}
		}

	public void updateFrame()
		{
		if (status == STATUS_SECONDARY)
			{
			String txt = (String) getUserObject();
			LGM.currentFile.getList(kind).get(resourceId).setName(txt);
			if (frame != null)
				{
				frame.setTitle(txt);
				frame.name.setText(txt);
				}
			}
		}
	}
