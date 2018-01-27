/*
 * Copyright (C) 2010 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

public class Trigger
	{
	public String name = ""; //$NON-NLS-1$
	public String condition = ""; //$NON-NLS-1$
	public int checkStep = Event.EV_STEP_NORMAL;
	public String constant = ""; //$NON-NLS-1$

	public Trigger copy()
		{
		Trigger copy = new Trigger();
		copy.name = name;
		copy.condition = condition;
		copy.checkStep = checkStep;
		copy.constant = constant;
		return copy;
		}
	}
