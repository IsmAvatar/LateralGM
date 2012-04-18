/*
 * Copyright (C) 2008, 2012 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.lateralgm.file.GmFile.ResourceHolder;
import org.lateralgm.file.ResourceList;
import org.lateralgm.jedit.GMLKeywords;
import org.lateralgm.joshedit.Code;
import org.lateralgm.joshedit.CompletionMenu;
import org.lateralgm.joshedit.CompletionMenu.Completion;
import org.lateralgm.joshedit.GMLTokenMarker;
import org.lateralgm.joshedit.DefaultTokenMarker;
import org.lateralgm.joshedit.DefaultTokenMarker.KeywordSet;
import org.lateralgm.joshedit.JoshText;
import org.lateralgm.joshedit.JoshText.CodeMetrics;
import org.lateralgm.joshedit.JoshText.LineChangeListener;
import org.lateralgm.joshedit.JoshText.Highlighter;
import org.lateralgm.joshedit.Runner;
import org.lateralgm.joshedit.Runner.EditorInterface;
import org.lateralgm.joshedit.Runner.JoshTextPanel;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Script;

public class GMLTextArea extends JoshTextPanel implements UpdateListener
	{
	private static final long serialVersionUID = 1L;

	static
		{
		Runner.editorInterface = new EditorInterface()
			{
				public ImageIcon getIconForKey(String key)
					{
					return LGM.getIconForKey(key);
					}

				public String getString(String key)
					{
					return Messages.getString(key);
					}

				public String getString(String key, String def)
					{
					String str = getString(key);
					if (str.equals('!' + key + '!')) return def;
					return str;
					}
			};
		}

	private static final GMLKeywords.Keyword[][] GML_KEYWORDS = { GMLKeywords.CONSTRUCTS,
			GMLKeywords.FUNCTIONS,GMLKeywords.VARIABLES,GMLKeywords.OPERATORS,GMLKeywords.CONSTANTS };

	protected static Timer timer;
	protected Integer lastUpdateTaskID = 0;
	private Set<SortedSet<String>> resourceKeywords = new HashSet<SortedSet<String>>();
	protected Completion[] completions;
	protected DefaultTokenMarker gmlTokenMarker = new GMLTokenMarker();

	private static final Color PURPLE = new Color(128,0,128);
	private static final Color BROWN = new Color(128,0,0);
	private static final Color FUNCTION = new Color(0,0,128);

	static KeywordSet resNames, scrNames, constructs, functions, operators, constants, variables;

	static
		{
		resNames = new KeywordSet("Resource Names",PURPLE,Font.PLAIN);
		scrNames = new KeywordSet("Script Names",FUNCTION,Font.PLAIN);
		functions = new KeywordSet("Functions",FUNCTION,Font.PLAIN);
		constructs = new KeywordSet("Constructs",Color.BLACK,Font.BOLD);
		operators = new KeywordSet("Operators",Color.BLACK,Font.BOLD);
		constants = new KeywordSet("Constants",BROWN,Font.PLAIN);
		variables = new KeywordSet("Variables",Color.BLUE,Font.ITALIC);
		}

	public GMLTextArea()
		{
		this(null);
		}

	public GMLTextArea(String code)
		{
		super(code);

		setTabSize(Prefs.tabSize);
		setTokenMarker(gmlTokenMarker);
		setupKeywords();
		updateKeywords();
		updateResourceKeywords();
		text.setFont(Prefs.codeFont);
		//painter.setStyles(PrefsStore.getSyntaxStyles());
		text.getActionMap().put("COMPLETIONS",completionAction);
		LGM.currentFile.updateSource.addListener(this);
		/*		addCaretListener(undoManager);
				document.addUndoableEditListener(undoManager);
				inputHandler.addKeyBinding("C+Z",undoManager.getUndoAction()); //$NON-NLS-1$
				inputHandler.addKeyBinding("C+Y",undoManager.getRedoAction()); //$NON-NLS-1$
				inputHandler.addKeyBinding("C+SPACE",new CompletionAction());*/
		}

	private static JButton makeToolbarButton(Action a)
		{
		String key = "JoshText." + a.getValue(Action.NAME);
		JButton b = new JButton(LGM.getIconForKey(key));
		b.setToolTipText(Messages.getString(key));
		b.setRequestFocusEnabled(false);
		b.addActionListener(a);
		return b;
		}

	public void addEditorButtons(JToolBar tb)
		{
		tb.add(makeToolbarButton(text.aUndo));
		tb.add(makeToolbarButton(text.aRedo));
		tb.add(makeToolbarButton(gotoAction));
		tb.addSeparator();
		tb.add(makeToolbarButton(text.aCut));
		tb.add(makeToolbarButton(text.aCopy));
		tb.add(makeToolbarButton(text.aPaste));
		}

	AbstractAction gotoAction = new AbstractAction("GOTO")
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
				{
				int line = showGotoDialog(getCaretLine());
				line = Math.max(0,Math.min(getLineCount() - 1,line));
				setCaretPosition(line,0);
				}
		};

	public static int showGotoDialog(int defVal)
		{
		final JDialog d = new JDialog((Frame) null,true);
		JPanel p = new JPanel();
		GroupLayout layout = new GroupLayout(p);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		p.setLayout(layout);

		JLabel l = new JLabel("Line: ");
		NumberField f = new NumberField(defVal);
		f.selectAll();
		JButton b = new JButton("Goto");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					d.setVisible(false);
					}
			});

		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(l)
		/*	*/.addComponent(f))
		/**/.addComponent(b,Alignment.CENTER));
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(l)
		/*	*/.addComponent(f))
		/**/.addComponent(b));

		//					JOptionPane.showMessageDialog(null,p);
		d.setContentPane(p);
		d.pack();
		d.setResizable(false);
		d.setLocationRelativeTo(null);
		d.setVisible(true); //blocks until user clicks OK

		return f.getIntValue();
		}

	private void setupKeywords()
		{
		gmlTokenMarker.tmKeywords.add(resNames);
		gmlTokenMarker.tmKeywords.add(scrNames);
		gmlTokenMarker.tmKeywords.add(functions);
		gmlTokenMarker.tmKeywords.add(constructs);
		gmlTokenMarker.tmKeywords.add(operators);
		gmlTokenMarker.tmKeywords.add(constants);
		gmlTokenMarker.tmKeywords.add(variables);
		}

	public static void updateKeywords()
		{
		constructs.words.clear();
		operators.words.clear();
		constants.words.clear();
		variables.words.clear();
		functions.words.clear();

		for (GMLKeywords.Construct keyword : GMLKeywords.CONSTRUCTS)
			constructs.words.add(keyword.getName());
		for (GMLKeywords.Operator keyword : GMLKeywords.OPERATORS)
			operators.words.add(keyword.getName());
		for (GMLKeywords.Constant keyword : GMLKeywords.CONSTANTS)
			constants.words.add(keyword.getName());
		for (GMLKeywords.Variable keyword : GMLKeywords.VARIABLES)
			variables.words.add(keyword.getName());
		for (GMLKeywords.Function keyword : GMLKeywords.FUNCTIONS)
			functions.words.add(keyword.getName());
		}

	public static void updateResourceKeywords()
		{
		resNames.words.clear();
		scrNames.words.clear();
		for (Entry<Class<?>,ResourceHolder<?>> e : LGM.currentFile.resMap.entrySet())
			{
			if (!(e.getValue() instanceof ResourceList<?>)) continue;
			ResourceList<?> rl = (ResourceList<?>) e.getValue();
			KeywordSet ks = e.getKey() == Script.class ? scrNames : resNames;
			for (Resource<?,?> r : rl)
				ks.words.add(r.getName());
			}
		}

	protected void updateCompletions()
		{
		int l = 0;
		for (Set<String> a : resourceKeywords)
			l += a.size();
		for (GMLKeywords.Keyword[] a : GML_KEYWORDS)
			l += a.length;
		completions = new Completion[l];
		int i = 0;
		for (Set<String> a : resourceKeywords)
			for (String s : a)
				completions[i++] = new CompletionMenu.WordCompletion(s);
		for (GMLKeywords.Keyword[] a : GML_KEYWORDS)
			for (GMLKeywords.Keyword k : a)
				{
				if (k instanceof GMLKeywords.Function)
					completions[i] = new FunctionCompletion((GMLKeywords.Function) k);
				else if (k instanceof GMLKeywords.Variable)
					completions[i] = new VariableCompletion((GMLKeywords.Variable) k);
				else
					completions[i] = new CompletionMenu.WordCompletion(k.getName());
				i++;
				}
		}

	public class VariableCompletion extends CompletionMenu.Completion
		{
		private final GMLKeywords.Variable variable;

		public VariableCompletion(GMLKeywords.Variable v)
			{
			variable = v;
			name = v.getName();
			}

		public boolean apply(JoshText a, char input, int row, int start, int end)
			{
			String s = name;
			int p = s.length();
			if (variable.arraySize > 0)
				{
				s += "[]";
				boolean ci = true;
				switch (input)
					{
					case '\0':
					case '[':
						break;
					case ']':
						ci = false;
						break;
					default:
						s += String.valueOf(input);
					}
				if (ci)
					p = s.length() - 1;
				else
					p = s.length();
				}
			if (!replace(a,row,start,end,s)) return false;
			setCaretPosition(row,start + p);
			return true;
			}

		public String toString()
			{
			String s = name;
			if (variable.arraySize > 0) s += "[0.." + String.valueOf(variable.arraySize - 1) + "]";
			if (variable.readOnly) s += "*";
			return s;
			}
		}

	public class FunctionCompletion extends CompletionMenu.Completion
		{
		private final GMLKeywords.Function function;

		public FunctionCompletion(GMLKeywords.Function f)
			{
			function = f;
			name = f.getName();
			}

		public boolean apply(JoshText a, char input, int row, int start, int end)
			{
			String s = name + "(" + getArguments() + ")";
			int p1, p2;
			boolean argSel = true;
			switch (input)
				{
				case '\0':
				case '(':
					break;
				case ')':
					argSel = false;
					break;
				default:
					s += String.valueOf(input);
				}
			if (argSel && function.arguments.length > 0)
				{
				p1 = name.length() + 1;
				p2 = p1 + getArgument(0).length();
				}
			else
				{
				p1 = s.length();
				p2 = p1;
				}
			if (!replace(a,row,start,end,s)) return false;
			setSelection(row,start + p1,row,start + p2);
			return true;
			}

		public String getArgument(int i)
			{
			if (i >= function.arguments.length) return null;
			return function.arguments[i] + (i == function.dynArgIndex ? "..." : "");
			}

		public String getArguments()
			{
			String s = "";
			for (int i = 0; i < function.arguments.length; i++)
				s += (i > 0 ? "," : "") + getArgument(i);
			return s;
			}

		public String toString()
			{
			return name + "(" + getArguments() + ")";
			}
		}

	private static String find(String input, Pattern p)
		{
		Matcher m = p.matcher(input);
		if (m.find()) return m.group();
		return new String();
		}

	AbstractAction completionAction = new AbstractAction("COMPLETE")
		{
			private static final long serialVersionUID = 1L;
			final Pattern W_BEFORE = Pattern.compile("\\w+$");
			final Pattern W_AFTER = Pattern.compile("^\\w+");

			public void actionPerformed(ActionEvent e)
				{
				int pos = getCaretColumn();
				int row = getCaretLine();
				String lt = getLineText(row);
				int x1 = pos - find(lt.substring(0,pos),W_BEFORE).length();
				int x2 = pos + find(lt.substring(pos),W_AFTER).length();
				if (completions == null) updateCompletions();
				new CompletionMenu(LGM.frame,text,row,x1,x2,pos,completions);
				}
		};

	public void updated(UpdateEvent e)
		{
		if (timer == null) timer = new Timer();
		timer.schedule(new UpdateTask(),500);
		}

	private class UpdateTask extends TimerTask
		{
		private int id;

		public UpdateTask()
			{
			synchronized (lastUpdateTaskID)
				{
				id = ++lastUpdateTaskID;
				}
			}

		public void run()
			{
			synchronized (lastUpdateTaskID)
				{
				if (id != lastUpdateTaskID) return;
				}
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
						{
						updateResourceKeywords();
						text.repaint(); //should be capable of figuring out its own visible lines
						//int fl = getFirstLine();
						//painter.invalidateLineRange(fl,fl + getVisibleLines());
						}
				});
			}
		}

	public boolean requestFocusInWindow()
		{
		return text.requestFocusInWindow();
		}

	public void markError(final int line, final int pos, int abs)
		{
		final Highlighter err = new ErrorHighlighter(line,pos);
		text.highlighters.add(err);
		text.addLineChangeListener(new LineChangeListener()
			{
				public void linesChanged(Code code, int start, int end)
					{
					text.highlighters.remove(err);
					text.removeLineChangeListener(this);
					}
			});
		text.repaint();
		}

	class ErrorHighlighter implements Highlighter
		{
		protected final Color COL_SQ = Color.RED;
		protected final Color COL_HL = new Color(255,240,230);
		protected int line, pos, x2;

		public ErrorHighlighter(int line, int pos)
			{
			this.line = line;
			this.pos = pos;
			String code = getLineText(line);
			int otype = JoshText.selGetKind(code,pos);
			x2 = pos;
			do
				x2++;
			while (JoshText.selOfKind(code,x2,otype));
			}

		public void paint(Graphics g, Insets i, CodeMetrics cm, int line_start, int line_end)
			{
			int gh = cm.lineHeight();
			g.setColor(COL_HL);
			g.fillRect(0,i.top + line * gh,g.getClipBounds().width,gh);
			g.setColor(COL_SQ);

			int y = i.top + line * gh + gh;
			int start = i.left + cm.lineWidth(line,pos);
			int end = i.left + cm.lineWidth(line,x2);

			for (int x = start; x < end; x += 2)
				{
				g.drawLine(x,y,x + 1,y - 1);
				g.drawLine(x + 1,y - 1,x + 2,y);
				}
			}
		}
	}
