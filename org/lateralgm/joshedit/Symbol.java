/* Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

public class Symbol
{
	public static final Symbol LIT_NUMBER = new Symbol("0");
	public static final Symbol LIT_STR_SINGLE = new Symbol("'");
	public static final Symbol LIT_STR_DOUBLE = new Symbol("\"");

	public static final Symbol COM_LINE = new Symbol("//");
	public static final Symbol COM_SPAN = new Symbol("/*");
	public static final Symbol COM_DOC = new Symbol("/**");

	public static final Symbol IDENT_VAR = new Symbol("myVar");
	public static final Symbol IDENT_FUNC = new Symbol("myFunc(");

	public static final Symbol KEY_BREAK = new Symbol("break;");
	public static final Symbol KEY_CONTINUE = new Symbol("continue;");

	public static final Symbol OP_EQ = new Symbol("=");
	public static final Symbol OP_EQEQ = new Symbol("==");
	public static final Symbol OP_PLUS = new Symbol("+");

	String name;
	Symbol type;
	Object val;

	protected Symbol()
	{
	}

	public Symbol(String name)
	{
		this.name = name;
	}

	public Symbol(Symbol type, Object val)
	{
		this.name = type.name;
		this.type = type;
		this.val = val;
	}

	@Override
	public String toString()
	{
		return name + " " + (val == null ? "" : val);
	}
}
