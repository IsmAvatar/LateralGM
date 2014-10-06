package org.lateralgm.subframes;

import java.util.ArrayList;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.resources.Resource;

public final class SubframeInformer
	{
	private static ArrayList<SubframeListener> listeners = new ArrayList<SubframeListener>();

	private SubframeInformer()
		{
		}

	public static void addSubframeListener(SubframeListener list)
		{
		listeners.add(list);
		}

	public static void removeSubframeListener(SubframeListener list)
		{
		listeners.remove(list);
		}

	public static boolean fireSubframeRequest(Resource<?,?> res, ResNode node)
		{
		for (SubframeListener sl : listeners)
			if (sl.subframeRequested(res,node)) return true;
		return false;
		}

	public static void fireSubframeAppear(MDIFrame source, boolean wasVisible)
		{
		for (SubframeListener sl : listeners)
			sl.subframeAppeared(source,wasVisible);
		}

	public static interface SubframeListener
		{
		/**
		 * @return Whether this event is consumed, thus overriding the default resource editor
		 */
		boolean subframeRequested(Resource<?,?> res, ResNode node);

		void subframeAppeared(MDIFrame source, boolean wasVisible);
		}
	}
