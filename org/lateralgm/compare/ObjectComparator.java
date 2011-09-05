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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

/**
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class ObjectComparator extends ReflectionComparator
	{
	public ObjectComparator(ReflectionComparator chainedComparator)
		{
		super(chainedComparator);
		}

	public boolean canHandle(Object left, Object right)
		{
		return left != null && right != null;
		}

	protected Difference doGetDifference(Object left, Object right, Stack<String> fieldStack,
			Set<TraversedInstancePair> traversedInstancePairs)
		{
		// check different class type
		Class<?> clazz = left.getClass();
		if (!clazz.equals(right.getClass()))
			{
			return new Difference("Different class types. Left: " + clazz + ", right: "
					+ right.getClass(),left,right,fieldStack);
			}

		// If an equals method is implemented, use it
		if (hasEqualsImpl(clazz))
			return left.equals(right) ? null : new Difference("inequality by use of equals method",left,
					right,fieldStack);

		// compare all fields of the object using reflection
		return compareFields(left,right,clazz,fieldStack,traversedInstancePairs);
		}

	/**
	 * Compares the values of all fields in the given objects by use of reflection.
	 * 
	 * @param left the left object for the comparison, not null
	 * @param right the right object for the comparison, not null
	 * @param clazz the type of both objects
	 * @param fieldStack the current field names
	 * @param traversedInstancePairs
	 * @return the difference, null if there is no difference
	 */
	protected Difference compareFields(Object left, Object right, Class<?> clazz,
			Stack<String> fieldStack, Set<TraversedInstancePair> traversedInstancePairs)
		{
		Field[] fields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(fields,true);
		traversedInstancePairs.add(new TraversedInstancePair(left,right));

		String[] excludes = exclusions.get(clazz);
		for (Field f : fields)
			{
			fieldStack.push(f.getName());

			// skip transient, static and excluded fields
			if (Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers())
					|| contains(excludes,f.getName()))
				{
				fieldStack.pop();
				continue;
				}
			try
				{

				// recursively check the value of the fields
				Difference difference = rootComparator.getDifference(f.get(left),f.get(right),fieldStack,
						traversedInstancePairs);
				if (difference != null)
					{
					return difference;
					}

				}
			catch (IllegalAccessException e)
				{
				// this can't happen. Would get a Security exception instead
				// throw a runtime exception in case the impossible happens.
				throw new InternalError("Unexpected IllegalAccessException");
				}
			catch (ClassCastException e)
				{
				System.out.println(f);
				throw e;
				}
			fieldStack.pop();
			}

		// compare fields declared in superclass
		Class<?> superclazz = clazz.getSuperclass();
		while (superclazz != null && !superclazz.getName().startsWith("java.lang"))
			{
			Difference dif = compareFields(left,right,superclazz,fieldStack,traversedInstancePairs);
			if (dif != null)
				{
				return dif;
				}
			superclazz = superclazz.getSuperclass();
			}
		return null;
		}

	private static boolean hasEqualsImpl(Class<?> clazz)
		{
		try
			{
			return clazz.getMethod("equals",Object.class).getDeclaringClass() != Object.class;
			}
		catch (NoSuchMethodException e)
			{
			e.printStackTrace();
			return false;
			}
		}

	public static boolean contains(Object[] dat, Object target)
		{
		if (dat == null) return false;
		for (Object o : dat)
			if (target.equals(o)) return true;
		return false;
		}

	protected HashMap<Class<?>,String[]> exclusions = new HashMap<Class<?>,String[]>();

	public void addExclusions(Class<?> clazz, String...fieldNames)
		{
		exclusions.put(clazz,fieldNames);
		}
	}
