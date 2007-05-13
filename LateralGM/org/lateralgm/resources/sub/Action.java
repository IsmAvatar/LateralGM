/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.library.Library;

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

	// LibAction properties for resave
	public Library library = null;
	public int libActionId = 101;
	public byte actionKind = ACT_NORMAL;
	public boolean allowRelative = false;
	public boolean question = false;
	public boolean canApplyTo = false;
	public byte execType = EXEC_FUNCTION;
	public String execFunction = "";
	public String execCode = "";

	// The actual Action properties
	public boolean relative = false;
	public boolean not = false;
	public ResId appliesTo = GmObject.OBJECT_SELF;

	public int noArguments = 0;
	public Argument[] arguments = new Argument[6];

	public Action()
		{
		for (int j = 0; j < 6; j++)
			{
			arguments[j] = new Argument();
			}
		}
	}