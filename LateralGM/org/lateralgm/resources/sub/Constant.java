/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

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
		return (c instanceof Constant) ? ((Constant) c).name.equals(name) : false;
		}

	public int compareTo(Constant c)
		{
		return name.compareTo(c.name);
		}
	}
