/*
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Stack;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.tree.TreeNode;

import org.lateralgm.components.ActionList;
import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.subframes.GmObjectFrame.EventGroupNode;
import org.lateralgm.subframes.GmObjectFrame.EventInstanceNode;
import org.lateralgm.subframes.GmObjectFrame.EventTree;

import com.sun.xml.internal.txw2.Document;

public class GmObjectInfoFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	protected JSpinner sSizes;
	protected JEditorPane editor;
	protected Color fgColor;
	private RTFEditorKit rtf = new RTFEditorKit();
	private CustomFileChooser fc;
	private GmObjectFrame gmObjFrame;

	public JToolBar makeToolbar()
	{
		JToolBar tb = new JToolBar();
		tb.add(addToolbarItem("GmObjectInfoFrame.CONFIRM"));
		tb.addSeparator();
		tb.add(addToolbarItem("GmObjectInfoFrame.FILESAVE"));
		tb.add(addToolbarItem("GmObjectInfoFrame.PRINT"));
		tb.addSeparator();
		tb.add(addToolbarItem("GmObjectInfoFrame.COPY"));
		return tb;
	}

public JPopupMenu makeContextMenu()
	{
  // build popup menu
  final JPopupMenu popup = new JPopupMenu();
  JMenuItem item;
  
	item = addItem("GmObjectInfoFrame.COPY"); //$NON-NLS-1$
	item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,KeyEvent.CTRL_DOWN_MASK));
	popup.add(item);
	popup.addSeparator();
	item = addItem("GmObjectInfoFrame.SELECTALL"); //$NON-NLS-1$
	item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,KeyEvent.CTRL_DOWN_MASK));
	popup.add(item);
	
  editor.setComponentPopupMenu(popup);
	editor.addMouseListener(new MouseAdapter() {

  	@Override
  	public void mousePressed(MouseEvent e) {
      showPopup(e);
  	}

  	@Override
  	public void mouseReleased(MouseEvent e) {
      showPopup(e);
  	}

  	private void showPopup(MouseEvent e) {
  		if (e.isPopupTrigger()) {
  			popup.show(e.getComponent(), e.getX(), e.getY());
  		}
  	}
	});
	
	return popup;
}

public JMenuItem addItem(String key)
{
	JMenuItem item = new JMenuItem(Messages.getString(key));
	item.setIcon(LGM.getIconForKey(key));
	item.setActionCommand(key);
	item.addActionListener(this);
	return item;
}

public JButton addToolbarItem(String key)
{
	JButton item = new JButton();
	item.setToolTipText(Messages.getString(key));
	item.setIcon(LGM.getIconForKey(key));
	item.setActionCommand(key);
	item.addActionListener(this);
	return item;
}
	public String loopActionsToString(ArrayList<Action> list)
	{
	  String info = "";
	  int lms = list.size();
	  for (int i = 0; i < lms; i++)
	  {
		  Action a = list.get(i);
		
		  LibAction la = a.getLibAction();
		
		  info += "\n" + i + " " + la.description;
	  }
	  
	  return info;
	}
	
	public void updateObjectInfo()
	{
	  String objInfo = "----- Properties -----\n\n";
	  ResourceReference<?> res;
	  objInfo += "Name: " + gmObjFrame.name.getText() + "\n";
	  
	  res = gmObjFrame.parent.getSelected();
	  objInfo += "Parent: ";
	  if (res != null) {
	    objInfo += res.get().getName();
	  } else {
  	  objInfo += "none";
    }
    objInfo += "\n";
    
	  res = gmObjFrame.sprite.getSelected();
	  objInfo += "Sprite: ";
	  if (res != null) {
	    objInfo += res.get().getName();
	  } else {
  	  objInfo += "none";
    }
    objInfo += "\n";
    
	  res = gmObjFrame.mask.getSelected();
	  objInfo += "Mask: ";
	  if (res != null) {
	    objInfo += res.get().getName();
	  } else {
  	  objInfo += "same as sprite";
    }
    objInfo += "\n";

	  objInfo += "Visible: " + gmObjFrame.visible.isSelected() + "\n";
	  objInfo += "Solid: " + gmObjFrame.solid.isSelected() + "\n";
	  objInfo += "Depth: " + gmObjFrame.depth.getValue().toString() + "\n";
	  objInfo += "Persistent: " + gmObjFrame.persistent.isSelected() + "\n";
	  
	  objInfo += "\n---- Events -----";
	  
	  // iterate events and get each ones model
		EventGroupNode e = gmObjFrame.rootEvent;
		gmObjFrame.saveEvents();
		EventInstanceNode etn, etnc;
		ActionList list;
		for (int i=0; i<e.getChildCount(); i++)
		{
		  TreeNode tn = (TreeNode) e.getChildAt(i);	
		  int etncc = tn.getChildCount();
		  if (etncc > 0)
		  {
			  for (int ii=0; ii<etncc; ii++)
				{
				  etnc = (EventInstanceNode) tn.getChildAt(ii);	
					objInfo += "\n\n  " + etnc.toString() + ":";
					//objInfo += loopActionsToString(etnc.getUserObject().actions);
			    if (etnc.getUserObject().actions.size() > 0) {
		        objInfo += loopActionsToString(etnc.getUserObject().actions);
		      } else {
		        objInfo += "\n0 empty";
		      }
				}
		  } else {
		    etn = (EventInstanceNode) e.getChildAt(i);	
			  objInfo += "\n\n  " + etn.toString();
		    objInfo += ":"; 
		    if (etn.getUserObject().actions.size() > 0) {
		      objInfo += loopActionsToString(etn.getUserObject().actions);
		    } else {
          objInfo += "\n0 empty";
        }
		  }
		}
	  
	  editor.setText(objInfo);
	  editor.setCaretPosition(0);
    editor.getCaret().setVisible(true); // show the caret
	}
	
	public GmObjectInfoFrame(GmObjectFrame gmObjF)
	{
	  gmObjFrame = gmObjF;
    setAlwaysOnTop(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(440,500);
		setTitle(Messages.getString("GmObjectInfoFrame.TITLE"));
		setResizable(true);
		setLocationRelativeTo(LGM.frame);
		
		fc = new CustomFileChooser("/org/lateralgm","LAST_GAMEINFO_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		fc.setFileFilter(new CustomFileFilter(
				Messages.getString("GameInformationFrame.TYPE_RTF"),".rtf")); //$NON-NLS-1$ //$NON-NLS-2$
		add(makeToolbar(), BorderLayout.NORTH);
		
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		//String key;
		editor = new JEditorPane();
		//editor.setEditorKit(rtf);
		p.add(editor, BorderLayout.CENTER);
    add(new JScrollPane(editor),BorderLayout.CENTER);
    editor.setText("object info will be displayed here when loaded");
    editor.setEditable(false);
    editor.getCaret().setVisible(true); // show the caret anyway
		makeContextMenu();
	}
	
	public void saveToFile()
		{
		fc.setDialogTitle(Messages.getString("GmObjectInfoFrame.SAVE_TITLE")); //$NON-NLS-1$
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
		String name = fc.getSelectedFile().getPath();
		if (CustomFileFilter.getExtension(name) == null) name += ".rtf"; //$NON-NLS-1$
		try
			{
			FileOutputStream i = new FileOutputStream(new File(name));
			rtf.write(i,editor.getDocument(),0,0);
			i.close();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	public void actionPerformed(ActionEvent ev)
	{
		String com = ev.getActionCommand();
		if (com.equals("GmObjectInfoFrame.FILESAVE")) //$NON-NLS-1$
		{
			saveToFile();
			return;
		}
		else if (com.equals("GmObjectInfoFrame.COPY")) //$NON-NLS-1$
		{
			editor.copy();
			return;
		}
		else if (com.equals("GmObjectInfoFrame.SELECTALL")) //$NON-NLS-1$
		{
			editor.selectAll();
			return;
		}
		else if (com.equals("GmObjectInfoFrame.CONFIRM")) //$NON-NLS-1$
		{
			this.setVisible(false);
			return;
		}
		
		editor.getCaret().setVisible(true); // make sure caret stays visible
	}
}