package resourcesRes;

import mainRes.Prefs;
import fileRes.ResourceList;

public class Script extends Resource
	{
	public String ScriptStr = "";

	public Script()
		{
		name = Prefs.prefixes[Resource.SCRIPT];
		}

	public Script copy(boolean update, ResourceList src)
		{
		Script scr = new Script();
		scr.ScriptStr = ScriptStr;
		if (update)
			{
			scr.Id.value = ++src.LastId;
			scr.name = Prefs.prefixes[Resource.SCRIPT] + src.LastId;
			src.add(scr);
			}
		else
			{
			scr.Id = Id;
			scr.name = name;
			}
		return scr;
		}
	}