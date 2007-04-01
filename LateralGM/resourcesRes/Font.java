package resourcesRes;

import mainRes.Prefs;
import fileRes.ResourceList;

public class Font extends Resource
	{
	// Fonts may be a problem as they are OS dependent
	public String FontName = "Arial";
	public int Size = 12;
	public boolean Bold = false;
	public boolean Italic = false;
	public int CharRangeMin = 32;
	public int CharRangeMax = 127;

	public Font copy(boolean update, ResourceList src)
		{
		Font font = new Font();
		font.FontName = FontName;
		font.Size = Size;
		font.Bold = Bold;
		font.Italic = Italic;
		font.CharRangeMin = CharRangeMin;
		font.CharRangeMax = CharRangeMax;
		if (update)
			{
			font.Id.value = ++src.LastId;
			font.name = Prefs.prefixes[Resource.FONT] + src.LastId;
			src.add(font);
			}
		else
			{
			font.Id = Id;
			font.name = name;
			}
		return font;
		}
	}