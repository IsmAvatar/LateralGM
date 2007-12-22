/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.lang.ref.WeakReference;

import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibArgument;

public class Action
	{
	public static final byte ACT_NORMAL = 0;
	public static final byte ACT_BEGIN = 1;
	public static final byte ACT_END = 2;
	public static final byte ACT_ELSE = 3;
	public static final byte ACT_EXIT = 4;
	public static final byte ACT_REPEAT = 5;
	public static final byte ACT_VARIABLE = 6;
	public static final byte ACT_CODE = 7;
	public static final byte ACT_PLACEHOLDER = 8;
	public static final byte ACT_SEPARATOR = 9;
	public static final byte ACT_LABEL = 10;

	public static final byte EXEC_NONE = 0;
	public static final byte EXEC_FUNCTION = 1;
	public static final byte EXEC_CODE = 2;

	/**
	 * If this Action was loaded from file, libAction is non-null.<br>
	 * To determine if this is an unknown libAction, parent == null
	 */
	public LibAction libAction;

	// The actual Action properties
	public boolean relative = false;
	public boolean not = false;
	public WeakReference<GmObject> appliesTo = GmObject.OBJECT_SELF;

	public Argument[] arguments;

	public Action(LibAction la)
		{
		libAction = la;
		if (la == null) return;
		arguments = new Argument[la.libArguments.length];
		for (int i = 0; i < la.libArguments.length; i++)
			{
			LibArgument arg = la.libArguments[i];
			arguments[i] = new Argument(arg.kind,arg.defaultVal,null);
			}
		}

	public Action copy()
		{
		Action act = new Action(libAction);
		act.relative = relative;
		act.not = not;
		act.appliesTo = appliesTo;
		act.arguments = new Argument[arguments.length];
		for (int l = 0; l < arguments.length; l++)
			act.arguments[l] = new Argument(arguments[l].kind,arguments[l].val,arguments[l].res);
		return act;
		}
	}
