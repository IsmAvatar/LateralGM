/**
* @file  ResourceInfoFrame.java
* @brief Class implementing a resource information frame.
*
* @section License
*
* Copyright (C) 2013-2014 Robert B. Colton
* This file is a part of the LateralGM IDE.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
**/
package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.FileWriter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.GmObject.PGmObject;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibArgument;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.ShapePoint;

public class ResourceInfoFrame extends JFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	protected JTextArea editor;
	protected Color fgColor;
	private CustomFileChooser fc;
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
		item.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ResourceInfoFrame.COPY")));
		popup.add(item);
		popup.addSeparator();
		item = addItem("ResourceInfoFrame.SELECTALL"); //$NON-NLS-1$
		item.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("ResourceInfoFrame.SELECTALL")));
		popup.add(item);

		editor.setComponentPopupMenu(popup);

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
		if (str == null || str.length() == 0) return 0;
		int lines = 1;
		int len = str.length();
		for (int pos = 0; pos < len; pos++)
			{
			char c = str.charAt(pos);
			if (c == '\r')
				{
				lines++;
				if (pos + 1 < len && str.charAt(pos + 1) == '\n') pos++;
				}
			else if (c == '\n')
				{
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
				text += "\n------ BEGIN ------";
				text += "\n" + code;
				text += "\n------  END  ------";
				}

			info += "\n" + i + " " + text;
			}

		return info;
		}

	public void updateTimelineInfo(ResourceReference<Timeline> res)
		{
		setIconImage(LGM.getIconForKey("Resource.TML").getImage());
		setTitle(Messages.getString("ResourceInfoFrame.TIMELINE_TITLE"));
		if (res == null)
			{
			editor.setText("ERROR! Timeline does not exist.");
			editor.setCaretPosition(0);
			editor.getCaret().setVisible(true); // show the caret
			return;
			}
		int totalLinesOfCode = 0;

		Timeline tml = res.get();
		String propInfo = "**** Properties ****\n\n";
		propInfo += Messages.getString("TimelineFrame.NAME") + " " + tml.getName() + "\n";
		propInfo += Messages.getString("TimelineFrame.MOMENTS") + " " + tml.moments.size() + "\n";
		propInfo += "Total Lines of Code" + ": ";

		String momInfo = "\n**** Moments ****";

		String actInfo;
		for (Moment mom : tml.moments)
			{
			momInfo += "\n\n  " + mom.toString();
			if (mom.actions.size() > 0)
				{
				actInfo = loopActionsToString(mom.actions);
				totalLinesOfCode += linesOfCode;
				momInfo += " (" + linesOfCode + " Lines Of Code) :";
				momInfo += actInfo;
				}
			else
				{
				momInfo += ":\n " + Messages.getString("TimelineFrame.EMPTY");
				}
			}

		editor.setText(propInfo + totalLinesOfCode + "\n" + momInfo + "\n");
		editor.setCaretPosition(0);
		editor.getCaret().setVisible(true); // show the caret
		}

	public void updateObjectInfo(ResourceReference<GmObject> ref)
		{
		setIconImage(LGM.getIconForKey("Resource.OBJ").getImage());
		setTitle(Messages.getString("ResourceInfoFrame.OBJECT_TITLE"));
		if (ref == null)
			{
			editor.setText("ERROR! Object does not exist.");
			editor.setCaretPosition(0);
			editor.getCaret().setVisible(true); // show the caret
			return;
			}
		int totalLinesOfCode = 0;

		GmObject obj = ref.get();

		String propInfo = "**** Properties ****\n\n";
		propInfo += Messages.getString("GmObjectFrame.NAME") + ": " + obj.getName() + "\n";

		ResourceReference<?> res = obj.get(PGmObject.PARENT);
		propInfo += Messages.getString("GmObjectFrame.PARENT") + ": ";
		if (res != null)
			{
			propInfo += res.get().getName();
			}
		else
			{
			propInfo += Messages.getString("GmObjectFrame.NO_PARENT");
			}
		propInfo += "\n";

		res = obj.get(PGmObject.SPRITE);
		propInfo += Messages.getString("GmObjectFrame.SPRITE") + ": ";
		if (res != null)
			{
			propInfo += res.get().getName();
			}
		else
			{
			propInfo += Messages.getString("GmObjectFrame.NO_SPRITE");
			}
		propInfo += "\n";

		res = obj.get(PGmObject.MASK);
		propInfo += Messages.getString("GmObjectFrame.MASK") + ": ";
		if (res != null)
			{
			propInfo += res.get().getName();
			}
		else
			{
			propInfo += Messages.getString("GmObjectFrame.SAME_AS_SPRITE");
			}
		propInfo += "\n";

		propInfo += Messages.getString("GmObjectFrame.VISIBLE") + ": " + obj.get(PGmObject.VISIBLE)
				+ "\n";
		propInfo += Messages.getString("GmObjectFrame.SOLID") + ": " + obj.get(PGmObject.SOLID) + "\n";
		propInfo += Messages.getString("GmObjectFrame.DEPTH") + ": " + obj.get(PGmObject.DEPTH) + "\n";
		propInfo += Messages.getString("GmObjectFrame.PERSISTENT") + ": "
				+ obj.get(PGmObject.PERSISTENT) + "\n";
		propInfo += "Total Lines of Code" + ": ";

		String phyInfo = "**** Physics ****\n\n";
		phyInfo += Messages.getString("GmObjectFrame.USES_PHYSICS") + ": "
				+ obj.get(PGmObject.PHYSICS_OBJECT) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.AWAKE") + ": " + obj.get(PGmObject.PHYSICS_AWAKE)
				+ "\n";
		phyInfo += Messages.getString("GmObjectFrame.SENSOR") + ": "
				+ obj.get(PGmObject.PHYSICS_SENSOR) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.KINEMATIC") + ": "
				+ obj.get(PGmObject.PHYSICS_KINEMATIC) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.DENSITY") + ": "
				+ obj.get(PGmObject.PHYSICS_DENSITY) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.RESTITUTION") + ": "
				+ obj.get(PGmObject.PHYSICS_RESTITUTION) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.COLLISION_GROUP") + ": "
				+ obj.get(PGmObject.PHYSICS_GROUP) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.DAMPING_LINEAR") + ": "
				+ obj.get(PGmObject.PHYSICS_DAMPING_LINEAR) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.DAMPING_ANGULAR") + ": "
				+ obj.get(PGmObject.PHYSICS_DAMPING_ANGULAR) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.FRICTION") + ": "
				+ obj.get(PGmObject.PHYSICS_FRICTION) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.COLLISION_SHAPE") + ": "
				+ obj.get(PGmObject.PHYSICS_SHAPE) + "\n";
		phyInfo += Messages.getString("GmObjectFrame.SHAPE_POINTS") + ": " + obj.shapePoints.size()
				+ "\n";

		for (ShapePoint sp : obj.shapePoints)
			{
			phyInfo += sp.getX() + ", " + sp.getY() + "\n";
			}

		String evtInfo = "\n**** Events ****";

		for (MainEvent me : obj.mainEvents)
			{
			for (Event ev : me.events)
				{
				if (ev.actions.size() > 0)
					{
					evtInfo += "\n\n  " + Event.eventName(ev.mainId,ev.id);
					String actInfo = loopActionsToString(ev.actions);
					totalLinesOfCode += linesOfCode;
					evtInfo += " (" + linesOfCode + " Lines Of Code) :";
					evtInfo += actInfo;
					}
				else
					{
					evtInfo += ":\n " + Messages.getString("GmObjectFrame.EMPTY");
					}
				}
			}

		editor.setText(propInfo + totalLinesOfCode + "\n\n" + phyInfo + evtInfo + "\n");
		editor.setCaretPosition(0);
		editor.getCaret().setVisible(true); // show the caret
		}

	public ResourceInfoFrame()
		{
		//setAlwaysOnTop(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(440,500);
		setLocationRelativeTo(LGM.frame);

		fc = new CustomFileChooser("/org/lateralgm","LAST_GAMEINFO_DIR"); //$NON-NLS-1$ //$NON-NLS-2$
		fc.setFileFilter(new CustomFileFilter(Messages.getString("ResourceInfoFrame.TYPE_TXT"),".txt")); //$NON-NLS-1$ //$NON-NLS-2$
		add(makeToolbar(),BorderLayout.NORTH);

		editor = new JTextArea();
		editor.setWrapStyleWord(false);
		JScrollPane scrollable = new JScrollPane(editor);
		add(scrollable,BorderLayout.CENTER);
		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(editor));
		editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		editor.setText("object info will be displayed here when loaded");
		editor.setEditable(false);
		editor.getCaret().setVisible(true); // show the caret anyway
		editor.addFocusListener(new FocusListener()
			{
				public void focusLost(FocusEvent e)
					{
					return;
					}

				public void focusGained(FocusEvent e)
					{
					editor.getCaret().setVisible(true); // show the caret anyway
					}
			});
		makeContextMenu();
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
			try {
				editor.print();
			} catch (Exception pex) {
				LGM.showDefaultExceptionHandler(pex);
			}
			return;
			}

		editor.getCaret().setVisible(true); // make sure caret stays visible
		}
	}
