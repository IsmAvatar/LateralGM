/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.awt.Dimension;
import java.awt.Point;
import java.lang.ref.WeakReference;

import org.lateralgm.file.GmFile;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Background;

public class Tile
	{
	public int tileId = 0;
	private WeakReference<Background> backgroundId = null;
	private Point bkgPos;
	private Point roomPos;
	private Dimension size;
	private int depth;
	public boolean locked = false;
	private boolean autoUpdate = true;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public Tile()
		{
		autoUpdate = false;
		}

	public Tile(GmFile f, WeakReference<Background> backgroundId, Point bkgPos, Point roomPos,
			Dimension size, int depth)
		{
		this(++f.lastTileId,backgroundId,bkgPos,roomPos,size,depth);
		}
	
	public Tile(int tileId, WeakReference<Background> backgroundId, Point bkgPos, Point roomPos,
			Dimension size, int depth)
		{
		this.tileId = tileId;
		this.backgroundId = backgroundId;
		this.bkgPos = bkgPos;
		this.roomPos = roomPos;
		this.size = size;
		this.depth = depth;
		fireUpdate();
		}

	public void setAutoUpdate(boolean auto)
		{
		autoUpdate = auto;
		if (auto) fireUpdate();
		}

	protected void fireUpdate()
		{
		if (autoUpdate) updateTrigger.fire();
		}

	public WeakReference<Background> getBackgroundId()
		{
		return backgroundId;
		}

	public void setBackgroundId(WeakReference<Background> backgroundId)
		{
		this.backgroundId = backgroundId;
		fireUpdate();
		}

	public Point getBackgroundPosition()
		{
		return bkgPos;
		}

	public void setBackgroundPosition(Point bkgPos)
		{
		this.bkgPos = bkgPos;
		fireUpdate();
		}

	public Point getRoomPosition()
		{
		return roomPos;
		}

	public void setRoomPosition(Point roomPos)
		{
		this.roomPos = roomPos;
		fireUpdate();
		}

	public Dimension getSize()
		{
		return size;
		}

	public void setSize(Dimension size)
		{
		this.size = size;
		fireUpdate();
		}

	public int getDepth()
		{
		return depth;
		}

	public void setDepth(int depth)
		{
		this.depth = depth;
		fireUpdate();
		}
	}
