package org.lateralgm.main;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.ActionList;
import org.lateralgm.components.CodeTextArea;
import org.lateralgm.components.CustomJToolBar;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.visual.HintTextField;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Shader;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.resources.sub.Moment;
import org.lateralgm.resources.sub.Instance.PInstance;
import org.lateralgm.subframes.ActionFrame;
import org.lateralgm.subframes.CodeFrame;
import org.lateralgm.subframes.GmObjectFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.RoomFrame;
import org.lateralgm.subframes.ScriptFrame;
import org.lateralgm.subframes.ShaderFrame;
import org.lateralgm.subframes.TimelineFrame;

public class Search
	{
	static JTextField filterText;
	private static JCheckBox matchCaseCB, regexCB;
	static JCheckBox wholeWordCB, pruneResultsCB;
	private static JButton closeButton;

	private Search()
		{
		}

	public static class InvisibleTreeModel extends DefaultTreeModel
		{
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = -7759731037386264163L;

		protected boolean filterIsActive;

		public InvisibleTreeModel(TreeNode root)
			{
			this(root,false);
			}

		public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren)
			{
			this(root,false,false);
			}

		public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren, boolean filterIsActive)
			{
			super(root,asksAllowsChildren);
			this.filterIsActive = filterIsActive;
			}

		public void activateFilter(boolean newValue)
			{
			filterIsActive = newValue;
			}

		public boolean isActivatedFilter()
			{
			return filterIsActive;
			}

		public Object getChild(Object parent, int index)
			{
			if (filterIsActive)
				{
				if (parent instanceof ResNode)
					{
					return ((ResNode) parent).getChildAt(index,filterIsActive);
					}
				}
			return ((TreeNode) parent).getChildAt(index);
			}

		public int getChildCount(Object parent)
			{
			if (filterIsActive)
				{
				if (parent instanceof ResNode)
					{
					return ((ResNode) parent).getChildCount(filterIsActive);
					}
				}
			return ((TreeNode) parent).getChildCount();
			}
		}

	private static boolean expressionMatch(String token, String expression, boolean matchCase,
			boolean wholeWord)
		{
		if (!matchCase)
			{
			token = token.toLowerCase();
			expression = expression.toLowerCase();
			}
		if (wholeWord)
			{
			return token.equals(expression);
			}
		else
			{
			//if (expression.length() == 0) { return false; } // without this all of your folders will be open by default, we don't want to
			// check matches with an empty string - don't touch this as everything works so just leave it here in case I come back to it - Robert B. Colton
			return token.contains(expression);
			}
		}

	public static DefaultMutableTreeNode applyFilterRecursion(Vector<ResNode> children,
			boolean filter, String expression, boolean matchCase, boolean wholeWord)
		{
		if (children == null)
			{
			return null;
			}
		DefaultMutableTreeNode firstResult = null;
		for (ResNode child : children)
			{
			boolean match = expressionMatch(child.toString(),expression,matchCase,wholeWord);
			if (firstResult == null && match)
				{
				firstResult = child;
				}
			DefaultMutableTreeNode childResult = applyFilterRecursion(child.getChildren(),filter,
					expression,matchCase,wholeWord);
			if (firstResult == null && childResult != null)
				{
				//if (childResult != null) {
				firstResult = childResult;
				}
			if (childResult != null || match)
				{
				child.setVisible(true);
				}
			else
				{
				child.setVisible(false);
				}
			}
		return firstResult;
		}

	public static boolean applyFilter(Vector<ResNode> children, boolean filter, String expression,
			boolean matchCase, boolean wholeWord, boolean selectFirst)
		{
		if (children == null)
			{
			return false;
			}
		DefaultMutableTreeNode firstResult = applyFilterRecursion(children,filter,expression,matchCase,
				wholeWord);

		if (firstResult != null && selectFirst)
			{
			LGM.tree.setSelectionPath(new TreePath(firstResult.getPath()));
			LGM.tree.updateUI();
			return true;
			}
		LGM.tree.updateUI();
		return false;
		}

	public static boolean searchFilter(ResNode child, String expression, boolean matchCase,
			boolean wholeWord, boolean backwards)
		{
		ResNode firstResult = null;
		while (child != null)
			{
			if (backwards)
				{
				child = (ResNode) child.getPreviousNode();
				}
			else
				{
				child = (ResNode) child.getNextNode();
				}
			if (child == null) break;
			boolean match = expressionMatch(child.toString(),expression,matchCase,wholeWord);
			if (firstResult == null && match)
				{
				firstResult = child;
				break;
				}
			}
		if (firstResult != null)
			{
			LGM.tree.setSelectionPath(new TreePath(firstResult.getPath()));
			LGM.tree.updateUI();
			//tree.expandPath(new TreePath(firstResult.getPath()));
			return true;
			}
		return false;
		}

	public static class MatchBlock
		{
		public String content;
		public boolean highlighted;

		MatchBlock(String content, boolean highlighted)
			{
			this.content = content;
			this.highlighted = highlighted;
			}
		}

	public static class LineMatch
		{
		public int lineNum;
		public List<MatchBlock> matchedText = new ArrayList<MatchBlock>();

		public String toHighlightableString()
			{
			boolean enablehtml = Prefs.highlightResultMatchBackground
					|| Prefs.highlightResultMatchForeground;
			String text = (enablehtml ? "<html>" : "") + lineNum + ": ";
			for (MatchBlock block : matchedText)
				{
				if (block.highlighted && enablehtml)
					{
					text += "<span";
					if (Prefs.highlightResultMatchBackground)
						{
						text += " bgcolor='" + Util.getHTMLColor(Prefs.resultMatchBackgroundColor,false) + "'";
						}
					if (Prefs.highlightResultMatchForeground)
						{
						text += " color='" + Util.getHTMLColor(Prefs.resultMatchForegroundColor,false) + "'";
						}
					text += ">";
					}
				text += block.content;
				if (block.highlighted && enablehtml)
					{
					text += "</span>";
					}
				}
			if (enablehtml)
				{
				text += "</html>";
				}
			return text;
			}
		}

	private static String formatMatchCountText(String pretext, int matches)
		{
		boolean enablehtml = Prefs.highlightMatchCountBackground || Prefs.highlightMatchCountForeground;
		String text = (enablehtml ? "<html>" : "") + pretext + " " + (enablehtml ? "<font" : "");
		if (Prefs.highlightMatchCountBackground)
			{
			text += " bgcolor='" + Util.getHTMLColor(Prefs.matchCountBackgroundColor,false) + "'";
			}
		if (Prefs.highlightMatchCountForeground)
			{
			text += " color='" + Util.getHTMLColor(Prefs.matchCountForegroundColor,false) + "'";
			}
		text += (enablehtml ? ">" : "") + "(" + matches + " " + Messages.getString("TreeFilter.MATCHES")
				+ ")" + (enablehtml ? "</font></html>" : "");

		return text;
		}

	private static final Pattern NEWLINE = Pattern.compile("\r\n|\r|\n");

	static List<LineMatch> getMatchingLines(String code, Pattern content)
		{
		List<LineMatch> res = new ArrayList<LineMatch>();
		Matcher m = content.matcher(code), nl = NEWLINE.matcher(code);
		// code editor starts at line 0 so we need to here as well
		int lineNum = 0, lineAt = 0, lastEnd = -1;
		LineMatch lastMatch = null;
		while (m.find())
			{
			nl.region(lineAt,m.start());
			int firstSkippedLineAt = lineAt;
			if (nl.find())
				{
				firstSkippedLineAt = nl.start();
				lineAt = nl.end();
				++lineNum;
				while (nl.find())
					{
					++lineNum;
					lineAt = nl.end();
					}
				}
			if (lastMatch != null)
				{
				// We have to add the rest of the line to the old match, either way.
				// And if we're matching on the same line, we add that match, too.
				if (lineNum == lastMatch.lineNum)
					{
					lastMatch.matchedText.add(new MatchBlock(code.substring(lastEnd,m.start()),false));
					lastMatch.matchedText.add(new MatchBlock(code.substring(m.start(),m.end()),true));
					}
				else
					{
					lastMatch.matchedText.add(
							new MatchBlock(code.substring(lastEnd,firstSkippedLineAt),false));
					}
				}
			if (lastMatch == null || lineNum != lastMatch.lineNum)
				{
				lastMatch = new LineMatch();
				lastMatch.lineNum = lineNum;
				if (m.start() > lineAt)
					{
					lastMatch.matchedText.add(new MatchBlock(code.substring(lineAt,m.start()),false));
					}
				lastMatch.matchedText.add(new MatchBlock(code.substring(m.start(),m.end()),true));
				res.add(lastMatch);
				}
			lastEnd = m.end();
			}
		if (lastMatch != null)
			{
			nl.region(lastEnd,code.length());
			int indTo = (nl.find()) ? nl.start() : code.length();
			lastMatch.matchedText.add(new MatchBlock(code.substring(lastEnd,indTo),false));
			}
		return res;
		}

	/*
	public static void assertEquals(Object obj1, Object obj2) {
		if (obj1.equals(obj2)) {
			Debug.println("assertEquals: ",obj1.toString() + "," + obj2.toString());
		} else {
			Debug.println("assertEquals: ","false");
		}
	}
	
	public static void testThing() {
	        String CODE = "runatestinatestwith\nsomemoretestsandthen\nyou'redone";
	        List<LineMatch> match  = getMatchingLines(CODE, Pattern.compile("test"));
	        LineMatch[] matches = (LineMatch[]) match.toArray(new LineMatch[match.size()]);
	        assertEquals(2, matches.length);
	        assertEquals(5, matches[0].matchedText.size());
	        assertEquals("runa",     matches[0].matchedText.get(0).content);
	        assertEquals("test",     matches[0].matchedText.get(1).content);
	        assertEquals("ina",      matches[0].matchedText.get(2).content);
	        assertEquals("test",     matches[0].matchedText.get(3).content);
	        assertEquals("with",     matches[0].matchedText.get(4).content);
	        assertEquals(3, matches[1].matchedText.size());
	        assertEquals("somemore", matches[1].matchedText.get(0).content);
	        assertEquals("test",     matches[1].matchedText.get(1).content);
	        assertEquals("sandthen", matches[1].matchedText.get(2).content);
	}
	*/

	public static void buildSearchHierarchy(ResNode resNode, SearchResultNode resultRoot)
		{
		DefaultMutableTreeNode searchNode = (DefaultMutableTreeNode) LGM.searchTree.getModel().getRoot();
		if (resNode == null)
			{
			searchNode.add(resultRoot);
			return;
			}
		TreeNode[] paths = resNode.getPath();
		// start at 1 because we don't want to copy the root
		// subtract 1 so we don't consider the node itself
		for (int n = 1; n < paths.length - 1; n++)
			{
			ResNode pathNode = (ResNode) paths[n];
			boolean found = false;
			for (int y = 0; y < searchNode.getChildCount(); y++)
				{
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) searchNode.getChildAt(y);
				if (childNode.getUserObject() == pathNode.getUserObject())
					{
					searchNode = childNode;
					found = true;
					break;
					}
				}
			if (!found)
				{
				SearchResultNode newSearchNode = new SearchResultNode(pathNode.getUserObject());
				newSearchNode.status = pathNode.status;
				searchNode.add(newSearchNode);
				searchNode = newSearchNode;
				}
			if (pathNode == resNode.getParent())
				{
				searchNode.insert(resultRoot,searchNode.getChildCount() + resNode.getDepth());
				}
			}
		}

	public static void searchInResourcesRecursion(DefaultMutableTreeNode node, Pattern pattern)
		{
		int numChildren = node.getChildCount();
		for (int i = 0; i < numChildren; ++i)
			{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			if (child instanceof ResNode)
				{
				ResNode resNode = (ResNode) child;
				if (resNode.status != ResNode.STATUS_SECONDARY)
					{
					searchInResourcesRecursion(child,pattern);
					}
				else
					{
					SearchResultNode resultRoot = null;
					ResourceReference<?> ref = resNode.getRes();

					if (ref != null)
						{
						Resource<?,?> resderef = ref.get();
						if (resNode.frame != null)
							{
							resNode.frame.commitChanges();
							resderef = resNode.frame.res;
							}

						if (resNode.kind == Script.class)
							{
							Script res = (Script) resderef;
							String code = res.getCode();
							List<LineMatch> matches = getMatchingLines(code,pattern);
							if (matches.size() > 0)
								{
								resultRoot = new SearchResultNode(
										formatMatchCountText(res.getName(),matches.size()));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
								for (LineMatch match : matches)
									{
									if (match.matchedText.size() > 0)
										{
										String text = match.toHighlightableString();

										SearchResultNode resultNode = new SearchResultNode(text);
										resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
										resultNode.status = SearchResultNode.STATUS_RESULT;
										resultNode.data = new Object[] { match.lineNum };
										resultRoot.add(resultNode);
										}
									}
								}
							}
						else if (resNode.kind == Shader.class)
							{
							Shader res = (Shader) resderef;
							String vcode = res.getVertexCode();
							String fcode = res.getFragmentCode();
							List<LineMatch> vertexmatches = getMatchingLines(vcode,pattern);
							List<LineMatch> fragmentmatches = getMatchingLines(fcode,pattern);
							if (vertexmatches.size() + fragmentmatches.size() > 0)
								{
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(),
										vertexmatches.size() + fragmentmatches.size()));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());

								SearchResultNode resultGroupNode = new SearchResultNode(formatMatchCountText(
										Messages.getString("TreeFilter.VERTEX_CODE") + ":",vertexmatches.size()));
								resultGroupNode.status = SearchResultNode.STATUS_VERTEX_CODE;
								resultRoot.add(resultGroupNode);
								for (LineMatch match : vertexmatches)
									{
									if (match.matchedText.size() > 0)
										{
										String text = match.toHighlightableString();

										SearchResultNode resultNode = new SearchResultNode(text);
										resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
										resultNode.status = SearchResultNode.STATUS_RESULT;
										resultNode.data = new Object[] { match.lineNum };
										resultGroupNode.add(resultNode);
										}
									}

								resultGroupNode = new SearchResultNode(formatMatchCountText(
										Messages.getString("TreeFilter.FRAGMENT_CODE") + ":",fragmentmatches.size()));
								resultGroupNode.status = SearchResultNode.STATUS_FRAGMENT_CODE;
								resultRoot.add(resultGroupNode);
								for (LineMatch match : fragmentmatches)
									{
									if (match.matchedText.size() > 0)
										{
										String text = match.toHighlightableString();

										SearchResultNode resultNode = new SearchResultNode(text);
										resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
										resultNode.status = SearchResultNode.STATUS_RESULT;
										resultNode.data = new Object[] { match.lineNum };
										resultGroupNode.add(resultNode);
										}
									}
								}
							}
						else if (resNode.kind == GmObject.class)
							{
							GmObject res = (GmObject) resderef;

							ArrayList<SearchResultNode> meNodes = new ArrayList<>();
							int matchCount = 0;
							for (MainEvent me : res.mainEvents)
								{
								ArrayList<SearchResultNode> evNodes = new ArrayList<>();
								int meMatches = 0;
								int mainid = 0;
								for (Event ev : me.events)
									{
									mainid = ev.mainId;
									ArrayList<SearchResultNode> actionNodes = new ArrayList<>();
									int evMatches = 0;
									List<org.lateralgm.resources.sub.Action> actions = ev.actions;
									for (int ii = 0; ii < actions.size(); ii++)
										{
										org.lateralgm.resources.sub.Action act = actions.get(ii);
										ArrayList<SearchResultNode> resultNodes = new ArrayList<>();
										int actMatches = 0;
										List<Argument> args = act.getArguments();
										for (int iii = 0; iii < args.size(); iii++)
											{
											Argument arg = args.get(iii);
											String code = arg.getVal();
											ResourceReference<? extends Resource<?,?>> aref = arg.getRes();
											if (aref != null)
												{
												Resource<?,?> ares = aref.get();
												code = ares.getName();
												}
											List<LineMatch> matches = getMatchingLines(code,pattern);
											actMatches += matches.size();
											for (LineMatch match : matches)
												{
												if (match.matchedText.size() > 0)
													{
													String text = match.toHighlightableString();

													SearchResultNode resultNode = null;
													if (act.getLibAction().actionKind != org.lateralgm.resources.sub.Action.ACT_CODE)
														{
														boolean enablehtml = Prefs.highlightResultMatchBackground
																|| Prefs.highlightResultMatchForeground;
														text = ((enablehtml ? "<html>" : "") + Integer.toString(iii)
																+ text.substring(text.indexOf(":"),text.length()));
														resultNode = new SearchResultNode(text);
														resultNode.data = new Object[] { iii };
														}
													else
														{
														resultNode = new SearchResultNode(text);
														resultNode.data = new Object[] { match.lineNum };
														}

													resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
													resultNode.status = SearchResultNode.STATUS_RESULT;
													resultNodes.add(resultNode);
													}
												}
											}
										evMatches += actMatches;
										if (resultNodes.size() > 0)
											{
											// Uses the same method of getting the Action name as ActionFrame
											SearchResultNode actRoot = new SearchResultNode(formatMatchCountText(
													act.getLibAction().name.replace("_"," "),actMatches));
											actRoot.status = SearchResultNode.STATUS_ACTION;
											actRoot.data = new Object[] { ii };
											actRoot.setIcon(
													new ImageIcon(act.getLibAction().actImage.getScaledInstance(16,16,0)));
											for (SearchResultNode actn : resultNodes)
												actRoot.add(actn);
											actionNodes.add(actRoot);
											}
										}
									meMatches += evMatches;
									if (actionNodes.size() > 0)
										{
										SearchResultNode evRoot = new SearchResultNode(formatMatchCountText(
												ev.toString().replaceAll("<","&lt;").replaceAll(">","&gt;"),evMatches));
										evRoot.status = SearchResultNode.STATUS_EVENT;
										evRoot.setIcon(LGM.getIconForKey("EventNode.EVENT" + ev.mainId));
										evRoot.data = new Object[] { ev.mainId,ev.id };
										for (SearchResultNode resn : actionNodes)
											evRoot.add(resn);
										evNodes.add(evRoot);
										}
									}
								matchCount += meMatches;
								if (evNodes.size() > 0)
									{
									if (evNodes.size() > 1)
										{
										SearchResultNode meRoot = new SearchResultNode(formatMatchCountText(
												Messages.getString("MainEvent.EVENT" + mainid),meMatches));
										meRoot.status = SearchResultNode.STATUS_MAIN_EVENT;
										meRoot.setIcon(LGM.getIconForKey("EventNode.GROUP" + mainid));
										for (SearchResultNode resn : evNodes)
											{
											meRoot.add(resn);
											}
										meNodes.add(meRoot);
										}
									else
										{
										for (SearchResultNode resn : evNodes)
											{
											meNodes.add(resn);
											}
										}
									}
								}

							if (meNodes.size() > 0)
								{
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(),matchCount));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
								}

							for (SearchResultNode resn : meNodes)
								resultRoot.add(resn);
							}
						else if (resNode.kind == Timeline.class)
							{
							Timeline res = (Timeline) resderef;

							ArrayList<SearchResultNode> momentNodes = new ArrayList<>();
							int matchCount = 0;
							for (Moment mom : res.moments)
								{
								ArrayList<SearchResultNode> actionNodes = new ArrayList<>();
								int momentMatches = 0;
								List<org.lateralgm.resources.sub.Action> actions = mom.actions;
								for (int ii = 0; ii < actions.size(); ii++)
									{
									org.lateralgm.resources.sub.Action act = actions.get(ii);
									ArrayList<SearchResultNode> resultNodes = new ArrayList<>();
									int actMatches = 0;
									List<Argument> args = act.getArguments();
									for (int iii = 0; iii < args.size(); iii++)
										{
										Argument arg = args.get(iii);
										String code = arg.getVal();
										ResourceReference<? extends Resource<?,?>> aref = arg.getRes();
										if (aref != null)
											{
											Resource<?,?> ares = aref.get();
											code = ares.getName();
											}
										List<LineMatch> matches = getMatchingLines(code,pattern);
										actMatches += matches.size();
										for (LineMatch match : matches)
											{
											if (match.matchedText.size() > 0)
												{
												String text = match.toHighlightableString();

												SearchResultNode resultNode = null;
												if (act.getLibAction().actionKind != org.lateralgm.resources.sub.Action.ACT_CODE)
													{
													boolean enablehtml = Prefs.highlightResultMatchBackground
															|| Prefs.highlightResultMatchForeground;
													text = ((enablehtml ? "<html>" : "") + Integer.toString(iii)
															+ text.substring(text.indexOf(":"),text.length()));
													resultNode = new SearchResultNode(text);
													resultNode.data = new Object[] { iii };
													}
												else
													{
													resultNode = new SearchResultNode(text);
													resultNode.data = new Object[] { match.lineNum };
													}

												resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
												resultNode.status = SearchResultNode.STATUS_RESULT;
												resultNodes.add(resultNode);
												}
											}
										}

									momentMatches += actMatches;
									if (resultNodes.size() > 0)
										{
										// Uses the same method of getting the Action name as ActionFrame
										SearchResultNode actRoot = new SearchResultNode(
												formatMatchCountText(act.getLibAction().name.replace("_"," "),actMatches));
										actRoot.status = SearchResultNode.STATUS_ACTION;
										actRoot.data = new Object[] { ii };
										actRoot.setIcon(
												new ImageIcon(act.getLibAction().actImage.getScaledInstance(16,16,0)));
										for (SearchResultNode actn : resultNodes)
											actRoot.add(actn);
										actionNodes.add(actRoot);
										}

									}
								matchCount += momentMatches;
								if (actionNodes.size() > 0)
									{
									SearchResultNode momentRoot = new SearchResultNode(
											formatMatchCountText(mom.toString(),momentMatches));
									momentRoot.status = SearchResultNode.STATUS_MOMENT;
									momentRoot.data = new Object[] { mom.stepNo };
									momentRoot.setIcon(null);
									for (SearchResultNode resn : actionNodes)
										momentRoot.add(resn);
									momentNodes.add(momentRoot);
									}
								}

							if (momentNodes.size() > 0)
								{
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(),matchCount));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
								}

							for (SearchResultNode momn : momentNodes)
								resultRoot.add(momn);
							}
						else if (resNode.kind == Room.class)
							{
							Room res = (Room) resderef;

							ArrayList<SearchResultNode> resultNodes = new ArrayList<>();
							int matchCount = 0;

							String code = res.getCode();
							List<LineMatch> matches = getMatchingLines(code,pattern);
							matchCount += matches.size();
							ArrayList<SearchResultNode> codeNodes = new ArrayList<>();
							for (LineMatch match : matches)
								{
								if (match.matchedText.size() > 0)
									{
									String text = match.toHighlightableString();

									SearchResultNode resultNode = new SearchResultNode(text);
									resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
									resultNode.status = SearchResultNode.STATUS_RESULT;
									resultNode.data = new Object[] { match.lineNum };
									codeNodes.add(resultNode);
									}
								}

							if (codeNodes.size() > 0)
								{
								SearchResultNode resultNode = new SearchResultNode(formatMatchCountText(
										Messages.getString("TreeFilter.CREATION_CODE"),matches.size()));
								resultNode.setIcon(null);
								resultNode.status = SearchResultNode.STATUS_ROOM_CREATION;
								resultNodes.add(resultNode);

								for (SearchResultNode codeNode : codeNodes)
									resultNode.add(codeNode);
								}

							for (Instance inst : res.instances)
								{
								code = inst.getCode();
								matches = getMatchingLines(code,pattern);
								matchCount += matches.size();
								codeNodes = new ArrayList<>();
								for (LineMatch match : matches)
									{
									if (match.matchedText.size() > 0)
										{
										String text = match.toHighlightableString();

										SearchResultNode resultNode = new SearchResultNode(text);
										resultNode.setIcon(LGM.getIconForKey("TreeFilter.RESULT"));
										resultNode.data = new Object[] { match.lineNum };
										resultNode.status = SearchResultNode.STATUS_RESULT;
										codeNodes.add(resultNode);
										}
									}

								if (codeNodes.size() > 0)
									{
									SearchResultNode resultNode = new SearchResultNode(formatMatchCountText(
											Messages.getString("TreeFilter.INSTANCE") + " " + inst.getID(),
											matches.size()));

									Resource<?,?> obj = ((ResourceReference<?>) inst.properties.get(
											PInstance.OBJECT)).get();
									resultNode.setIcon(obj == null ? null : obj.getNode().getIcon());
									resultNode.status = SearchResultNode.STATUS_INSTANCE_CREATION;
									resultNode.data = new Object[] { inst.getID() };
									resultNodes.add(resultNode);

									for (SearchResultNode codeNode : codeNodes)
										resultNode.add(codeNode);
									}
								}

							if (resultNodes.size() > 0)
								{
								resultRoot = new SearchResultNode(formatMatchCountText(res.getName(),matchCount));
								resultRoot.ref = res.reference;
								resultRoot.status = ResNode.STATUS_SECONDARY;
								resultRoot.setIcon(res.getNode().getIcon());
								}

							for (SearchResultNode resultNode : resultNodes)
								resultRoot.add(resultNode);
							}

						if (resultRoot != null)
							{
							TreeNode[] paths = resNode.getPath();
							DefaultMutableTreeNode searchNode = (DefaultMutableTreeNode) LGM.searchTree.getModel().getRoot();
							// start at 1 because we don't want to copy the root
							// subtract 1 so we don't consider the node itself
							for (int n = 1; n < paths.length - 1; n++)
								{
								ResNode pathNode = (ResNode) paths[n];
								boolean found = false;
								for (int y = 0; y < searchNode.getChildCount(); y++)
									{
									DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) searchNode.getChildAt(
											y);
									if (childNode.getUserObject() == pathNode.getUserObject())
										{
										searchNode = childNode;
										found = true;
										break;
										}
									}
								if (!found)
									{
									SearchResultNode newSearchNode = new SearchResultNode(pathNode.getUserObject());
									newSearchNode.status = pathNode.status;
									searchNode.add(newSearchNode);
									searchNode = newSearchNode;
									}
								if (pathNode == resNode.getParent()) searchNode.add(resultRoot);
								}

							}
						}
					}
				}
			}
		}

	public static void searchInResources(DefaultMutableTreeNode node, String expression,
			boolean regex, boolean matchCase, boolean wholeWord)
		{
		DefaultMutableTreeNode searchRoot = (DefaultMutableTreeNode) LGM.searchTree.getModel().getRoot();
		searchRoot.removeAllChildren();
		Pattern pattern = Pattern.compile(
				wholeWord ? "\\b" + Pattern.quote(expression) + "\\b"
						: regex ? expression : Pattern.quote(expression),
				matchCase ? 0 : Pattern.CASE_INSENSITIVE);
		searchInResourcesRecursion(node,pattern);
		// Reload because root is invisible.
		((DefaultTreeModel) LGM.searchTree.getModel()).reload();
		}

	static class SearchResultNode extends DefaultMutableTreeNode
		{
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = -8827316922729826331L;

		public static final byte STATUS_RESULT = 4;
		public static final byte STATUS_MAIN_EVENT = 5;
		public static final byte STATUS_EVENT = 6;
		public static final byte STATUS_MOMENT = 7;
		public static final byte STATUS_VERTEX_CODE = 8;
		public static final byte STATUS_FRAGMENT_CODE = 9;
		public static final byte STATUS_ACTION = 10;
		public static final byte STATUS_ROOM_CREATION = 11;
		public static final byte STATUS_INSTANCE_CREATION = 12;

		public byte status;
		ResourceReference<?> ref;
		private Icon icon = null;

		Object[] data;
		Object[] parentdata;

		public SearchResultNode()
			{
			super();
			}

		public SearchResultNode(Object text)
			{
			super(text);
			}

		public void setIcon(Icon ico)
			{
			icon = ico;
			}

		public ResourceReference<?> getRef()
			{
			SearchResultNode par = (SearchResultNode) this.getParent();
			ResourceReference<?> ret = par.ref;
			while (ret == null)
				{
				par = (SearchResultNode) par.getParent();
				ret = par.ref;
				}
			return ret;
			}

		public void threadCaretUpdate(final CodeTextArea code, int row, int col)
			{
			SwingUtilities.invokeLater(new Runnable()
				{
				@Override
				public void run()
					{
					code.setCaretPosition((Integer) data[0],0);
					code.text.centerCaret();
					code.repaint();
					}
				});
			}

		public void openFrame()
			{
			if (status >= STATUS_RESULT)
				{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getParent();
				if (node instanceof SearchResultNode)
					{
					SearchResultNode parNode = (SearchResultNode) node;
					parNode.openFrame();

					ResourceReference<?> parentRef = this.getRef();
					if (parentRef != null)
						{
						Resource<?,?> res = parentRef.get();
						if (res != null)
							{
							ResNode resNode = res.getNode();
							if (resNode != null)
								{
								ResourceFrame<?,?> frame = resNode.frame;
								if (frame != null)
									{
									if (resNode.kind == GmObject.class)
										{
										GmObjectFrame objframe = (GmObjectFrame) frame;
										if (status == STATUS_EVENT)
											{
											objframe.setSelectedEvent((Integer) data[0],(Integer) data[1]);
											}
										else if (status == STATUS_ACTION)
											{
											//TODO: There is a bug here where if the user deletes the action
											//and then selects it again from the search results tree it may open
											//and in fact scope a different action in the list because we have
											//no unique reference to the action we want to open, and we can't
											//because the frame may have been closed. May possibly lead to an NPE
											objframe.actions.setSelectedIndex((Integer) data[0]);
											org.lateralgm.resources.sub.Action act = objframe.actions.getSelectedValue();
											if (act != null)
												{
												ActionFrame af = (ActionFrame) ActionList.openActionFrame(objframe,act);
												parentdata = new Object[] { af };
												}
											}
										else if (status == STATUS_RESULT && parNode.status == STATUS_ACTION)
											{
											ActionFrame af = (ActionFrame) parNode.parentdata[0];
											org.lateralgm.resources.sub.Action act = af.getAction();
											if (act.getLibAction().actionKind != org.lateralgm.resources.sub.Action.ACT_CODE)
												{
												af.focusArgumentComponent((Integer) data[0]);
												}
											else
												{
												if ((Integer) data[0] < af.code.getLineCount())
													threadCaretUpdate(af.code,(Integer) data[0],0);
												}
											}
										}
									else if (resNode.kind == Timeline.class)
										{
										TimelineFrame tmlframe = (TimelineFrame) frame;
										if (status == STATUS_MOMENT)
											{
											tmlframe.setSelectedMoment((Integer) data[0]);
											}
										else if (status == STATUS_ACTION)
											{
											tmlframe.actions.setSelectedIndex((Integer) data[0]);
											org.lateralgm.resources.sub.Action act = tmlframe.actions.getSelectedValue();
											if (act != null)
												{
												ActionFrame af = (ActionFrame) ActionList.openActionFrame(tmlframe,act);
												parentdata = new Object[] { af };
												}
											}
										else if (status == STATUS_RESULT && parNode.status == STATUS_ACTION)
											{
											ActionFrame af = (ActionFrame) parNode.parentdata[0];
											org.lateralgm.resources.sub.Action act = af.getAction();
											if (act.getLibAction().actionKind != org.lateralgm.resources.sub.Action.ACT_CODE)
												{
												af.focusArgumentComponent((Integer) data[0]);
												}
											else
												{
												if ((Integer) data[0] < af.code.getLineCount())
													threadCaretUpdate(af.code,(Integer) data[0],0);
												}
											}
										}
									else if (resNode.kind == Room.class)
										{
										RoomFrame roomframe = (RoomFrame) frame;
										if (status == STATUS_INSTANCE_CREATION)
											{
											roomframe.tabs.setSelectedIndex(0);
											parentdata = new Object[] {
													roomframe.openInstanceCodeFrame((Integer) data[0],true) };
											}
										else if (status == STATUS_ROOM_CREATION)
											{
											roomframe.tabs.setSelectedIndex(1);
											parentdata = new Object[] { roomframe.openRoomCreationCode() };
											}
										else if (status == STATUS_RESULT)
											{
											if (parNode.status == STATUS_INSTANCE_CREATION
													|| parNode.status == STATUS_ROOM_CREATION)
												{
												CodeFrame cf = (CodeFrame) parNode.parentdata[0];
												if ((Integer) data[0] < cf.code.getLineCount())
													threadCaretUpdate(cf.code,(Integer) data[0],0);
												}
											}
										}
									else if (resNode.kind == Script.class)
										{
										final ScriptFrame scrframe = (ScriptFrame) frame;
										if (status == STATUS_RESULT)
											{
											if ((Integer) data[0] < scrframe.code.text.getLineCount())
												threadCaretUpdate(scrframe.code,(Integer) data[0],0);
											}
										}
									else if (resNode.kind == Shader.class)
										{
										ShaderFrame shrframe = (ShaderFrame) frame;
										if (status == STATUS_VERTEX_CODE)
											{
											shrframe.editors.setSelectedIndex(0);
											}
										else if (status == STATUS_FRAGMENT_CODE)
											{
											shrframe.editors.setSelectedIndex(1);
											}
										else if (status == STATUS_RESULT)
											{
											if (parNode.status == STATUS_VERTEX_CODE)
												{
												shrframe.vcode.requestFocusInWindow();
												if ((Integer) data[0] < shrframe.vcode.text.getLineCount())
													threadCaretUpdate(shrframe.vcode,(Integer) data[0],0);
												}
											else if (parNode.status == STATUS_FRAGMENT_CODE)
												{
												shrframe.fcode.requestFocusInWindow();
												if ((Integer) data[0] < shrframe.fcode.text.getLineCount())
													threadCaretUpdate(shrframe.fcode,(Integer) data[0],0);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			else if (status == ResNode.STATUS_SECONDARY)
				{
				if (ref != null)
					{
					Resource<?,?> res = ref.get();
					if (res != null)
						{
						ResNode node = res.getNode();
						if (node != null) node.openFrame();
						}
					}
				}
			}
		}

	public static class SearchResultsRenderer extends DefaultTreeCellRenderer
		{
		SearchResultNode last;
		private Color nonSelectColor;
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
				boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
			if (value instanceof SearchResultNode)
				{
				last = (SearchResultNode) value;
				}

			// this is a patch for the DarkEye Synthetica look and feel which for some reason
			// overrides its own UI property in its paint method, likely a bug on their part
			// same fix applied in GmTreeGraphics.java
			setTextNonSelectionColor(nonSelectColor);

			Component com = super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);

			// Bold primary nodes
			if (value instanceof SearchResultNode && com instanceof JLabel)
				{
				SearchResultNode rn = (SearchResultNode) value;
				JLabel label = (JLabel) com;
				if (rn.status == ResNode.STATUS_PRIMARY)
					{
					label.setText("<html><b>" + label.getText() + "</b></html>");
					}
				}

			return com;
			}

		@Override
		public void updateUI()
			{
			super.updateUI();
			nonSelectColor = this.getTextNonSelectionColor();
			}

		public Icon getLeafIcon()
			{
			if (last != null)
				{
				Icon icon = last.icon;
				if (icon != null) return icon;
				}
			return null;
			}

		public Icon getClosedIcon()
			{
			if (last != null)
				{
				if (last.status == ResNode.STATUS_PRIMARY || last.status == ResNode.STATUS_GROUP)
					{
					return LGM.getIconForKey("GmTreeGraphics.GROUP");
					}
				else
					{
					Icon icon = last.icon;
					if (icon != null) return icon;
					}
				}
			return null;
			}

		public Icon getOpenIcon()
			{
			if (last != null)
				{
				if (last.status == ResNode.STATUS_PRIMARY || last.status == ResNode.STATUS_GROUP)
					{
					return LGM.getIconForKey("GmTreeGraphics.GROUP_OPEN");
					}
				else
					{
					Icon icon = last.icon;
					if (icon != null) return icon;
					}
				}
			return null;
			}
		}

	private static JDialog createFilterSettingsDialog()
		{
		String title = Messages.getString("TreeFilter.TITLE");
		final JDialog filterSettings = new JDialog(LGM.frame,title,false);

		filterSettings.setIconImage(LGM.getIconForKey("TreeFilter.ICON").getImage());
		filterSettings.setResizable(false);

		wholeWordCB = new JCheckBox(Messages.getString("TreeFilter.WHOLEWORD"));
		wholeWordCB.addItemListener(new ItemListener()
			{
			public void itemStateChanged(ItemEvent e)
				{
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				Search.applyFilter(LGM.root.getChildren(),ml.isActivatedFilter(),filterText.getText(),
						matchCaseCB.isSelected(),wholeWordCB.isSelected(),false);
				}
			});
		regexCB = new JCheckBox(Messages.getString("TreeFilter.REGEX"));
		regexCB.addItemListener(new ItemListener()
			{
			public void itemStateChanged(ItemEvent e)
				{
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				Search.applyFilter(LGM.root.getChildren(),ml.isActivatedFilter(),filterText.getText(),
						matchCaseCB.isSelected(),wholeWordCB.isSelected(),false);
				}
			});
		matchCaseCB = new JCheckBox(Messages.getString("TreeFilter.MATCHCASE"));
		matchCaseCB.addItemListener(new ItemListener()
			{
			public void itemStateChanged(ItemEvent e)
				{
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				Search.applyFilter(LGM.root.getChildren(),ml.isActivatedFilter(),filterText.getText(),
						matchCaseCB.isSelected(),wholeWordCB.isSelected(),false);
				}
			});
		pruneResultsCB = new JCheckBox(Messages.getString("TreeFilter.PRUNERESULTS"));
		pruneResultsCB.addItemListener(new ItemListener()
			{
			public void itemStateChanged(ItemEvent e)
				{
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				ml.activateFilter(pruneResultsCB.isSelected());
				Search.applyFilter(LGM.root.getChildren(),ml.isActivatedFilter(),filterText.getText(),false,
						wholeWordCB.isSelected(),false);
				}
			});
		closeButton = new JButton(Messages.getString("TreeFilter.CLOSE"));
		closeButton.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent arg0)
				{
				filterSettings.setVisible(false);
				}
			});

		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		panel.setLayout(gl);
		filterSettings.getContentPane().setLayout(new GridBagLayout());
		filterSettings.add(panel);

		gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
		/**/.addGroup(gl.createSequentialGroup()
		/* */.addGroup(gl.createParallelGroup()
		/*  */.addComponent(wholeWordCB)
		/*  */.addComponent(matchCaseCB))
		/* */.addGroup(gl.createParallelGroup()
		/*  */.addComponent(regexCB)
		/*  */.addComponent(pruneResultsCB)))
		/**/.addComponent(closeButton));
		gl.setVerticalGroup(gl.createSequentialGroup()
		/**/.addGroup(gl.createParallelGroup()
		/* */.addGroup(gl.createSequentialGroup()
		/*  */.addComponent(wholeWordCB)
		/*  */.addComponent(matchCaseCB))
		/* */.addGroup(gl.createSequentialGroup()
		/*  */.addComponent(regexCB)
		/*  */.addComponent(pruneResultsCB)))
		/**/.addComponent(closeButton));

		filterSettings.pack();
		filterSettings.setLocationRelativeTo(LGM.frame);

		return filterSettings;
		}

	public static JToolBar createSearchToolbar()
		{
		// Use a toolbar so that the buttons render like tool buttons and smaller.
		JToolBar toolbar = new CustomJToolBar();

		final JDialog filterSettingsDialog = createFilterSettingsDialog();

		filterText = new HintTextField(Messages.getString("TreeFilter.SEARCHFOR"),true);

		JButton prevButton = new JButton(LGM.getIconForKey("TreeFilter.PREV"));
		prevButton.setToolTipText(Messages.getString("TreeFilter.PREV"));
		prevButton.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent arg0)
				{
				Search.searchFilter((ResNode) LGM.tree.getLastSelectedPathComponent(),filterText.getText(),
						matchCaseCB.isSelected(),wholeWordCB.isSelected(),true);
				}
			});

		JButton nextButton = new JButton(LGM.getIconForKey("TreeFilter.NEXT"));
		nextButton.setToolTipText(Messages.getString("TreeFilter.NEXT"));
		nextButton.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent arg0)
				{
				Search.searchFilter((ResNode) LGM.tree.getLastSelectedPathComponent(),filterText.getText(),
						matchCaseCB.isSelected(),wholeWordCB.isSelected(),false);
				}
			});

		JButton searchInButton = new JButton(LGM.getIconForKey("TreeFilter.SEARCHIN"));
		searchInButton.setToolTipText(Messages.getString("TreeFilter.SEARCHIN"));
		searchInButton.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent arg0)
				{
				if (filterText.getText().length() <= 0) return;
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				Search.searchInResources((DefaultMutableTreeNode) ml.getRoot(),filterText.getText(),
						regexCB.isSelected(),matchCaseCB.isSelected(),wholeWordCB.isSelected());
				LGM.setSelectedTab(LGM.treeTabs,Messages.getString("TreeFilter.TAB_SEARCHRESULTS"));
				}
			});

		JButton setButton = new JButton(LGM.getIconForKey("TreeFilter.SET"));
		setButton.setToolTipText(Messages.getString("TreeFilter.SET"));
		setButton.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent arg0)
				{
				filterSettingsDialog.setVisible(true);
				}
			});

		filterText.getDocument().addDocumentListener(new DocumentListener()
			{
			public void changedUpdate(DocumentEvent e)
				{

				}

			public void removeUpdate(DocumentEvent e)
				{
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				if (ml.isActivatedFilter())
					Search.applyFilter(LGM.root.getChildren(),ml.isActivatedFilter(),filterText.getText(),false,
							wholeWordCB.isSelected(),true);
				else
					Search.searchFilter(LGM.root,filterText.getText(),matchCaseCB.isSelected(),
							wholeWordCB.isSelected(),false);
				}

			public void insertUpdate(DocumentEvent e)
				{
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				if (ml.isActivatedFilter())
					Search.applyFilter(LGM.root.getChildren(),ml.isActivatedFilter(),filterText.getText(),false,
							wholeWordCB.isSelected(),true);
				else
					Search.searchFilter(LGM.root,filterText.getText(),matchCaseCB.isSelected(),
							wholeWordCB.isSelected(),false);
				}
			});

		filterText.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent evt)
				{
				if (filterText.getText().length() <= 0) return;
				InvisibleTreeModel ml = (InvisibleTreeModel) LGM.tree.getModel();
				Search.searchInResources((DefaultMutableTreeNode) ml.getRoot(),filterText.getText(),
						regexCB.isSelected(),matchCaseCB.isSelected(),wholeWordCB.isSelected());
				LGM.setSelectedTab(LGM.treeTabs,Messages.getString("TreeFilter.TAB_SEARCHRESULTS"));
				}
			});

		// Use a custom layout so that the filterText control will stretch horizontally under
		// all Look and Feels.
		GroupLayout filterLayout = new GroupLayout(toolbar);

		filterLayout.setHorizontalGroup(filterLayout.createSequentialGroup()
		/**/.addComponent(filterText)
		/**/.addComponent(prevButton)
		/**/.addComponent(nextButton)
		/**/.addComponent(searchInButton)
		/**/.addComponent(setButton));

		filterLayout.setVerticalGroup(filterLayout.createParallelGroup(Alignment.CENTER)
		/**/.addComponent(filterText,PREFERRED_SIZE,PREFERRED_SIZE,PREFERRED_SIZE)
		/**/.addComponent(prevButton)
		/**/.addComponent(nextButton)
		/**/.addComponent(searchInButton)
		/**/.addComponent(setButton));

		toolbar.setLayout(filterLayout);
		toolbar.setFloatable(true);
		toolbar.setVisible(Prefs.showTreeFilter);
		return toolbar;
		}

	public static JTree createSearchTree()
		{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		final JTree tree = new JTree(root);

		// Create tree context menu
		final JPopupMenu searchMenu = new JPopupMenu();
		JMenuItem expandAllItem = new JMenuItem(Messages.getString("TreeFilter.EXPANDALL"));
		expandAllItem.setIcon(LGM.getIconForKey("TreeFilter.EXPANDALL"));
		expandAllItem.setAccelerator(
				KeyStroke.getKeyStroke(Messages.getKeyboardString("TreeFilter.EXPANDALL")));
		expandAllItem.addActionListener(new ActionListener()
			{
			public void expandChildren(JTree tree, DefaultMutableTreeNode node)
				{
				Enumeration<?> children = node.children();
				DefaultMutableTreeNode it = null;
				while (children.hasMoreElements())
					{
					it = (DefaultMutableTreeNode) children.nextElement();
					tree.expandPath(new TreePath(it.getPath()));
					if (it.getChildCount() > 0)
						{
						expandChildren(tree,it);
						}
					}
				}

			public void actionPerformed(ActionEvent ev)
				{
				expandChildren(tree,(DefaultMutableTreeNode) tree.getModel().getRoot());
				}
			});
		searchMenu.add(expandAllItem);
		JMenuItem collapseAllItem = new JMenuItem(Messages.getString("TreeFilter.COLLAPSEALL"));
		collapseAllItem.setIcon(LGM.getIconForKey("TreeFilter.COLLAPSEALL"));
		collapseAllItem.setAccelerator(
				KeyStroke.getKeyStroke(Messages.getKeyboardString("TreeFilter.COLLAPSEALL")));
		collapseAllItem.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent ev)
				{
				//NOTE: The code for expanding all nodes does not work here because collapsing a child node
				//will expand its parent, so you have to do it in reverse. For now I will just reload the tree.
				((DefaultTreeModel) tree.getModel()).reload();
				}
			});
		searchMenu.add(collapseAllItem);
		searchMenu.addSeparator();

		Action treeCopyAction = new AbstractAction("COPY") //$NON-NLS-1$
			{
			/**
			 * NOTE: Default UID generated, change if necessary.
			 */
			private static final long serialVersionUID = 2505969552404421504L;

			public void actionPerformed(ActionEvent ev)
				{
				Object obj = ev.getSource();
				if (obj == null) return;
				JTree tree = null;
				if (!(obj instanceof JTree))
					tree = LGM.searchTree;
				else
					tree = (JTree) obj;

				String text = ""; //$NON-NLS-1$
				int[] rows = tree.getSelectionRows();
				java.util.Arrays.sort(rows);
				for (int i = 0; i < rows.length; i++)
					{
					TreePath path = tree.getPathForRow(rows[i]);
					text += (i > 0 ? "\n" : "") //$NON-NLS-1$ //$NON-NLS-2$
							+ path.getLastPathComponent().toString().replaceAll("\\<[^>]*>",""); //$NON-NLS-1$ //$NON-NLS-2$
					}

				StringSelection selection = new StringSelection(text);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection,selection);
				}
			};

		JMenuItem copyItem = new JMenuItem();
		copyItem.setAction(treeCopyAction);
		copyItem.setText(Messages.getString("TreeFilter.COPY"));
		copyItem.setIcon(LGM.getIconForKey("TreeFilter.COPY"));
		copyItem.setAccelerator(KeyStroke.getKeyStroke(Messages.getKeyboardString("TreeFilter.COPY")));

		tree.getActionMap().put("COPY",treeCopyAction);
		tree.getInputMap().put(copyItem.getAccelerator(),"COPY");
		// Add it to the main tree as well to remove HTML formatting
		tree.getActionMap().put("COPY",treeCopyAction);
		tree.getInputMap().put(copyItem.getAccelerator(),"COPY");

		searchMenu.add(copyItem);
		searchMenu.addSeparator();
		JMenuItem selectAllItem = new JMenuItem(Messages.getString("TreeFilter.SELECTALL"));

		selectAllItem.setIcon(LGM.getIconForKey("TreeFilter.SELECTALL"));
		selectAllItem.setAccelerator(
				KeyStroke.getKeyStroke(Messages.getKeyboardString("TreeFilter.SELECTALL")));
		//NOTE: It's possible to grab the trees built in Select All action.
		//selectAllItem.setAction(tree.getActionMap().get(tree.getInputMap().get(selectAllItem.getAccelerator())));

		selectAllItem.addActionListener(new ActionListener()
			{
			public void selectAllChildren(JTree tree, DefaultMutableTreeNode node)
				{
				Enumeration<?> children = node.children();
				DefaultMutableTreeNode it = null;
				while (children.hasMoreElements())
					{
					it = (DefaultMutableTreeNode) children.nextElement();
					tree.addSelectionPath(new TreePath(it.getPath()));
					if (tree.isExpanded(new TreePath(it.getPath())))
						{
						selectAllChildren(tree,it);
						}
					}
				}

			public void actionPerformed(ActionEvent ev)
				{
				selectAllChildren(tree,(DefaultMutableTreeNode) tree.getModel().getRoot());
				}
			});
		searchMenu.add(selectAllItem);

		tree.setToggleClickCount(0); // we only want to expand on double click with group nodes, not result nodes
		tree.setCellRenderer(new SearchResultsRenderer());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.addMouseListener(new MouseAdapter()
			{
			public void mouseReleased(MouseEvent me)
				{
				TreePath path = tree.getPathForLocation(me.getX(),me.getY());

				boolean inpath = false;
				if (path != null)
					{
					//Check to see if we have clicked on a different node then the one
					//currently selected.
					TreePath[] paths = tree.getSelectionPaths();

					if (paths != null)
						{
						for (int i = 0; i < paths.length; i++)
							{
							if (paths[i].equals(path)) inpath = true;
							}
						}

					if (me.getModifiers() == InputEvent.BUTTON1_MASK && inpath)
						{
						tree.setSelectionPath(path);
						}
					}
				//Isn't Java supposed to handle ctrl+click for us? For some reason it doesn't.
				if (me.getModifiers() == InputEvent.BUTTON3_MASK && me.getClickCount() == 1)
					{
					// Yes the right click button does change the selection,
					// go ahead and experiment with Eclipse, CodeBlocks, Visual Studio
					// or Qt. Swing's default component popup listener does not do this
					// indicating it is an inconsistency with the framework compared to
					// other GUI libraries.
					if (!inpath && path != null)
						{
						tree.setSelectionPath(path);
						}
					searchMenu.show((Component) me.getSource(),me.getX(),me.getY());
					return;
					}

				if (path == null) return;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node == null) return;
				if (me.getModifiers() == InputEvent.BUTTON1_MASK && me.getClickCount() >= 2
						&& ((me.getClickCount() & 1) == 0))
					{
					if (node instanceof SearchResultNode)
						{
						SearchResultNode srn = (SearchResultNode) node;

						if (srn.status >= ResNode.STATUS_SECONDARY)
							{
							srn.openFrame();
							return;
							}
						else
							{
							if (tree.isExpanded(path))
								tree.collapsePath(path);
							else
								tree.expandPath(path);
							}
						}
					else
						{
						if (tree.isExpanded(path))
							tree.collapsePath(path);
						else
							tree.expandPath(path);
						}
					}
				}
			});

		return tree;
		}
	}
