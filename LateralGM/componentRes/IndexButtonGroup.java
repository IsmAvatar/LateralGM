package componentRes;

import java.awt.Container;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

public class IndexButtonGroup
	{
	private static final long serialVersionUID = 1L;
	private Hashtable<AbstractButton,Integer> map;
	private ButtonGroup g;

	public IndexButtonGroup()
		{
		g = new ButtonGroup();
		map = new Hashtable<AbstractButton,Integer>();
		}

	public void add(AbstractButton b, int value)
		{
		g.add(b);
		map.put(b,value);
		}

	public int getValue()
		{
		int value = 0;
		for (Map.Entry e : map.entrySet())
			{
			if (((AbstractButton)e.getKey()).isSelected()) value |= (Integer)e.getValue();
			}
		return value;
		}

	public void setValue(int value)
		{
		for (Map.Entry e : map.entrySet())
			{
			if (((Integer)e.getValue() & value) != 0) ((AbstractButton)e.getKey()).setSelected(true);
			}
		}
	
	public void populate(Container c)
		{
		for (Enumeration<AbstractButton> e = map.keys(); e.hasMoreElements();)
			{
			c.add(e.nextElement());
			}
		}
	}