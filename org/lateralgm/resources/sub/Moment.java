/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

public class Moment extends ActionContainer implements Comparable<Object>
	{
	public int stepNo = 0;

	public Moment copy()
		{
		Moment mom2 = new Moment();
		mom2.stepNo = stepNo;
		for (Action act : actions)
			mom2.actions.add(act.copy());
		return mom2;
		}

	public int compareTo(Object o)
		{
		if (o instanceof Moment) return stepNo - ((Moment) o).stepNo;
		if (o instanceof Integer) return stepNo - (Integer) o;
		throw new ClassCastException();
		}

	public String toString()
		{
		return "Step " + stepNo;
		}
	}
