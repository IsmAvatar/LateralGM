package componentRes;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import mainRes.LGM;

public class GmTreeGraphics extends DefaultTreeCellRenderer
	{
	private static final long serialVersionUID = 1L;

	public Component getTreeCellRendererComponent(JTree tree,Object val,boolean sel,boolean exp,boolean leaf,
			int row,boolean focus)
		{
		super.getTreeCellRendererComponent(tree,val,sel,exp,leaf,row,focus);
		ResNode node = (ResNode)val;
		if (node.status == ResNode.STATUS_SECONDARY)
			{
			setIcon(LGM.findIcon(LGM.kinds[node.kind] + ".png"));
			}
		else
			{
			if (exp && !node.isLeaf())
				setIcon(LGM.findIcon("group_open.png"));
			else
				setIcon(LGM.findIcon("group.png"));
			}
		return this;
		}
	}