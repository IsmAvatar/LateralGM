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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
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
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.subframes.GmObjectFrame.EventGroupNode;
import org.lateralgm.subframes.GmObjectFrame.EventInstanceNode;
import org.lateralgm.subframes.GmObjectFrame.EventTree;

import com.sun.xml.internal.txw2.Document;

// TODO: This window can be reformated to work with timelines as well

public class ResourceInfoFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	protected JSpinner sSizes;
	protected JEditorPane editor;
	protected Color fgColor;
	private CustomFileChooser fc;
	private GmObjectFrame gmObjFrame;
	private TimelineFrame timelineFrame;
	private int linesOfCode = 0;

	public JToolBar makeToolbar()
	{
		JToolBar tb = new JToolBar();
		tb.add(addToolbarItem("ResourceInfoFrame.CONFIRM"));
		tb.addSeparator();
		tb.add(addToolbarItem("ResourceInfoFrame.FILESAVE"));
		tb.add(addToolbarItem("ResourceInfoFrame.PRINT"));
		tb.addSeparator();
		tb.add(addToolbarItem("ResourceInfoFrame.COPY"));
		return tb;
	}

public JPopupMenu makeContextMenu()
	{
  // build popup menu
  final JPopupMenu popup = new JPopupMenu();
  JMenuItem item;
  
	item = addItem("ResourceInfoFrame.COPY"); //$NON-NLS-1$
	item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,KeyEvent.CTRL_DOWN_MASK));
	popup.add(item);
	popup.addSeparator();
	item = addItem("ResourceInfoFrame.SELECTALL"); //$NON-NLS-1$
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

