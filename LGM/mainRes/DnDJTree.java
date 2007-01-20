package mainRes;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.tree.*;


public class DnDJTree extends JTree 
		implements DragSourceListener, DropTargetListener, DragGestureListener {
		static DataFlavor localObjectFlavor;
		static {
			try {


		localObjectFlavor = 
			new DataFlavor (DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) { cnfe.printStackTrace(); }
		} 
		static DataFlavor[] supportedFlavors = { localObjectFlavor };
		DragSource dragSource;	
		DropTarget dropTarget; 
		TreeNode dropTargetNode = null; 
		TreeNode draggedNode = null;
		public DnDJTree () {   
			super();    
			setCellRenderer (new DnDTreeCellRenderer()); 
			setModel (new DefaultTreeModel(new DefaultMutableTreeNode("default")));      
			dragSource = new DragSource(); 
			DragGestureRecognizer dgr =
		dragSource.createDefaultDragGestureRecognizer (this,
					DnDConstants.ACTION_MOVE,
										this);
			dropTarget = new DropTarget (this, this);
		}
		


		// DragGestureListener
		public void dragGestureRecognized (DragGestureEvent dge) {
			System.out.println ("dragGestureRecognized");
			// find object at this x,y
			Point clickPoint = dge.getDragOrigin();
			TreePath path = getPathForLocation (clickPoint.x, clickPoint.y);
			if (path == null) {

		System.out.println ("not on a node");
		return;
			}
			draggedNode = (TreeNode) path.getLastPathComponent();
			Transferable trans = new RJLTransferable (draggedNode);
			dragSource.startDrag (dge,Cursor.getDefaultCursor(),
			
			trans, this);
			}
			// DragSourceListener events
			public void dragDropEnd (DragSourceDropEvent dsde) {

		System.out.println ("dragDropEnd()");
		dropTargetNode = null;
		draggedNode = null;
		repaint();

		}
		public void dragEnter (DragSourceDragEvent dsde) {}
		public void dragExit (DragSourceEvent dse) {}
		public void dragOver (DragSourceDragEvent dsde) {}
		public void dropActionChanged (DragSourceDragEvent dsde) {}
		// DropTargetListener events
		public void dragEnter (DropTargetDragEvent dtde) {	

			System.out.println ("dragEnter"); 
			
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);

			System.out.println ("accepted dragEnter");
		}
		public void dragExit (DropTargetEvent dte) {}
		public void dragOver (DropTargetDragEvent dtde) {

			// figure out which cell it's over, no drag to self
			Point dragPoint = dtde.getLocation();
			TreePath path = getPathForLocation (dragPoint.x, dragPoint.y);
			if (path == null)

			dropTargetNode = null; 
		else        
			dropTargetNode = (TreeNode) path.getLastPathComponent();

			repaint();
		}
		public void drop (DropTargetDropEvent dtde) {
			System.out.println ("drop()!");
			Point dropPoint = dtde.getLocation();
			// int index = locationToIndex (dropPoint);
			TreePath path = getPathForLocation (dropPoint.x, dropPoint.y);
			System.out.println ("drop path is " + path);
			boolean dropped = false;
			try {
		dtde.acceptDrop (DnDConstants.ACTION_MOVE);
		System.out.println ("accepted");
		Object droppedObject =

			dtde.getTransferable().getTransferData(localObjectFlavor); 
		MutableTreeNode droppedNode = null; 
		if (droppedObject instanceof MutableTreeNode) {
		// remove from old location
		droppedNode = (MutableTreeNode) droppedObject;
		((DefaultTreeModel)getModel()).
			removeNodeFromParent(droppedNode); }
			else {
		droppedNode = new DefaultMutableTreeNode (droppedObject); 
			} 
			// insert into spec'd path. if dropped into a parent 
			// make it last child of that parent 
			DefaultMutableTreeNode dropNode =
			(DefaultMutableTreeNode) path.getLastPathComponent();
            if (dropNode.isLeaf()) { 
		DefaultMutableTreeNode parent =
		(DefaultMutableTreeNode) dropNode.getParent(); 
			int index = parent.getIndex (dropNode);        
	((DefaultTreeModel)getModel()).insertNodeInto (droppedNode,
		parent, index); } 
	else {
	((DefaultTreeModel)getModel()).insertNodeInto 
		(droppedNode, dropNode,
          dropNode.getChildCount());
			}
	dropped = true;
	} catch (Exception e) {

	        e.printStackTrace();
	}
	dtde.dropComplete (dropped);
	}
	public void dropActionChanged (DropTargetDragEvent dtde) {}
	// main() method (unit test) goes here
	// inner classes go here }

	class RJLTransferable implements Transferable { 
	Object object; 
	public RJLTransferable (Object o) { 
		object = o; 
	} 
	public Object getTransferData(DataFlavor df) 
		throws UnsupportedFlavorException, IOException { 
		if (isDataFlavorSupported (df)) 
			return object; 
		else 
			throw new UnsupportedFlavorException(df); 
		} 
		public boolean isDataFlavorSupported (DataFlavor df) { 
			return (df.equals (localObjectFlavor)); 
		}
public DataFlavor[] getTransferDataFlavors () {
			return supportedFlavors;
		}	
	}

	class DnDTreeCellRenderer
	extends DefaultTreeCellRenderer {
	boolean isTargetNode;
	boolean isTargetNodeLeaf;
	boolean isLastItem;
	int	BOTTOM_PAD = 30;
	public DnDTreeCellRenderer() {
		super(); 
	} 
	public Component getTreeCellRendererComponent (JTree tree,
								Object value, 
								boolean isSelected, 
								boolean isExpanded, 
								boolean isLeaf, 
								int row, 
								boolean hasFocus) {
		isTargetNode = (value == dropTargetNode);
		isTargetNodeLeaf = (isTargetNode &&

			                ((TreeNode)value).isLeaf());
		// isLastItem = (index == list.getModel().getSize()-1); 
		boolean showSelected = isSelected &
				(dropTargetNode == null);
		return super.getTreeCellRendererComponent (tree, value, 
								isSelected, isExpanded, 													isLeaf, row, hasFocus);
	}
	

	public void paintComponent (Graphics g) {
		super.paintComponent(g);
		System.out.println("Draw");
		if (isTargetNode) {
			g.setColor(Color.cyan); 
			if (isTargetNodeLeaf) { 
               g.drawLine (0, 0, getSize().width, 0); 
			} else {
                g.drawRect (0, 0, getSize().width-1, getSize().height-1); 
			} 
		} 
	} 
}
}

