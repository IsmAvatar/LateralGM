/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.library;

import java.awt.image.BufferedImage;

import org.lateralgm.resources.sub.Action;

public class LibAction
	{
	public static final byte INTERFACE_NORMAL = 0;
	public static final byte INTERFACE_NONE = 1;
	public static final byte INTERFACE_ARROWS = 2;
	public static final byte INTERFACE_CODE = 3;
	public static final byte INTERFACE_TEXT = 4;

	public int id = 0;
	public int parentId = -1;//Preserves the id when library is unknown
	public Library parent = null;
	public BufferedImage actImage;
	public boolean hidden = false;
	public boolean advanced = false;
	public boolean registeredOnly = false;
	public String description = "";
	public String listText = "";
	public String hintText = "";
	public byte actionKind = Action.ACT_NORMAL;
	public byte interfaceKind = INTERFACE_NORMAL;
	public boolean question = false;
	public boolean canApplyTo = false;
	public boolean allowRelative = false;
	public byte execType = Action.EXEC_FUNCTION;
	public String execFunction = "";
	public String execCode = "";
	public LibArgument[] libArguments;
	}