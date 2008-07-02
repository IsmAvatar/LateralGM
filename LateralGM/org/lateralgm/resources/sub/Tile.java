/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.lang.ref.WeakReference;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Background;

public class Tile
	{
	private int x = 0;
	private int y = 0;
	private WeakReference<Background> backgroundId = null;
	private int tileX = 0;
	private int tileY = 0;
	private int width = 16;
	private int height = 16;
	private int depth = 0;
	public int tileId = 0;
	public boolean locked = false;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	protected void fireUpdate()
		{
		updateTrigger.fire();
		}

	public int getX()
		{
		return x;
		}

	public void setX(int x)
		{
		this.x = x;
		fireUpdate();
		}

	public int getY()
		{
		return y;
		}

	public void setY(int y)
		{
		this.y = y;
		fireUpdate();
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

	public int getTileX()
		{
		return tileX;
		}

	public void setTileX(int tileX)
		{
		this.tileX = tileX;
		fireUpdate();
		}

	public int getTileY()
		{
		return tileY;
		}

	public void setTileY(int tileY)
		{
		this.tileY = tileY;
		fireUpdate();
		}

	public int getWidth()
		{
		return width;
		}

	public void setWidth(int width)
		{
		this.width = width;
		fireUpdate();
		}

	public int getHeight()
		{
		return height;
		}

	public void setHeight(int height)
		{
		this.height = height;
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
