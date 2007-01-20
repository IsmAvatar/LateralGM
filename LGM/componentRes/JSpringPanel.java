package componentRes;


/*
 * JSpringPanel - A JPanel class for placing elements relative to one another
 * IsmAvatar 20060419 - Original Coding and Concept
 */

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class JSpringPanel extends JPanel
	{
	private static final long serialVersionUID = 1L;
	public static SpringLayout layout = new SpringLayout();
	public final static String w = "West", s = "South", n = "North", e = "East";
	public JSpringPanel()
		{
		super(layout);
		}

	/*
	 * Add a component "me" to this Panel
	 * using SpringLayout "layout"
	 * sets position relative to component "it"
	 * such that cardinal point "d11" of me
	 * lines up with cardinal point "d12" of it
	 * with "d1" pixels separation
	 * and cardinal point "d21" of me
	 * lines up with cardinal point "d22" of it
	 * with "d2" pixels separation
	 * 
	 * returns the component added to the frame
	 */
	public Component add(
			Component me, Component it,
			String d11, String d12, int d1,
			String d21, String d22, int d2)
		{
		add(me);
		layout.putConstraint(d11,me,d1,d12,it);
		layout.putConstraint(d21,me,d2,d22,it);
		return me;
		}
	public void set(
			Component me, Component it,
			String d11,String d12, int d1)
		{
		layout.putConstraint(d11,me,d1,d12,it);
		}
	}