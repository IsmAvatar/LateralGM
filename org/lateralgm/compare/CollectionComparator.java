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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class CollectionComparator extends ReflectionComparator
	{
	public CollectionComparator(ReflectionComparator chainedComparator)
		{
		super(chainedComparator);
		}

	public boolean canHandle(Object left, Object right)
		{
		return (left != null && right != null)
				&& (left.getClass().isArray() || left instanceof Collection<?>)
				&& (right.getClass().isArray() || right instanceof Collection<?>);
		}

	protected Difference doGetDifference(Object left, Object right, Stack<String> fieldStack,
			Set<TraversedInstancePair> traversedInstancePairs)
		{
		// Convert to list and compare as collection
		Collection<?> leftCollection = convertToCollection(left);
		Collection<?> rightCollection = convertToCollection(right);

		if (leftCollection.size() != rightCollection.size())
			{
			return new Difference("Different array/collection sizes. Left size: " + leftCollection.size()
					+ ", right size: " + rightCollection.size(),left,right,fieldStack);
			}

		int i = 0;
		Iterator<?> lhsIterator = leftCollection.iterator();
		Iterator<?> rhsIterator = rightCollection.iterator();
		while (lhsIterator.hasNext() && rhsIterator.hasNext())
			{
			fieldStack.push("" + i++);
			Difference difference = rootComparator.getDifference(lhsIterator.next(),rhsIterator.next(),
					fieldStack,traversedInstancePairs);
			if (difference != null)
				{
				return difference;
				}
			fieldStack.pop();
			}
		return null;
		}

	/**
	 * Converts the given array or collection object (possibly primitive array) to type Collection
	 * 
	 * @param object the array or collection
	 * @return the object collection
	 */
	protected Collection<?> convertToCollection(Object object)
		{
		if (object instanceof Collection<?>)
			{
			return (Collection<?>) object;
			}

		// If needed convert primitive array to object array
		Object[] objectArray = convertToObjectArray(object);

		// Convert array to collection
		return Arrays.asList(objectArray);
		}

	/**
	 * Converts the given array object (possibly primitive array) to type Object[]
	 * 
	 * @param object the array
	 * @return the object array
	 */
	protected Object[] convertToObjectArray(Object object)
		{
		return (Object[]) object;
		}
	}
