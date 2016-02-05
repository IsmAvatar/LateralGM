/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.library;

import java.util.ArrayList;

public class Library
	{
	public int id = 0;
	public String tabCaption = "";
	public boolean advanced = false;
	public ArrayList<LibAction> libActions = new ArrayList<LibAction>();

	public LibAction addLibAction()
		{
		LibAction act = new LibAction();
		libActions.add(act);
		return act;
		}

	public LibAction getLibAction(int id)
		{
		for (LibAction act : libActions)
			if (act.id == id) return act;
		return null;
		}
	}
