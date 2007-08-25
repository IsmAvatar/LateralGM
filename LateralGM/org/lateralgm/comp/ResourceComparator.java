/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 *  
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package org.lateralgm.comp;

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
		addExclusions(Resource.class,"changeEvent","listenerList");
		}
	}
