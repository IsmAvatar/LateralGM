/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

public class Constant implements Comparable<Constant>
	{
	public String name = "";
	public String value = "";

	public Constant copy()
		{
		Constant copy = new Constant();
		copy.name = name;
		copy.value = value;
		return copy;
		}

	public boolean equals(Object c)
		{
		if (c instanceof Constant)
			return ((Constant) c).name.equals(name);
		else
			return false;
		}
	
	public int hashCode()
		{
		return super.hashCode();
		}

	public int compareTo(Constant c)
		{
		return name.compareTo(c.name);
		}
	}
