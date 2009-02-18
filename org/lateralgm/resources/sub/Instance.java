/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.awt.Point;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;

public class Instance implements UpdateListener
	{
	private static final long serialVersionUID = 1L;

	private Point pos;
	private ResourceReference<GmObject> object = null;
	public int instanceId = 0;
	private String creationCode = "";
	public boolean locked = false;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	protected void fireUpdate()
		{
		updateTrigger.fire();
		}

	public Point getPosition()
		{
		return pos;
		}

	public void setPosition(Point pos)
		{
		this.pos = pos;
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

	public ResourceReference<GmObject> getObject()
		{
		return object;
		}

	public void setObject(ResourceReference<GmObject> object)
		{
		if (this.object != null) this.object.updateSource.removeListener(this);
		this.object = object;
		if (object != null) object.updateSource.addListener(this);
		fireUpdate();
		}

	public void updated(UpdateEvent e)
		{
		updateTrigger.fire(e);
		}
	}
