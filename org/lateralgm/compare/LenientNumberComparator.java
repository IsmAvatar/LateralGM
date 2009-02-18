/* 
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by Clam <clamisgood@gmail.com>
 *  
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.compare;

import java.util.Set;
import java.util.Stack;

/**
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class LenientNumberComparator extends ReflectionComparator
	{
	public LenientNumberComparator(ReflectionComparator chainedComparator)
		{
		super(chainedComparator);
		}

	public boolean canHandle(Object left, Object right)
		{
		return (left != null && right != null) && (left instanceof Character || left instanceof Number)
				&& (right instanceof Character || right instanceof Number);
		}

	protected Difference doGetDifference(Object left, Object right, Stack<String> fieldStack,
			Set<TraversedInstancePair> traversedInstancePairs)
		{
		// check if right and left have same number value (including NaN and Infinity)
		Double leftDouble = getDoubleValue(left);
		Double rightDouble = getDoubleValue(right);
		if (leftDouble.equals(rightDouble))
			{
			return null;
			}
		return new Difference("Different primitive values.",left,right,fieldStack);
		}

	/**
	 * Gets the double value for the given left Character or Number instance.
	 * 
	 * @param object the Character or Number, not null
	 * @return the value as a Double (this way NaN and infinity can be compared)
	 */
	private Double getDoubleValue(Object object)
		{
		if (object instanceof Number)
			{
			return ((Number) object).doubleValue();
			}
		return (double) ((Character) object).charValue();
		}
	}
