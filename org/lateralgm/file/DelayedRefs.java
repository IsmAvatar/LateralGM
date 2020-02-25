package org.lateralgm.file;

import java.util.HashMap;
import java.util.Map;

import org.lateralgm.file.RefList;

public class DelayedRefs
	{
	private Map<Class<?>, RefList<?>> lists = new HashMap<>();
	
	public RefList getList(Class c)
		{
		RefList rl = lists.get(c);
		if (rl != null) return rl;
		rl = new RefList(c);
		lists.put(c,rl);
		return rl;
		}
	}
