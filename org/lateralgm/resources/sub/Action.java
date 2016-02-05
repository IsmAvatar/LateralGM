/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.util.AbstractList;
import java.util.List;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.main.Util.InherentlyUnique;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibArgument;

public class Action implements UpdateListener,InherentlyUnique<Action>
	{
	public static final byte ACT_NORMAL = 0;
	public static final byte ACT_BEGIN = 1;
	public static final byte ACT_END = 2;
	public static final byte ACT_ELSE = 3;
	public static final byte ACT_EXIT = 4;
	public static final byte ACT_REPEAT = 5;
	public static final byte ACT_VARIABLE = 6;
	public static final byte ACT_CODE = 7;
	public static final byte ACT_PLACEHOLDER = 8;
	public static final byte ACT_SEPARATOR = 9;
	public static final byte ACT_LABEL = 10;

	public static final byte EXEC_NONE = 0;
	public static final byte EXEC_FUNCTION = 1;
	public static final byte EXEC_CODE = 2;

	/**
	 * If this Action was loaded from file, libAction is non-null.<br>
	 * To determine if this is an unknown libAction, parent == null
	 */
	private LibAction libAction;

	// The actual Action properties
	private boolean relative = false;
	private boolean not = false;
	private ResourceReference<GmObject> appliesTo = GmObject.OBJECT_SELF;

	private ArgumentList arguments;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	public Action(LibAction la, Argument[] args)
		{
		libAction = la;
		if (la == null) return;
		if (args == null)
			{
			args = new Argument[la.libArguments.length];
			for (int i = 0; i < args.length; i++)
				{
				LibArgument arg = la.libArguments[i];
				args[i] = new Argument(arg.kind,arg.defaultVal,null);
				}
			}
		arguments = new ArgumentList(args);
		}

	public Action(LibAction la)
		{
		this(la,null);
		}

	public Action copy()
		{
		Argument[] args = arguments.toArray(new Argument[arguments.size()]);
		for (int l = 0; l < args.length; l++)
			if (args[l] != null) args[l] = new Argument(args[l].kind,args[l].getVal(),args[l].getRes());
		Action act = new Action(libAction,args);
		act.relative = relative;
		act.not = not;
		act.appliesTo = appliesTo;
		return act;
		}

	protected void fireUpdate()
		{
		updateTrigger.fire();
		}

	public LibAction getLibAction()
		{
		return libAction;
		}

	public boolean isRelative()
		{
		return relative;
		}

	public void setRelative(boolean relative)
		{
		this.relative = relative;
		fireUpdate();
		}

	public boolean isNot()
		{
		return not;
		}

	public void setNot(boolean not)
		{
		this.not = not;
		fireUpdate();
		}

	public ResourceReference<GmObject> getAppliesTo()
		{
		return appliesTo;
		}

	public void setAppliesTo(ResourceReference<GmObject> appliesTo)
		{
		this.appliesTo = appliesTo;
		fireUpdate();
		}

	public List<Argument> getArguments()
		{
		return arguments;
		}

	public void setArguments(Argument[] arguments)
		{
		this.arguments = new ArgumentList(arguments);
		fireUpdate();
		}

	public class ArgumentList extends AbstractList<Argument>
		{
		private final Argument[] args;

		public ArgumentList(Argument[] args)
			{
			this.args = args;
			for (Argument a : args)
				{
				if (a != null) a.updateSource.addListener(Action.this);
				}
			}

		public Argument get(int index)
			{
			return args[index];
			}

		public Argument set(int index, Argument element)
			{
			Argument oa = args[index];
			oa.updateSource.addListener(Action.this);
			args[index] = element;
			element.updateSource.addListener(Action.this);
			Action.this.fireUpdate();
			return oa;
			}

		public int size()
			{
			return args.length;
			}
		}

	public void updated(UpdateEvent e)
		{
		fireUpdate();
		}

	public boolean isEqual(Action other)
		{
		if (this == other) return true;
		if (other == null) return false;
		if (appliesTo == null)
			{
			if (other.appliesTo != null) return false;
			}
		else if (!appliesTo.equals(other.appliesTo)) return false;
		if (arguments == null)
			{
			if (other.arguments != null) return false;
			}
		else if (!arguments.equals(other.arguments)) return false;
		if (libAction == null)
			{
			if (other.libAction != null) return false;
			}
		else if (!libAction.equals(other.libAction)) return false;
		return (not == other.not && relative == other.relative);
		}
	}
