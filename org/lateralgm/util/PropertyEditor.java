/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.util;

public interface PropertyEditor<V>
	{
	<K extends Enum<K>>PropertyLink<K,V> getLink(PropertyMap<K> m, K k);
	}
