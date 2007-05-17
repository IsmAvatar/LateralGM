/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.util.ArrayList;

import org.lateralgm.resources.library.LibAction;

public abstract class ActionContainer
	{
	private ArrayList<Action> actions = new ArrayList<Action>();

	public int noActions()
		{
		return actions.size();
		}

	public Action addAction()
		{
		Action act = new Action();
		actions.add(act);
		return act;
		}

	// adds an action set to the properties of given LibAction
	public Action addAction(LibAction libAction)
		{
		Action act = new Action();
		for (int i = 0; i < libAction.libArguments.length; i++)
			{
			act.arguments[i].kind = libAction.libArguments[i].kind;
			switch (act.arguments[i].kind)
				{
				case Argument.ARG_SPRITE:
				case Argument.ARG_SOUND:
				case Argument.ARG_BACKGROUND:
				case Argument.ARG_PATH:
				case Argument.ARG_SCRIPT:
				case Argument.ARG_GMOBJECT:
				case Argument.ARG_ROOM:
				case Argument.ARG_FONT:
				case Argument.ARG_TIMELINE:
					act.arguments[i].res = null;
					break;
				default:
					act.arguments[i].val = libAction.libArguments[i].defaultVal;
					break;
				}
			}
		actions.add(act);
		return act;
		}

	public Action getAction(int listIndex)
		{
		if (listIndex >= 0 && listIndex < noActions()) return actions.get(listIndex);
		return null;
		}

	public void removeAction(int listIndex)
		{
		if (listIndex >= 0 && listIndex < noActions()) actions.remove(listIndex);
		}

	public void clearActions()
		{
		actions.clear();
		}
	}
