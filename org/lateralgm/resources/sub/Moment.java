/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import org.lateralgm.main.Util;
import org.lateralgm.main.Util.InherentlyUnique;
import org.lateralgm.messages.Messages;

public class Moment extends ActionContainer implements Comparable<Object>,InherentlyUnique<Moment>
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
		return Messages.getString("Moment.STEP") + " " + stepNo;
		}

	public boolean isEqual(Moment other)
		{
		if (this == other) return true;
		if (other == null || stepNo != other.stepNo) return false;
		return Util.areInherentlyUniquesEqual(actions,other.actions);
		}
	}
