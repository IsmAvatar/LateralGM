package resourcesRes;

import java.awt.image.BufferedImage;

import mainRes.Prefs;
import fileRes.ResourceList;

public class Background extends Resource
	{
	public int Width = 0;
	public int Height = 0;
	public boolean Transparent = false;
	public boolean SmoothEdges = false;
	public boolean Preload = false;
	public boolean UseAsTileSet = false;
	public int TileWidth = 16;
	public int TileHeight = 16;
	public int HorizOffset = 0;
	public int VertOffset = 0;
	public int HorizSep = 0;
	public int VertSep = 0;
	public BufferedImage BackgroundImage = null;

	public BufferedImage copyBackgroundImage()
		{
		if (BackgroundImage != null)
			{
			BufferedImage bf = BackgroundImage;
			BufferedImage bf2 = new BufferedImage(bf.getWidth(),bf.getHeight(),bf.getType());
			bf2.setData(bf.getData());
			return bf2;
			}
		return null;
		}

	public Background copy(boolean update, ResourceList src)
		{
		Background back = new Background();
		back.Width = Width;
		back.Height = Height;
		back.Transparent = Transparent;
		back.SmoothEdges = SmoothEdges;
		back.Preload = Preload;
		back.UseAsTileSet = UseAsTileSet;
		back.TileWidth = TileWidth;
		back.TileHeight = TileHeight;
		back.HorizOffset = HorizOffset;
		back.VertOffset = VertOffset;
		back.HorizSep = HorizSep;
		back.VertSep = VertSep;
		back.BackgroundImage = copyBackgroundImage();
		if (update)
			{
			back.Id.value = ++src.LastId;
			back.name = Prefs.prefixes[Resource.BACKGROUND] + src.LastId;
			src.add(back);
			}
		else
			{
			back.Id = Id;
			back.name = name;
			}
		return back;
		}
	}