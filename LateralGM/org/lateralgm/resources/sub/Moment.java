/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

public class Moment extends ActionContainer
	{
	public int stepNo = 0;

	public Moment copy()
		{
		Moment mom2 = new Moment();
		mom2.stepNo = stepNo;
		for (Action act : actions)
			{
			Action act2 = mom2.addAction();
			act2.libAction = act.libAction;
			act2.relative = act.relative;
			act2.not = act.not;
			act2.appliesTo = act.appliesTo;
			act2.arguments = new Argument[act.arguments.length];
			for (int k = 0; k < act.arguments.length; k++)
				act2.arguments[k] = new Argument(act.arguments[k].kind,act.arguments[k].val,
						act.arguments[k].res);
			}
		return mom2;
		}

	public String toString()
		{
		return "Step " + stepNo;
		}
	}
