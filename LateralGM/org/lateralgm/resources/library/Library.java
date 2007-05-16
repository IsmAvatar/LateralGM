/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
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

	private LibAction addLibAction()
		{
		LibAction act = new LibAction();
		libActions.add(act);
		return act;
		}

	public LibAction getLibAction(int id)
		{
		for (int i = 0; i < libActions.size(); i++)
			if (libActions.get(i).id == id)
				return libActions.get(i);
		return null;
		}
	}