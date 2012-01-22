/* Copyright (C) 2011 Josh Ventura <joshv@zoominternet.net>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class FindDialog extends JDialog implements WindowListener,ActionListener
{
	private static final long serialVersionUID = 1L;
	public static JComboBox tFind, tReplace;
	public static JCheckBox whole, start, sens, esc, regex, wrap, back;
	public static JRadioButton scGame, scObject, scEvent, scCode, scSel;
	public static JButton bFind, bReplace, bRepAll;

	protected static FindDialog INSTANCE = new FindDialog();
	protected Set<ActionListener> listenerList = new HashSet<ActionListener>();
	protected static Set<ActionListener> permListenerList = new HashSet<ActionListener>();

	public JoshText selectedJoshText = null;

	private FindDialog()
	{
		super((Frame) null,"Find/Replace");
		applyLayout();
		addWindowListener(this);
		pack();
		setMinimumSize(getSize());
	}

	public static FindDialog getInstance()
	{
		return INSTANCE;
	}

	class EnterListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			bFind.doClick();
		}
	}

	public interface FindNavigator
	{
		void updateParameters(String find, String replace);
		
		void present();

		void findNext();

		void findPrevious();

		void replaceNext();

		void replacePrevious();
	}

	private void applyLayout()
	{
		GroupLayout gl = new GroupLayout(getContentPane());
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		setLayout(gl);

		JLabel lFind = new JLabel("Find: ");
		tFind = new JComboBox();
		tFind.setEditable(true);
		tFind.getEditor().addActionListener(new EnterListener());

		JLabel lReplace = new JLabel("Replace: ");
		tReplace = new JComboBox();
		tReplace.setEditable(true);
		tReplace.getEditor().addActionListener(new EnterListener());

		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options,BoxLayout.PAGE_AXIS));
		options.setBorder(BorderFactory.createTitledBorder("Options"));
		options.add(whole = new JCheckBox("Whole word"));
		options.add(start = new JCheckBox("Start of word"));
		options.add(wrap = new JCheckBox("Wrap at EOF"));
		options.add(sens = new JCheckBox("Case sensitive"));
		options.add(esc = new JCheckBox("Escape sequences"));
		options.add(regex = new JCheckBox("Regular expression"));
		options.add(back = new JCheckBox("Search backwards"));

		new ButGroup(whole,start);
		new ButGroup(esc,regex);
		regex.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				boolean b = regex.isSelected();
				back.setEnabled(!b);
			}
		});

		JPanel scope = new JPanel();
		scope.setLayout(new BoxLayout(scope,BoxLayout.PAGE_AXIS));
		scope.setBorder(BorderFactory.createTitledBorder("Scope"));

		ButtonGroup bg = new ButtonGroup();

		// TODO: Modularize to avoid ties to LGM.
		bg.add(scGame = new JRadioButton("Game"));
		scope.add(scGame);
		bg.add(scObject = new JRadioButton("Object"));
		scope.add(scObject);
		bg.add(scEvent = new JRadioButton("Event"));
		scope.add(scEvent);
		bg.add(scCode = new JRadioButton("Code",true));
		scope.add(scCode);
		bg.add(scSel = new JRadioButton("Selection"));
		scope.add(scSel);

		bFind = new JButton("Find");
		bFind.addActionListener(this);
		bReplace = new JButton("Replace");
		bReplace.addActionListener(this);
		bRepAll = new JButton("Replace all");
		bRepAll.addActionListener(this);
		JButton bClose = new JButton("Close");

		bFind.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (selectedJoshText == null)
				{
					System.err.println("No text editor selected. Ever.");
					return;
				}
				setVisible(false);
				selectedJoshText.finder.updateParameters((String) tFind.getEditor().getItem(),
						(String) tReplace.getEditor().getItem());
				selectedJoshText.finder.present();
				if (back.isSelected())
					selectedJoshText.finder.findPrevious();
				else
					selectedJoshText.finder.findNext();
			}
		});
		bReplace.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (selectedJoshText == null)
				{
					System.err.println("No text editor selected. Ever.");
					return;
				}
				setVisible(false);
				selectedJoshText.finder.updateParameters((String) tFind.getEditor().getItem(),
						(String) tReplace.getEditor().getItem());
				selectedJoshText.finder.present();
				if (back.isSelected())
					selectedJoshText.finder.replacePrevious();
				else
					selectedJoshText.finder.replaceNext();
			}
		});
		bClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		int pref = GroupLayout.PREFERRED_SIZE;

		gl.setHorizontalGroup(gl.createParallelGroup()
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(lFind)
		/*		*/.addComponent(lReplace))
		/*	*/.addGroup(gl.createParallelGroup()
		/*		*/.addComponent(tFind)
		/*		*/.addComponent(tReplace)))
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(options,pref,pref,Integer.MAX_VALUE)
		/*	*/.addComponent(scope,pref,pref,Integer.MAX_VALUE))
		/**/.addGroup(gl.createSequentialGroup()
		/*	*/.addComponent(bFind,pref,pref,Integer.MAX_VALUE)
		/*	*/.addComponent(bReplace,pref,pref,Integer.MAX_VALUE)
		/*	*/.addComponent(bRepAll,pref,pref,Integer.MAX_VALUE)
		/*	*/.addComponent(bClose,pref,pref,Integer.MAX_VALUE)));

		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(lFind)
		/*	*/.addComponent(tFind,pref,pref,pref))
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(lReplace)
		/*	*/.addComponent(tReplace,pref,pref,pref))
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(options,pref,pref,Integer.MAX_VALUE)
		/*	*/.addComponent(scope,pref,pref,Integer.MAX_VALUE))
		/**/.addGroup(gl.createParallelGroup()
		/*	*/.addComponent(bFind)
		/*	*/.addComponent(bReplace)
		/*	*/.addComponent(bRepAll)
		/*	*/.addComponent(bClose)));
	}

	/**
	 * Adds an action listener to this dialog for when one of the buttons gets pressed.
	 * Note that the ActionEvent.getSource() will be the respective button, and not this dialog.
	 * <p>Also note that this listener automatically unregisters when the dialog closes.
	 * For a more permanent listener that exists beyond dialog closes, use the static
	 * <code>addPermanentActionListener</code>.
	 * @param l The <code>ActionListener</code> to be added.
	 */
	public void addActionListener(ActionListener l)
	{
		listenerList.add(l);
	}

	/**
	 * Adds an action listener to this dialog that exists beyond dialog closes.
	 * Note that the ActionEvent.getSource() will be the respective button, and not this dialog.
	 * <p>For projects with multiple places that you can find/replace in, it becomes undesirable
	 * for one Find/replace to trigger the old listener of another location, so it would be
	 * preferable to remove the listener after a dialog close. The non-static
	 * <code>addActionListener</code> handles this for you automatically.
	 * @param l The <code>ActionListener</code> to be added.
	 */
	public static void addPermanentActionListener(ActionListener l)
	{
		permListenerList.add(l);
	}

	public static void removePermanentActionListener(ActionListener l)
	{
		permListenerList.remove(l);
	}

	public void actionPerformed(ActionEvent e)
	{
		for (ActionListener l : listenerList)
			l.actionPerformed(e);
		for (ActionListener l : permListenerList)
			l.actionPerformed(e);
	}

	public void windowClosed(WindowEvent e)
	{
		listenerList.clear();
	}

	public void windowClosing(WindowEvent e)
	{
		listenerList.clear();
	}

	//unused
	public void windowActivated(WindowEvent e)
	{ //unused
	}

	public void windowDeactivated(WindowEvent e)
	{ //unused
	}

	public void windowDeiconified(WindowEvent e)
	{ //unused
	}

	public void windowIconified(WindowEvent e)
	{ //unused
	}

	public void windowOpened(WindowEvent e)
	{ //unused
	}
}