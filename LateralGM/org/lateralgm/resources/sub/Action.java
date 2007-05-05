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
	public int LibraryId = 1;
	public int LibActionId = 101;
	public byte ActionKind = ACT_NORMAL;
	public boolean AllowRelative = false;
	public boolean Question = false;
	public boolean CanApplyTo = false;
	public byte ExecType = EXEC_FUNCTION;
	public String ExecFunction = "";
	public String ExecCode = "";

	// The actual Action properties
	public boolean Relative = false;
	public boolean Not = false;
	public ResId AppliesTo = GmObject.OBJECT_SELF;

	public int NoArguments = 0;
	public Argument[] Arguments = new Argument[6];

	public Action()
		{
		for (int j = 0; j < 6; j++)
			{
			Arguments[j] = new Argument();
			}
		}
	}