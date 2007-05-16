/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.library.LibAction;
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


	public LibAction libAction;

	// The actual Action properties
	public boolean relative = false;
	public boolean not = false;
	public ResId appliesTo = GmObject.OBJECT_SELF;

	public Argument[] arguments;
	}