/*
 * Copyright (C) 2008, 2012 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013-2014 Robert B. Colton
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
import java.awt.print.PrinterException;
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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.lateralgm.file.ProjectFile.ResourceHolder;
import org.lateralgm.file.ResourceList;
import org.lateralgm.joshedit.lexers.GMLKeywords;
import org.lateralgm.joshedit.Code;
import org.lateralgm.joshedit.CompletionMenu;
import org.lateralgm.joshedit.DefaultKeywords;
import org.lateralgm.joshedit.DefaultTokenMarker;
import org.lateralgm.joshedit.CompletionMenu.Completion;
import org.lateralgm.joshedit.DefaultTokenMarker.KeywordSet;
import org.lateralgm.joshedit.DefaultKeywords.HasKeywords;
import org.lateralgm.joshedit.JoshText;
import org.lateralgm.joshedit.JoshText.CodeMetrics;
import org.lateralgm.joshedit.JoshText.LineChangeListener;
import org.lateralgm.joshedit.JoshText.Highlighter;
import org.lateralgm.joshedit.Runner;
import org.lateralgm.joshedit.Runner.EditorInterface;
import org.lateralgm.joshedit.JoshTextPanel;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Script;

public class CodeTextArea extends JoshTextPanel implements UpdateListener,ActionListener
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

	protected static Timer timer;
	protected Integer lastUpdateTaskID = 0;
	private Set<SortedSet<String>> resourceKeywords = new HashSet<SortedSet<String>>();
	protected Completion[] completions;
	protected DefaultTokenMarker tokenMarker;

	private static final Color PURPLE = new Color(138,54,186);
	private static final Color BROWN = new Color(150,0,0);
	private static final Color FUNCTION = new Color(0,100,150);

	//new Color(255,0,128);
	static KeywordSet resNames, scrNames, constructs, functions, operators, constants, variables;

	public CodeTextArea()
		{
		this(null,MarkerCache.getMarker("gml"));
		}

	public CodeTextArea(String code)
		{
		this(code,MarkerCache.getMarker("gml"));
		}

	public CodeTextArea(String code, DefaultTokenMarker marker)
		{
		super(code);

		tokenMarker = marker;

		setTabSize(Prefs.tabSize);
		setTokenMarker(tokenMarker);
		setupKeywords();
		updateKeywords();
		updateResourceKeywords();
		text.setFont(Prefs.codeFont);
		//painter.setStyles(PrefsStore.getSyntaxStyles());
		text.getActionMap().put("COMPLETIONS",completionAction);
		LGM.currentFile.updateSource.addListener(this);

		// build popup menu
		final JPopupMenu popup = new JPopupMenu();
		popup.add(makeContextButton(this.text.actCut));
		popup.add(makeContextButton(this.text.actCopy));
		popup.add(makeContextButton(this.text.actPaste));
		popup.addSeparator();
		final JMenuItem undoItem = makeContextButton(this.text.actUndo);
		popup.add(undoItem);
		final JMenuItem redoItem = makeContextButton(this.text.actRedo);
		popup.add(redoItem);
		popup.addSeparator();
		popup.add(makeContextButton(this.text.actSelAll));

		popup.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0)
				{
				// TODO Auto-generated method stub
				}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0)
				{
				// TODO Auto-generated method stub
				}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0)
				{
					undoItem.setEnabled(text.canUndo());
					redoItem.setEnabled(text.canRedo());
				}

		});

		text.setComponentPopupMenu(popup);
		}

	private JButton makeToolbarButton(String name)
		{
		String key = "JoshText." + name;
		JButton b = new JButton(LGM.getIconForKey(key));
		b.setToolTipText(Messages.getString(key));
		b.setRequestFocusEnabled(false);
		b.setActionCommand(key);
		b.addActionListener(this);
		return b;
		}

	private static JMenuItem makeContextButton(Action a)
		{
		String key = "JoshText." + a.getValue(Action.NAME);
		JMenuItem b = new JMenuItem();
		b.setIcon(LGM.getIconForKey(key));
		b.setText(Messages.getString(key));
		b.setRequestFocusEnabled(false);
		b.addActionListener(a);
		return b;
		}

	public void addEditorButtons(JToolBar tb)
		{
		tb.add(makeToolbarButton("LOAD"));
		tb.add(makeToolbarButton("SAVE"));
		tb.add(makeToolbarButton("PRINT"));

		tb.addSeparator();

		tb.add(makeToolbarButton("CUT"));
		tb.add(makeToolbarButton("COPY"));
		tb.add(makeToolbarButton("PASTE"));

		tb.addSeparator();

		final JButton undoButton = makeToolbarButton("UNDO");
		tb.add(undoButton);
		final JButton redoButton = makeToolbarButton("REDO");
		tb.add(redoButton);
		// need to set the default state unlike the component popup
		undoButton.setEnabled(text.canUndo());
		redoButton.setEnabled(text.canRedo());
		text.addLineChangeListener(new LineChangeListener() {

			@Override
			public void linesChanged(Code code, int start, int end)
				{
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run()
							{
							undoButton.setEnabled(text.canUndo());
							redoButton.setEnabled(text.canRedo());
							}

					});

				}

		});
		tb.addSeparator();
		tb.add(makeToolbarButton("FIND"));
		tb.add(makeToolbarButton("GOTO"));
		}

	public void aGoto()
		{
		int line = showGotoDialog(getCaretLine());
		line = Math.max(0,Math.min(getLineCount() - 1,line));
		setCaretPosition(line,0);
		repaint();
		}

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
		/**/.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/*	*/.addComponent(l)
		/*	*/.addComponent(f))
		/**/.addComponent(b));

		d.setContentPane(p);
		d.pack();
		d.setResizable(false);
		d.setLocationRelativeTo(null);
		d.setVisible(true); //blocks until user clicks OK

		return f.getIntValue();
		}

	private void setupKeywords()
		{
		resNames = tokenMarker.addKeywordSet("Resource Names",PURPLE,Font.PLAIN);
		scrNames = tokenMarker.addKeywordSet("Script Names",FUNCTION,Font.PLAIN);
		functions = tokenMarker.addKeywordSet("Functions",FUNCTION,Font.PLAIN);
		constructs = tokenMarker.addKeywordSet("Constructs",Color.BLACK,Font.BOLD);
		operators = tokenMarker.addKeywordSet("Operators",Color.BLACK,Font.BOLD);
		constants = tokenMarker.addKeywordSet("Constants",BROWN,Font.PLAIN);
		variables = tokenMarker.addKeywordSet("Variables",Color.BLUE,Font.ITALIC);
		}

	//TODO: I believe this method can be removed.
	public static void updateKeywords()
		{
		constructs.words.clear();
		operators.words.clear();
		constants.words.clear();
		variables.words.clear();
		functions.words.clear();

		for (DefaultKeywords.Construct keyword : GMLKeywords.CONSTRUCTS)
			constructs.words.add(keyword.getName());
		for (DefaultKeywords.Operator keyword : GMLKeywords.OPERATORS)
			operators.words.add(keyword.getName());
		for (DefaultKeywords.Constant keyword : GMLKeywords.CONSTANTS)
			constants.words.add(keyword.getName());
		for (DefaultKeywords.Variable keyword : GMLKeywords.VARIABLES)
			variables.words.add(keyword.getName());
		for (DefaultKeywords.Function keyword : GMLKeywords.FUNCTIONS)
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

	protected void updateCompletions(DefaultTokenMarker tokenMarker2)
		{
		int l = 0;
		for (Set<String> a : resourceKeywords)
			{
			l += a.size();
			}
		DefaultKeywords.Keyword[][] keywords = null;
		if (tokenMarker2 instanceof HasKeywords) {
			HasKeywords hk = (HasKeywords) tokenMarker2;
			keywords = hk.getKeywords();
			for (DefaultKeywords.Keyword[] a : keywords)
				l += a.length;
		}

		completions = new Completion[l];
		int i = 0;
		for (Set<String> a : resourceKeywords)
			{
			for (String s : a)
				{
				completions[i] = new CompletionMenu.WordCompletion(s);
				i += 1;
				}
			}

		if (keywords == null) return;
		for (DefaultKeywords.Keyword[] a : keywords)
			for (DefaultKeywords.Keyword k : a)
				{
				if (k instanceof DefaultKeywords.Function)
					completions[i] = new FunctionCompletion((DefaultKeywords.Function) k);
				else if (k instanceof DefaultKeywords.Variable)
					completions[i] = new VariableCompletion((DefaultKeywords.Variable) k);
				else
					completions[i] = new CompletionMenu.WordCompletion(k.getName());
				i++;
				}
		}

	public class VariableCompletion extends CompletionMenu.Completion
		{
		private final DefaultKeywords.Variable variable;

		public VariableCompletion(DefaultKeywords.Variable v)
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
		private final DefaultKeywords.Function function;

		public FunctionCompletion(DefaultKeywords.Function f)
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
				if (completions == null) updateCompletions(tokenMarker);
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

	public void setTokenMarker(DefaultTokenMarker tokenMarker2)
		{
		tokenMarker = tokenMarker2;
		super.setTokenMarker(tokenMarker2);
		this.updateCompletions(tokenMarker2);
		}

	public void actionPerformed(ActionEvent ev)
		{
		String com = ev.getActionCommand();
		if (com.equals("JoshText.LOAD"))
			{
			text.Load();
			}
		else if (com.equals("JoshText.SAVE"))
			{
			text.Save();
			}
		else if (com.equals("JoshText.PRINT"))
			{
			try
				{
				this.Print();
				}
			catch (PrinterException e)
				{
				LGM.showDefaultExceptionHandler(e);
				}
			}
		else if (com.equals("JoshText.UNDO"))
			{
			text.Undo();
			}
		else if (com.equals("JoshText.REDO"))
			{
			text.Redo();
			}
		else if (com.equals("JoshText.CUT"))
			{
			text.Cut();
			}
		else if (com.equals("JoshText.COPY"))
			{
			text.Copy();
			}
		else if (com.equals("JoshText.PASTE"))
			{
			text.Paste();
			}
		else if (com.equals("JoshText.FIND"))
			{
			text.ShowFind();
			}
		else if (com.equals("JoshText.GOTO"))
			{
			this.aGoto();
			}
		else if (com.equals("JoshText.SELALL"))
			{
			text.SelectAll();
			}
		}
	}
