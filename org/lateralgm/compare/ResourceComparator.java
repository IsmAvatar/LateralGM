/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package org.lateralgm.compare;

import org.lateralgm.resources.Resource;

/**
 * A convenience class that comes pre-setup to ignore certain fields of Resource.
 */
public class ResourceComparator extends LenientNumberComparator
	{
	public ResourceComparator()
		{
		super(new SimpleCasesComparator(new CollectionComparator(new MapComparator(
				new ObjectComparator(null)))));
		addExclusions(Resource.class,"updateTrigger","updateSource","node");
		}
	}
