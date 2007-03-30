package resourcesRes;

import java.util.ArrayList;

import resourcesRes.subRes.Point;

public class Path extends Resource
	{
	public boolean Smooth = false;
	public boolean Closed = true;
	public int Precision = 4;
	public ResId BackgroundRoom = null;
	public int SnapX = 16;
	public int SnapY = 16;
	private ArrayList<Point> Points = new ArrayList<Point>();

	public int NoPoints()
		{
		return Points.size();
		}

	public Point addPoint()
		{
		Point point = new Point();
		Points.add(point);
		return point;
		}

	public Point getPoint(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoPoints()) return Points.get(ListIndex);
		return null;
		}

	public void removePoint(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoPoints()) Points.remove(ListIndex);
		}

	public void clearPoints()
		{
		Points.clear();
		}
	}