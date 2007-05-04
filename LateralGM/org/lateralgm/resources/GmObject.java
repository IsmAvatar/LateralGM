package org.lateralgm.resources;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;


public class GmObject extends Resource
	{
	public static final ResId OBJECT_SELF = new ResId(-1);
	public static final ResId OBJECT_OTHER = new ResId(-2);

	public ResId Sprite = null;
	public boolean Solid = false;
	public boolean Visible = true;
	public int Depth = 0;
	public boolean Persistent = false;
	public ResId Parent = null;
	public ResId Mask = null;
	public MainEvent[] MainEvents = new MainEvent[11];

	public GmObject()
		{
		name = Prefs.prefixes[Resource.GMOBJECT];
		for (int j = 0; j < 11; j++)
			{
			MainEvents[j] = new MainEvent();
			}
		}

	public GmObject copy(boolean update, ResourceList src)
		{
		GmObject obj = new GmObject();
		obj.Sprite = Sprite;
		obj.Solid = Solid;
		obj.Visible = Visible;
		obj.Depth = Depth;
		obj.Persistent = Persistent;
		obj.Parent = Parent;
		obj.Mask = Mask;
		for (int i = 0; i < 11; i++)
			{
			MainEvent mev = MainEvents[i];
			MainEvent mev2 = obj.MainEvents[i];
			for (int j = 0; j < mev.NoEvents(); j++)
				{
				Event ev = mev.getEventList(j);
				Event ev2 = mev2.addEvent();
				ev2.Id = ev.Id;
				for (int k = 0; k < ev.NoActions(); k++)
					{
					Action act = ev.getAction(k);
					Action act2 = ev2.addAction();
					act2.LibraryId = act.LibraryId;
					act2.LibActionId = act.LibActionId;
					act2.ActionKind = act.ActionKind;
					act2.AllowRelative = act.AllowRelative;
					act2.Question = act.Question;
					act2.CanApplyTo = act.CanApplyTo;
					act2.ExecType = act.ExecType;
					act2.ExecFunction = act.ExecFunction;
					act2.ExecCode = act.ExecCode;
					act2.Relative = act.Relative;
					act2.Not = act.Not;
					act2.AppliesTo = act.AppliesTo;
					act2.NoArguments = act.NoArguments;
					for (int l = 0; l < act.NoArguments; l++)
						{
						act2.Arguments[k].Kind = act.Arguments[k].Kind;
						act2.Arguments[k].Res = act.Arguments[k].Res;
						act2.Arguments[k].Val = act.Arguments[k].Val;
						}
					}
				}
			}
		if (update)
			{
			obj.Id.value = ++src.LastId;
			obj.name = Prefs.prefixes[Resource.GMOBJECT] + src.LastId;
			src.add(obj);
			}
		else
			{
			obj.Id = Id;
			obj.name = name;
			}
		return obj;
		}
	}