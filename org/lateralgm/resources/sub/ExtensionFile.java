/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

public class ExtensionFile implements Comparable<ExtensionFile>
	{
	public String name = "";
	public String value = "";

	public ExtensionFile copy()
		{
		ExtensionFile copy = new ExtensionFile();
		copy.name = name;
		copy.value = value;
		return copy;
		}

	@Override
	public int hashCode()
		{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
		}

	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ExtensionFile)) return false;
		ExtensionFile other = (ExtensionFile) obj;
		if (name == null)
			{
			if (other.name != null) return false;
			}
		else if (!name.equals(other.name)) return false;
		return true;
		}

	public int compareTo(ExtensionFile c)
		{
		return name.compareTo(c.name);
		}
	}
