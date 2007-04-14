package fileRes;

import java.util.Collections;

import resourcesRes.ResId;
import resourcesRes.Resource;

import java.util.ArrayList;

public class ResourceList<R extends Resource>
	{
	private ArrayList<R> Resources = new ArrayList<R>();

	private Class type; // used as a workaround for add()

	ResourceList(Class type) // it's *YOUR* problem if this class doesn't extend Resource (you shouldn't really
	// need to construct a ResourceList manually anyway)
		{
		this.type = type;
		}

	public int LastId = -1;

	public int count()
		{
		return Resources.size();
		}

	public R add(R res)
		{
		Resources.add(res);
		res.Id.value = ++LastId;
		return res;
		}

	public R add()// Be careful when using this with rooms (they default to LGM.currentFile as their owner)
		{
		R res = null;
		try
			{
			res = (R) type.newInstance();
			res.Id.value = ++LastId;
			Resources.add(res);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		return res;
		}

	public R getUnsafe(int id)
		{
		for (int i = 0; i < Resources.size(); i++)
			{
			if (Resources.get(i).Id.value == id)
				{
				return Resources.get(i);
				}
			}
		return null;
		}

	public R get(ResId id)
		{
		int ListIndex = index(id);
		if (ListIndex != -1) return Resources.get(ListIndex);
		return null;
		}

	public R get(String Name)
		{
		int ListIndex = index(Name);
		if (ListIndex != -1) return Resources.get(ListIndex);
		return null;
		}

	public R getList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < Resources.size()) return Resources.get(ListIndex);
		return null;
		}

	public void remove(ResId id)
		{
		int ListIndex = index(id);
		if (ListIndex != -1) Resources.remove(ListIndex);
		}

	public void remove(String Name)
		{
		int ListIndex = index(Name);
		if (ListIndex != -1) Resources.remove(ListIndex);
		}

	public int index(ResId id)
		{
		for (int i = 0; i < Resources.size(); i++)
			{
			if (Resources.get(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int index(String Name)
		{
		for (int i = 0; i < Resources.size(); i++)
			{
			if (Resources.get(i).name.equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clear()
		{
		Resources.clear();
		}

	public void sort()
		{
		Collections.sort(Resources);
		}

	public R duplicate(ResId id, boolean update)
		{
		R res = get(id);
		R res2 = null;
		if (res != null) res2 = (R) res.copy(update,this);
		return res2;
		}

	public void replace(ResId srcId, R replacement)
		{
		int ind = index(srcId);
		if (replacement != null && ind >= 0)
			{
			Resources.set(ind,replacement);
			}
		}

	public void replace(int SrcIndex, R Replacement)
		{
		if (SrcIndex >= 0 && SrcIndex < Resources.size() && Replacement != null)
			Resources.set(SrcIndex,Replacement);
		}
	
	public void defragIds()
		{
		sort();
		for(int i=0;i<Resources.size();i++)
			{
			Resources.get(i).Id.value=i;
			}
		LastId=Resources.size()-1;
		}
	}