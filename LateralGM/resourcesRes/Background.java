package resourcesRes;

import java.awt.image.BufferedImage;

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
	}