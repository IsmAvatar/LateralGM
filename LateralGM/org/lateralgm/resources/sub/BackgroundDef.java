/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.lang.ref.WeakReference;

import org.lateralgm.resources.Background;

public class BackgroundDef
	{
	public boolean visible = false;
	public boolean foreground = false;
	public WeakReference<Background> backgroundId = null;
	public int x = 0;
	public int y = 0;
	public boolean tileHoriz = true;
	public boolean tileVert = true;
	public int horizSpeed = 0;
	public int vertSpeed = 0;
	public boolean stretch = false;
	}
