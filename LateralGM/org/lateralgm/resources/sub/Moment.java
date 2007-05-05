/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.util.ArrayList;

import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;

public class Moment
	{
	private ArrayList<Action> Actions = new ArrayList<Action>();
	public int stepNo = 0;

	public int NoActions()
		{
		return Actions.size();
		}

	public Action addAction()
		{
		Action act = new Action();
		Actions.add(act);
		return act;
		}

	public Action addAction(int LibId, int LibActionId)// adds an action set to the properties of given
	// LibAction
		{
		Action act = new Action();
		LibAction lact = LibManager.getLibAction(LibId,LibActionId);
		if (lact != null)
			{
			act.LibActionId = LibActionId;
			act.LibraryId = LibId;
			act.ActionKind = lact.ActionKind;
			act.Question = lact.Question;
			act.CanApplyTo = lact.CanApplyTo;
			act.AllowRelative = lact.AllowRelative;
			act.ExecType = lact.ExecType;
			act.ExecFunction = lact.ExecFunction;
			act.ExecCode = lact.ExecCode;
			act.NoArguments = lact.NoLibArguments;
			for (int i = 0; i < lact.NoLibArguments; i++)
				{
				act.Arguments[i].Kind = lact.LibArguments[i].Kind;
				switch (act.Arguments[i].Kind)
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
						act.Arguments[i].Res = null;
						break;
					default:
						act.Arguments[i].Val = lact.LibArguments[i].DefaultVal;
						break;
					}
				}
			}
		Actions.add(act);
		return act;
		}

	public Action getAction(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoActions()) return Actions.get(ListIndex);
		return null;
		}

	public void removeAction(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoActions()) Actions.remove(ListIndex);
		}

	public void clearActions()
		{
		Actions.clear();
		}
	}