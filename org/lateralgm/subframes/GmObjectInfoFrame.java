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
import java.util.List;
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
import org.lateralgm.resources.library.LibArgument;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.subframes.GmObjectFrame.EventGroupNode;
import org.lateralgm.subframes.GmObjectFrame.EventInstanceNode;
import org.lateralgm.subframes.GmObjectFrame.EventTree;

import com.sun.xml.internal.txw2.Document;

// TODO: This window can be reformated to work with timelines as well

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
		
		  List<Argument> args = a.getArguments();
		  LibArgument[] libargs = la.libArguments;
		  
		  /* this is code that could be used to make the information more detailed with exact parameter values
		   * i would also suggest adding this as a function to the action or lib action to convert it into a descriptive
		   * string instead of just a generic list description
		  String text = la.hintText;
		  for (int ii = 0; ii < args.size(); ii++)
		  {
		  	text = text.replace("@" + ii, args.get(ii).toString(libargs[ii]));
		  }
		  */
      String text = la.description;
      
      if (la.actionKind == Action.ACT_CODE)
      {
          text += "\n**** BEGIN ****";
        	text += "\n" + args.get(args.size() - 1).toString(libargs[args.size() - 1]);
        	text += "\n**** END ****";
      }
      
		  info += "\n" + i + " " + text;
	  }
	  
	  return info;
	}
	
	public void updateObjectInfo()
	{
	  String objInfo = "------ Properties ------\n\n";
	  ResourceReference<?> res;
	  objInfo += Messages.getString("GmObjectFrame.NAME") + ": " + gmObjFrame.name.getText() + "\n";
	  
	  res = gmObjFrame.parent.getSelected();
	  objInfo += Messages.getString("GmObjectFrame.PARENT") + ": ";
	  if (res != null) {
	    objInfo += res.get().getName();
	  } else {
  	  objInfo += Messages.getString("GmObjectFrame.NO_PARENT");
    }
    objInfo += "\n";
    
	  res = gmObjFrame.sprite.getSelected();
	  objInfo += Messages.getString("GmObjectFrame.SPRITE") + ": ";
	  if (res != null) {
	    objInfo += res.get().getName();
	  } else {
  	  objInfo += Messages.getString("GmObjectFrame.NO_SPRITE");
    }
    objInfo += "\n";
    
	  res = gmObjFrame.mask.getSelected();
	  objInfo += Messages.getString("GmObjectFrame.MASK") + ": ";
	  if (res != null) {
	    objInfo += res.get().getName();
	  } else {
  	  objInfo += Messages.getString("GmObjectFrame.SAME_AS_SPRITE");
    }
    objInfo += "\n";

	  objInfo += Messages.getString("GmObjectFrame.VISIBLE") + ": " + gmObjFrame.visible.isSelected() + "\n";
	  objInfo += Messages.getString("GmObjectFrame.SOLID") + ": " + gmObjFrame.solid.isSelected() + "\n"; 
	  objInfo += Messages.getString("GmObjectFrame.DEPTH") + ": " + gmObjFrame.depth.getValue().toString() + "\n";
	  objInfo += Messages.getString("GmObjectFrame.PERSISTENT") + ": " + gmObjFrame.persistent.isSelected() + "\n";
	  
	  objInfo += "\n------ Events ------";
	  
	  // this here will need rewritten if its ever planned that
	  // event tree nodes and event tree group nodes will have
	  // multiple child group nodes as this code is not recursive 
	  // the event tree also needs to be abstracted enough for obtaining
	  // the information from other classes and components 
	  // i dont like it as is - Robert B. Colton
	  
		gmObjFrame.saveEvents(); // this here might need refactored 
		// but i had to add it otherwise the panel your editing, changes
		// wont show to obj info window, but it might make the whole object
		// save but i dont think so
	  // iterate events and get each ones model
		EventGroupNode e = (EventGroupNode)gmObjFrame.events.getModel().getRoot();
		EventInstanceNode etn, etnc;
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
			    if (etnc.getUserObject().actions.size() > 0) {
		        objInfo += loopActionsToString(etnc.getUserObject().actions);
		      } else {
		        objInfo += "\n0 " + Messages.getString("GmObjectFrame.EMPTY");
		      }
				}
		  } else {
		    etn = (EventInstanceNode) e.getChildAt(i);	
			  objInfo += "\n\n  " + etn.toString();
		    objInfo += ":"; 
		    if (etn.getUserObject().actions.size() > 0) {
		      objInfo += loopActionsToString(etn.getUserObject().actions);
		    } else {
          objInfo += "\n0 " + Messages.getString("GmObjectFrame.EMPTY");
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