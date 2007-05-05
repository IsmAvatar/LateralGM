/*
 * Copyright (C) 2006, 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyVetoException;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.Resource;
import org.lateralgm.subframes.FontFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.ScriptFrame;
import org.lateralgm.subframes.SoundFrame;


public class ResNode extends DefaultMutableTreeNode implements Transferable
	{
	private static final long serialVersionUID = 1L;
	public static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType,"Node");
	private DataFlavor[] flavors = { NODE_FLAVOR };
	public static byte STATUS_PRIMARY = 1;
	public static byte STATUS_GROUP = 2;
	public static byte STATUS_SECONDARY = 3;
	public byte status;
	public byte kind;
	public ResId resourceId;
	public ResourceFrame frame = null;

	public ResNode(String name, byte status, byte kind, ResId res)
		{
		super(name);
		this.status = status;
		this.kind = kind;
		resourceId = res;
		}

	public ResNode(String name, int status, int kind, ResId res)
		{
		super(name);
		this.status = (byte) status;
		this.kind = (byte) kind;
		resourceId = res;
		}

	public ResNode(String name, int status, int kind)
		{
		super(name);
		this.status = (byte) status;
		this.kind = (byte) kind;
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
		if (Prefs.protectLeaf && status == STATUS_SECONDARY) return false;
		if (Prefs.protectRoot && this == LGM.root) return false;
		return true;
		}

	public DataFlavor[] getTransferDataFlavors()
		{
		return flavors;
		}

	public boolean isDataFlavorSupported(DataFlavor flavor)
		{
		return Arrays.asList(flavors).contains(flavor);
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
			ResourceFrame rf = null;
			switch (kind)
				{
				case Resource.SOUND:
					rf = new SoundFrame(LGM.currentFile.Sounds.get(resourceId),this);
					break;
				case Resource.SCRIPT:
					rf = new ScriptFrame(LGM.currentFile.Scripts.get(resourceId),this);
					break;
				case Resource.FONT:
					rf = new FontFrame(LGM.currentFile.Fonts.get(resourceId),this);
					break;
				}
			if (rf != null)
				{
				frame = rf;
				LGM.MDI.add(rf);
				rf.setVisible(true);
				}
			}
		else
			{
			frame.toFront();
			try
				{
				frame.setIcon(false);
				}
			catch (PropertyVetoException e)
				{
				e.printStackTrace();
				}
			}
		}

	public void updateFrame()
		{
		if (status == STATUS_SECONDARY)
			{
			String txt = (String) getUserObject();
			LGM.currentFile.getList(kind).get(resourceId).name = txt;
			if (frame != null)
				{
				frame.setTitle(txt);
				frame.name.setText(txt);
				}
			}
		}
	}