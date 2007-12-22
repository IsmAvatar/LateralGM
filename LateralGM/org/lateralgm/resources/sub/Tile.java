/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.lang.ref.WeakReference;

import org.lateralgm.resources.Background;

public class Tile
	{
	public int x = 0;
	public int y = 0;
	public WeakReference<Background> backgroundId = null;
	public int tileX = 0;
	public int tileY = 0;
	public int width = 16;
	public int height = 16;
	public int depth = 0;
	public int tileId = 0;
	public boolean locked = false;
	}
