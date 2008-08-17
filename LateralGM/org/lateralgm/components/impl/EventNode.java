/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.lang.ref.WeakReference;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class EventNode extends DefaultMutableTreeNode implements Transferable
	{
	private static final long serialVersionUID = 1L;
	public static final DataFlavor EVENTNODE_FLAVOR = new DataFlavor(
			DataFlavor.javaJVMLocalObjectMimeType,"Event Node");
	private DataFlavor[] flavors = { EVENTNODE_FLAVOR };
	public int mainId;
	public int eventId;
	public WeakReference<GmObject> other;
	public Icon icon = null;

	public EventNode(String text, Icon icon)
		{
		this(text,-1,0,icon);
		}

	public EventNode(String text, int mainId, int eventId, Icon icon)
		{
		setUserObject(text);
		this.mainId = mainId;
		this.eventId = eventId;
		this.icon = icon;
		}

	public void add(int mainId, int eventId, Icon icon)
		{
		add(new EventNode(Event.eventName(mainId,eventId),mainId,eventId,icon));
		}

	public void add(int mainId, Icon icon)
		{
		add(new EventNode(Messages.getString("MainEvent.EVENT" + mainId),mainId,0,icon)); //$NON-NLS-1$
		}

	public boolean isValid()
		{
		switch (mainId)
			{
			case MainEvent.EV_KEYBOARD:
			case MainEvent.EV_KEYPRESS:
			case MainEvent.EV_KEYRELEASE:
				return Event.KEYS.contains(eventId);
			case MainEvent.EV_COLLISION:
				return other != null;
			default:
				return true;
			}
		}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
		if (flavor != EVENTNODE_FLAVOR) throw new UnsupportedFlavorException(flavor);
		return this;
		}

	public DataFlavor[] getTransferDataFlavors()
		{
		return flavors;
		}

	public boolean isDataFlavorSupported(DataFlavor flavor)
		{
		return flavor.equals(EVENTNODE_FLAVOR);
		}
	}
