package resourcesRes;

import fileRes.ResourceList;
import mainRes.Prefs;

public class Script extends Resource
	{
	public String ScriptStr = "";

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
			scr.Id = scr.Id;
			scr.name = scr.name;
			}
		return scr;
		}
	}