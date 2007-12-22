/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.lang.ref.WeakReference;

import org.lateralgm.resources.GmObject;

public class Instance
	{
	private static final long serialVersionUID = 1L;

	public int x = 0;
	public int y = 0;
	public WeakReference<GmObject> gmObjectId = null;
	public int instanceId = 0;
	public String creationCode = "";
	public boolean locked = false;
	}
