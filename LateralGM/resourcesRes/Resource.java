package resourcesRes;

public abstract class Resource implements Comparable<Resource>
	{
	public static final byte SPRITE = 2;
	public static final byte SOUND = 3;
	public static final byte BACKGROUND = 6;
	public static final byte PATH = 8;
	public static final byte SCRIPT = 7;
	public static final byte FONT = 9;
	public static final byte TIMELINE = 12;
	public static final byte GMOBJECT = 1;
	public static final byte ROOM = 4;

	public static final byte GAMEINFO = 10;
	public static final byte GAMESETTINGS = 11;

	public ResId Id = new ResId(0);
	public String name = "";

	public int compareTo(Resource res)
		{
		if (res.Id.value > Id.value) return 1;
		return res.Id.value < Id.value ? -1 : 0;
		}
//	protected abstract Object clone();
	}