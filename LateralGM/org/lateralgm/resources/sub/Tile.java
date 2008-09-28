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
	private boolean autoUpdate = false;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	/**
	 * Do not call this constructor unless you intend
	 * to handle your own tile ID. See Tile(GmFile f).
	 */
	public Tile()
		{
		}

	/**
	 * Constructs a tile for this GmFile, and determines ID via the last tile id.
	 * Notice that a tile initializes with no settings and with auto-update off.
	 * It is your responsibility to use the setters and call setAutoUpdate(true) when done.
	 */
	public Tile(GmFile f)
		{
		tileId = ++f.lastTileId;
		}

	/**
	 * Sets whether changing settings to this tile will inform its listeners.
	 * This is especially useful for applying multiple settings before updating.
	 * Setting this to true will cause this tile to update immediately.
	 */
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
