package org.lateralgm.components.visual;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;

import javax.swing.JTextField;

public class HintTextField extends JTextField implements FocusListener {

	/**
	 * NOTE: Default UID generated, change if necessary.
	 */
	private static final long serialVersionUID = 1891424933399664075L;

	private String hint;
	private boolean hideOnFocus;
	private Color color;

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		repaint();
	}

	public boolean isHideOnFocus() {
		return hideOnFocus;
	}

	public void setHideOnFocus(boolean hideOnFocus) {
		this.hideOnFocus = hideOnFocus;
		repaint();
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
		repaint();
	}

	public HintTextField(String hint) {
		this(hint,false);
	}

	public HintTextField(String hint, boolean hideOnFocus) {
		this(hint,hideOnFocus, null);
	}

	public HintTextField(String hint, boolean hideOnFocus, Color color) {
		this.hint = hint;
		this.hideOnFocus = hideOnFocus;
		this.color = color;
		addFocusListener(this);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (hint != null && getText().length() == 0 && hideOnFocus && !hasFocus()) {
			Shape clip = g.getClip();
			Rectangle bounds = this.getBounds();
			Insets insets = this.getInsets();

			g.clipRect(insets.left, insets.top, bounds.width - insets.left - insets.right,
				bounds.height - insets.top - insets.bottom);

			// this allows the control to get the global property for antialiasing
			// thus making it affected by the preference in the preferences window
			Object map = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
			if (map != null) {
				((Graphics2D) g).addRenderingHints((Map<?,?>) map);
			}

			g.drawString(hint, insets.left, g.getFontMetrics().getAscent() + insets.top);

			g.setClip(clip);
		}
	}

	public void focusGained(FocusEvent e) {
		if (hideOnFocus) repaint();
	}

	public void focusLost(FocusEvent e) {
		if (hideOnFocus) repaint();
	}
}
