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
import org.lateralgm.resources.GmObject;

public class Instance
	{
	private static final long serialVersionUID = 1L;

	private int x = 0;
	private int y = 0;
	public WeakReference<GmObject> gmObjectId = null;
	public int instanceId = 0;
	private String creationCode = "";
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

	public String getCreationCode()
		{
		return creationCode;
		}

	public void setCreationCode(String creationCode)
		{
		this.creationCode = creationCode;
		fireUpdate();
		}
	}