public static int countLines(String str)
{
    if (str == null || str.length() == 0)
        return 0;
    int lines = 1;
    int len = str.length();
    for( int pos = 0; pos < len; pos++) {
        char c = str.charAt(pos);
        if( c == '\r' ) {
            lines++;
            if ( pos+1 < len && str.charAt(pos+1) == '\n' )
                pos++;
        } else if( c == '\n' ) {
            lines++;
        }
    }
    return lines;
}

	public String loopActionsToString(List<Action> list)
	{
	  String info = "";
	  linesOfCode = 0;
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
      String code = "";
      if (la.actionKind == Action.ACT_CODE)
      {
          code = args.get(args.size() - 1).toString(libargs[args.size() - 1]);
          linesOfCode += countLines(code);
          text += " (" + linesOfCode + " Lines)";
          text += "\n**** BEGIN ****";
        	text += "\n" + code;
        	text += "\n**** END ****";
      }
      
		  info += "\n" + i + " " + text;
	  }
	  
	  return info;
	}
	
	public void updateTimelineInfo()
	{
	  if (timelineFrame == null) {
	    editor.setText("ERROR! Timeline Frame Does Not Exist");
	    editor.setCaretPosition(0);
      editor.getCaret().setVisible(true); // show the caret
      return;
	  }
	  int totalLinesOfCode = 0;
	  String propInfo = "------ Properties ------\n\n";
	  ResourceReference<?> res;
	  propInfo += Messages.getString("TimelineFrame.NAME") + " " + timelineFrame.name.getText() + "\n";
	  propInfo += Messages.getString("TimelineFrame.MOMENTS") + " " +
	    timelineFrame.moments.getModel().getSize() + "\n";
	  propInfo += "Total Lines of Code" + ": ";
	  
	  String momInfo = "\n------ Moments ------";
	  
	  // Make the current selected event realize any recent changes to actions
		JList moms = timelineFrame.moments;
		ListModel mommodel = moms.getModel();
		
		if (mommodel.getSize() > 0) {
	    Moment node = (Moment)mommodel.getElementAt(moms.getSelectedIndex());
	    if (node != null) {
	      timelineFrame.actions.setActionContainer(node);
	    }
		}

		Moment mom = null;
		String actInfo;
		for (int i = 0; i < mommodel.getSize(); i++)
		{
		  mom = (Moment)mommodel.getElementAt(i);
			momInfo += "\n\n  " + mom.toString();
	    if (mom.actions.size() > 0) {
	      actInfo = loopActionsToString(mom.actions);
	      totalLinesOfCode += linesOfCode;
	      momInfo += " (" + linesOfCode + " Lines Of Code) :";
        momInfo += actInfo;
      } else {
        momInfo += ":\n " + Messages.getString("TimelineFrame.EMPTY");
      }
		}
	 
	  editor.setText(propInfo + totalLinesOfCode + "\n" + momInfo);
	  editor.setCaretPosition(0);
    editor.getCaret().setVisible(true); // show the caret
	}
	
	public void updateObjectInfo()
	{
    if (gmObjFrame == null) {
      editor.setText("ERROR! Object Frame Does Not Exist");
      editor.setCaretPosition(0);
      editor.getCaret().setVisible(true); // show the caret
      return;
    }

    int totalLinesOfCode = 0;
	  String propInfo = "------ Properties ------\n\n";
	  ResourceReference<?> res;
	  propInfo += Messages.getString("GmObjectFrame.NAME") + ": " + gmObjFrame.name.getText() + "\n";
	  
	  res = gmObjFrame.parent.getSelected();
	  propInfo += Messages.getString("GmObjectFrame.PARENT") + ": ";
	  if (res != null) {
	    propInfo += res.get().getName();
	  } else {
  	  propInfo += Messages.getString("GmObjectFrame.NO_PARENT");
    }
    propInfo += "\n";
    
	  res = gmObjFrame.sprite.getSelected();
	  propInfo += Messages.getString("GmObjectFrame.SPRITE") + ": ";
	  if (res != null) {
	    propInfo += res.get().getName();
	  } else {
  	  propInfo += Messages.getString("GmObjectFrame.NO_SPRITE");
    }
    propInfo += "\n";
    
	  res = gmObjFrame.mask.getSelected();
	  propInfo += Messages.getString("GmObjectFrame.MASK") + ": ";
	  if (res != null) {
	    propInfo += res.get().getName();
	  } else {
  	  propInfo += Messages.getString("GmObjectFrame.SAME_AS_SPRITE");
    }
    propInfo += "\n";

	  propInfo += Messages.getString("GmObjectFrame.VISIBLE") + ": " + gmObjFrame.visible.isSelected() + "\n";
	  propInfo += Messages.getString("GmObjectFrame.SOLID") + ": " + gmObjFrame.solid.isSelected() + "\n"; 
	  propInfo += Messages.getString("GmObjectFrame.DEPTH") + ": " + gmObjFrame.depth.getValue().toString() + "\n";
	  propInfo += Messages.getString("GmObjectFrame.PERSISTENT") + ": " + gmObjFrame.persistent.isSelected() + "\n";
	  propInfo += "Total Lines of Code" + ": ";
	  
	  String evtInfo = "\n------ Events ------";
	  
	  // this here will need rewritten if its ever planned that
	  // event tree nodes and event tree group nodes will have
	  // multiple child group nodes as this code is not recursive 
	  // the event tree also needs to be abstracted enough for obtaining
	  // the information from other classes and components 
	  // i dont like it as is - Robert B. Colton
	  
	  // Make the current selected event realize any recent changes to actions
	  DefaultMutableTreeNode node = (DefaultMutableTreeNode) gmObjFrame.events.getLastSelectedPathComponent();
	  if (node != null) {
	    gmObjFrame.actions.setActionContainer((Event) node.getUserObject());
	  }
		
		EventGroupNode e = (EventGroupNode)gmObjFrame.events.getModel().getRoot();
		if (e == null)
		{
			return;
		}
		EventInstanceNode etn, etnc;
		String actInfo;
		for (int i=0; i<e.getChildCount(); i++)
		{
		  TreeNode tn = (TreeNode) e.getChildAt(i);	
		  int etncc = tn.getChildCount();
		  if (etncc > 0)
		  {
			  for (int ii=0; ii<etncc; ii++)
				{
				  etnc = (EventInstanceNode) tn.getChildAt(ii);	
					evtInfo += "\n\n  " + etnc.toString();
			    if (etnc.getUserObject().actions.size() > 0) {
			      actInfo = loopActionsToString(etnc.getUserObject().actions);
			      totalLinesOfCode += linesOfCode;
			      evtInfo += " (" + linesOfCode + " Lines Of Code) :";
		        evtInfo += actInfo;
		      } else {
		        evtInfo += ":\n " + Messages.getString("GmObjectFrame.EMPTY");
		      }
				}
		  } else {
		    etn = (EventInstanceNode) e.getChildAt(i);	
			  evtInfo += "\n\n  " + etn.toString();
		    evtInfo += ""; 
		    if (etn.getUserObject().actions.size() > 0) {
	        actInfo = loopActionsToString(etn.getUserObject().actions);
	        evtInfo += " (" + linesOfCode + " Lines Of Code) :";
	        totalLinesOfCode += linesOfCode;
          evtInfo += actInfo;
		    } else {
          evtInfo += ":\n " + Messages.getString("GmObjectFrame.EMPTY");
        }
		  }
		}
	  
	  editor.setText(propInfo + totalLinesOfCode + "\n" + evtInfo);
	  editor.setCaretPosition(0);
    editor.getCaret().setVisible(true); // show the caret
	}
	
	public ResourceInfoFrame()
	{
    setAlwaysOnTop(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(440,500);
		setTitle(Messages.getString("ResourceInfoFrame.TITLE"));
		setResizable(true);
		setLocationRelativeTo(LGM.frame);
		
		fc = new CustomFileChooser("/org/lateralgm","LAST_GAMEINFO_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		fc.setFileFilter(new CustomFileFilter(
				Messages.getString("ResourceInfoFrame.TYPE_TXT"),".txt")); //$NON-NLS-1$ //$NON-NLS-2$
		add(makeToolbar(), BorderLayout.NORTH);
		
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		//String key;
		editor = new JEditorPane();
		p.add(editor, BorderLayout.CENTER);
    add(new JScrollPane(editor),BorderLayout.CENTER);
    editor.setText("object info will be displayed here when loaded");
    editor.setFont(new Font("Courier 10 Pitch", Font.PLAIN, 12));
    editor.setEditable(false);
    editor.getCaret().setVisible(true); // show the caret anyway
		makeContextMenu();
	}
	
	public ResourceInfoFrame(GmObjectFrame gmObjF)
		{
      this();
		  gmObjFrame = gmObjF;
		}
	
	public ResourceInfoFrame(TimelineFrame timelineF)
		{
      this();
		  timelineFrame = timelineF;
		}
	
	public void saveToFile()
		{
		fc.setDialogTitle(Messages.getString("ResourceInfoFrame.SAVE_TITLE")); //$NON-NLS-1$
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
		String name = fc.getSelectedFile().getPath();
		if (CustomFileFilter.getExtension(name) == null) name += ".txt"; //$NON-NLS-1$
		try
			{
			FileWriter out = new FileWriter(name);
      out.write(editor.getText());
      out.close();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	public void actionPerformed(ActionEvent ev)
	{
		String com = ev.getActionCommand();
		if (com.equals("ResourceInfoFrame.FILESAVE")) //$NON-NLS-1$
		{
			saveToFile();
			return;
		}
		else if (com.equals("ResourceInfoFrame.COPY")) //$NON-NLS-1$
		{
			editor.copy();
			return;
		}
		else if (com.equals("ResourceInfoFrame.SELECTALL")) //$NON-NLS-1$
		{
			editor.selectAll();
			return;
		}
		else if (com.equals("ResourceInfoFrame.CONFIRM")) //$NON-NLS-1$
		{
			this.setVisible(false);
			return;
		}
		else if (com.equals("ResourceInfoFrame.PRINT")) //$NON-NLS-1$
		{
	    //TODO: Make the fucker actually print
	    PrinterJob pj = PrinterJob.getPrinterJob();
      if (pj.printDialog()) {
          try {
            pj.print();
          }
          catch (PrinterException exc) {
            System.out.println(exc);
          }
       }   
			return;
		}
		
		editor.getCaret().setVisible(true); // make sure caret stays visible
	}
}