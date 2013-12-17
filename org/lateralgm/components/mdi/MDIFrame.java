/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2013 Robert B Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;

public class MDIFrame extends JInternalFrame
	{
	private static final long serialVersionUID = 1L;
	private javax.swing.plaf.basic.BasicInternalFrameTitlePane titlePane;

	public MDIFrame()
		{
		this("",false,false,false,false);
		}

	public MDIFrame(String title)
		{
		this(title,false,false,false,false);
		}

	public MDIFrame(String title, boolean resizable)
		{
		this(title,resizable,false,false,false);
		}

	public MDIFrame(String title, boolean resizable, boolean closable)
		{
		this(title,resizable,closable,false,false);
		}

	public MDIFrame(String title, boolean resizable, boolean closable, boolean maximizable)
		{
		this(title,resizable,closable,maximizable,false);
		}

	public MDIFrame(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable)
	{
		super(title,resizable,closable,maximizable,iconifiable);
		if (LGM.themename.equals("Quantum")) {
		  this.setUI(new CustomInternalFrameUI(this));
		  // please leave the frame as opaque
		  // set it to false to fix the corners of mdi frames from
		  // not bliting properly
		  // but be warned it makes frames literally 2x as slow
		  // because all the other mdi frames have to painted first just for
		  // that barely noticeable 4px area
		  // the only other way around it is to leave it on
		  // and tell the DesktopPane to only invalidate the 4px areas
		  // of the mdiframe, but this screws up the DesktopPane's zordering
		  // so just leave it be is my suggestion to you - Robert B. Colton
			setOpaque(true);
		}
	}

	private MDIPane getMDIPane()
		{
		Container c = getParent();
		if (c != null && c instanceof MDIPane) return (MDIPane) c;
		return null;
		}

	public void toTop()
		{
		try
			{
			setVisible(true);
			setIcon(false);
			setSelected(true);
			MDIPane pane = getMDIPane();
			if (pane != null)
				{
				if (pane.isMaximum())
					{
					if (isMaximizable())
						{
						toFront();
						setMaximum(true);
						}
					else
						pane.bringMaximumToTop();
					}
				else
					toFront();
				}
			}
		catch (PropertyVetoException e1)
			{
			e1.printStackTrace();
			}
		}

	@Override
	public void setMaximum(boolean b) throws PropertyVetoException
		{
		super.setMaximum(b);
		MDIPane pane = getMDIPane();
		if (pane != null) pane.resizeDesktop();
		}
	
	@Override
	public void paint(Graphics g)
		{
		if (!LGM.themename.equals("Quantum")) {
		  super.paint(g);
		  return;
		}
    Graphics2D g2d = (Graphics2D)g;
		boolean gradientBackground = true;
		Color lightColor = null, darkColor = null;
    Color frameColor = null;
  
    if (!Prefs.antialiasContolFont.toLowerCase().equals("off")) {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
	  
    if (gradientBackground) {
	    if (this.isSelected) {
        lightColor = new Color(60, 170, 250);
        darkColor = new Color(10, 120, 200);
      } else {
        lightColor = new Color(20, 140, 220);
        darkColor = new Color(0, 90, 170);
      }
      GradientPaint gp = new GradientPaint(
          0, 0, lightColor,
          0, 20, darkColor );
      g2d.setPaint(gp);
    } else {
      if (this.isSelected()) {
		    frameColor = darkColor;
      } else {
        frameColor = lightColor;
      }
      g.setColor(frameColor);
    }
    
    /* Custom Clipping For Optimization
     * NOTE! Breaks Z-ordering */
    Rectangle clipRect = this.getRootPane().getBounds();
    clipRect.setLocation(this.getRootPane().getX(), getRootPane().getY());
    Area insideClip = calculateRectOutside(clipRect);
    //g2d.setClip(insideClip); 
    
    int arcWidth = 10;
    int arcHeight = 10;
    
      if (this.isMaximum) {
		    g2d.fillRect(0, 0, getWidth(), getHeight());
		    g2d.setColor(Color.black);
		    g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1); 
      } else {
	      g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
	      g2d.setColor(Color.black);
	      g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
      }
		  
		  // Custom clipping disabled, see comment above
		 // g2d.setClip(this.getVisibleRect());
		  this.paintChildren(g2d);
		  
		  //g2d.setClip(null);
		}
	
	private Area calculateRectOutside(Rectangle2D r) {
 	  Area outside = new Area(this.getVisibleRect());
	  outside.subtract(new Area(r));
	  return outside;
  }

	@Override
	public void setVisible(boolean visible)
		{
		super.setVisible(visible);
		MDIPane pane = getMDIPane();
		if (pane != null)
			{
			if (visible)
				{
				if (pane.isMaximum() && isMaximizable())
					try
						{
						setMaximum(true);
						}
					catch (PropertyVetoException e)
						{
						e.printStackTrace();
						}
				else
					pane.bringMaximumToTop();
				}
			}
		}
	
	private class CustomInternalFrameTitlePane extends BasicInternalFrameTitlePane
	{
	
	  /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private class JFrameButton extends JButton 
	  {
	    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private boolean hover = false;
			private boolean clicked = false;
			
			public JFrameButton()
		  {
				//setFocusPainted(true);
				
				setOpaque(false);
				setContentAreaFilled(false);
				setBorderPainted(false);
				
				addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseEntered(java.awt.event.MouseEvent evt) {
				  hover = true;
				}
				public void mouseExited(java.awt.event.MouseEvent evt) {
				  hover = false;
				}
				public void mouseClicked(java.awt.event.MouseEvent evt) {
				  Rectangle rect = new Rectangle(2, 2, getWidth() - 4, getHeight() - 4);
			    if (rect.contains(evt.getPoint())) {
			      clicked = true;
			    }
			  }
				public void mouseReleased(java.awt.event.MouseEvent evt) {
		      clicked = false;
		    }
				});
				
			}
			
			@Override
			public Dimension getPreferredSize()
				{
					Dimension result=super.getPreferredSize();
					result.width += 16;
					result.height += 16;
					
					return result;
				}

			@Override
			public void paintComponent(Graphics g) 
	    {
	    //super.paint(g);

	      //AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0f);
	      //Graphics2D g2d = (Graphics2D) g;
	      //g2d.setBackground(new Color(0, 0, 0, 0));
	      //clear
	      //g2d.setComposite(composite);
	      //g2d.setColor(new Color(0, 0, 0, 0));
	      //g2d.fillRect(0, 0, 100, 100);
	      

	      Graphics2D g2 = (Graphics2D) g.create();
	      if (!Prefs.antialiasContolFont.toLowerCase().equals("off")) {
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	      }
	      Graphics2D g2d = (Graphics2D)g;
	  		boolean gradientBackground = true;
	  		Color lightColor = null, darkColor = null;
	      Color frameColor = null;
	    
	      if (clicked) {
          //g2d.setComposite(AlphaComposite.getInstance(
          // AlphaComposite.SRC_OVER, 1f));
	        lightColor = Color.gray;
          darkColor = Color.gray;
        } else if (hover) {
	        lightColor = new Color(20, 140, 220);
	        darkColor = new Color(0, 90, 170);
        } else {
	        lightColor = new Color(60, 170, 250);
	        darkColor = new Color(10, 120, 200);
        }
	      
		    GradientPaint gp = new GradientPaint(
		        0, 0, lightColor,
		        0, 20, darkColor );
		    g2d.setPaint(gp);
		    
        g.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 3, 3);
      
        this.getIcon().paintIcon(this, g2, 0, 0);
	      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0));
        g.dispose();
	      //this.getIcon().paintIcon(this, g2, 0, 0);
	      //super.paint(g2);
	      //g2.dispose();
	      
        //g2d.setComposite(AlphaComposite.getInstance(
            //AlphaComposite.SRC_OVER, 1f));

	      // Uncomment this if you want the buttons on the other side of the frame
	      // like ubuntu
	      //AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
	      //tx.translate(-this.getIcon().getIconWidth(), 0);
	      //AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	      //g2d.transform(tx);

        //g2.dispose();
	    }
	  }

		public CustomInternalFrameTitlePane(JInternalFrame f)
			{
			super(f);
			// TODO Auto-generated constructor stub
			this.notSelectedTitleColor = new Color(0, 0, 0, 0);
			this.selectedTitleColor = new Color(0, 0, 0, 0);
			this.notSelectedTextColor = new Color(200, 200, 200);
			this.selectedTextColor = new Color(255, 255, 255);
			this.setOpaque(false);
			this.setBackground(new Color(0,0,0,0));
			
			//this.addSubComponents();

			//this.windowMenu.setBackground(Color.red);
			}
		
		@Override
		public void createButtons() 
		{
		  closeIcon = LGM.getIconForKey("MdiFrame.CLOSE");
		  maxIcon = LGM.getIconForKey("MdiFrame.MAXIMIZE");
		  minIcon = LGM.getIconForKey("MdiFrame.RESTORE");
		  iconIcon = LGM.getIconForKey("MdiFrame.MINIMIZE");
		
		  iconButton= new JFrameButton();
		  iconButton.setFocusPainted(false);
		  iconButton.setFocusable(false);
		  iconButton.setOpaque(true);
		  iconButton.addActionListener(iconifyAction);
		  
		  maxButton = new JFrameButton();
		  maxButton.setFocusPainted(false);
		  maxButton.setFocusable(false);
		  maxButton.setOpaque(true);		
		  maxButton.addActionListener(maximizeAction);
      
			closeButton = new JFrameButton();
			closeButton.setFocusPainted(false);
			closeButton.setFocusable(false);
			closeButton.setOpaque(true);		
			closeButton.addActionListener(closeAction);
			
	    windowMenu = new JMenu();
	    windowMenu.add(new JButton());
			
      setButtonIcons();
		}
		
		@Override
		protected LayoutManager createLayout() 
			{
				 return new TitlePaneLayout();
			}
		
		/**	Layout for the InternalFrameTitlePane */
		class TitlePaneLayout implements LayoutManager
		{
			private boolean isPalette = false;
			private int paletteTitleHeight = 50;


			/**	Adds a component which is to be layouted */
			public void addLayoutComponent(String name, Component c)
			{
				// Does nothing
			}
			
			
			/**	Removes a component which is to be layouted */
			public void removeLayoutComponent(Component c)
			{
				// Does nothing
			}
			
			
			/**	Returns the preferred size of this layout for the specified component */
			public Dimension preferredLayoutSize(Container c)
			{
				return minimumLayoutSize(c);
			}


			/**	Returns the minimum size of this layout for the specified component */
			public Dimension minimumLayoutSize(Container c)
			{
				// Compute width.
				int width= 30;
				if (frame.isClosable())
				{
					width += closeIcon.getIconWidth();
				}
				if (frame.isMaximizable())
				{
					width += maxIcon.getIconWidth() + (frame.isClosable() ? 10 : 4);
				}
				if (frame.isIconifiable())
				{
					width += iconIcon.getIconWidth() 
						+ (frame.isMaximizable() ? 2 : (frame.isClosable() ? 10 : 4));
				}
				FontMetrics fm= getFontMetrics(getFont());
				String frameTitle= frame.getTitle();
				int title_w= frameTitle != null ? fm.stringWidth(frameTitle) : 0;
				int title_length= frameTitle != null ? frameTitle.length() : 0;

				if (title_length > 2)
				{
					int subtitle_w=
						fm.stringWidth(frame.getTitle().substring(0, 2) + "...");
					width += (title_w < subtitle_w) ? title_w : subtitle_w;
				}
				else
				{
					width += title_w;
				}

				// Compute height.
				int height= 0;
				if (isPalette)
				{
					height= paletteTitleHeight;
				}
				else
				{
					int fontHeight= fm.getHeight();
					fontHeight += 7;
					Icon icon= frame.getFrameIcon();
					int iconHeight= 0;
					if (icon != null)
					{
						// SystemMenuBar forces the icon to be 16x16 or less.
						iconHeight= Math.min(icon.getIconHeight(), 16);
					}
					iconHeight += 5;
					height= Math.max(fontHeight, iconHeight);
				}
				return new Dimension(width, height);
			}

			
			/**	Does a layout for the specified container */
			public void layoutContainer(Container c)
			{
				boolean leftToRight = true;

				int w= getWidth();
				int x= leftToRight ? w : 0;
				int spacing;

				// assumes all buttons have the same dimensions
				// these dimensions include the borders
				int buttonHeight= closeButton.getIcon().getIconHeight();
				int buttonWidth= closeButton.getIcon().getIconWidth();
				int y = 1;
				
				spacing= 0;
				x += leftToRight ? -spacing - (buttonWidth + 2) : spacing;
				iconButton.setBounds(x, y, buttonWidth + 2, getHeight() - 4);
				if (!leftToRight)
					x += (buttonWidth + 2);
				
				if (frame.isClosable())
				{
					if (isPalette)
					{
						spacing= 3;
						x += leftToRight ? -spacing - (buttonWidth) : spacing;
						closeButton.setBounds(x + 30, y, buttonWidth, getHeight() - 4);
						if (!leftToRight)
							x += (buttonWidth + 2);
					}
					else
					{
						spacing= 0;
						x += leftToRight ? -spacing - buttonWidth : spacing;
						closeButton.setBounds(x + 30, y + 1, buttonWidth, buttonHeight);
						if (!leftToRight)
							x += buttonWidth;
					}
				}

				if (frame.isMaximizable() && !isPalette)
				{
					x += leftToRight ? -spacing - buttonWidth : spacing;
					buttonHeight= closeButton.getIcon().getIconHeight();
					buttonWidth= closeButton.getIcon().getIconWidth();
					closeButton.setBounds(x + 30, y, buttonWidth, buttonHeight);
					if (!leftToRight)
						x += buttonWidth;
				}
				
				if (frame.isMaximizable() && !isPalette)
				{
					x += leftToRight ? -spacing - buttonWidth : spacing;
					buttonHeight= maxButton.getIcon().getIconHeight();
					buttonWidth= maxButton.getIcon().getIconWidth();
					maxButton.setBounds(x + 30, y, buttonWidth, buttonHeight);
					if (!leftToRight)
						x += buttonWidth;
				}
				
				if (frame.isIconifiable() && !isPalette)
				{
					x += leftToRight ? -spacing - buttonWidth : spacing;
					buttonHeight= iconButton.getIcon().getIconHeight();
					buttonWidth= iconButton.getIcon().getIconWidth();
					iconButton.setBounds(x + 30, y, buttonWidth, buttonHeight);
					if (!leftToRight)
						x += buttonWidth;
				}
			}
		}
		
		@Override
		 public void paint(Graphics g) 
		    {
		    //super.paint(g);
		    this.paintChildren(g);
		    Graphics2D g2d = (Graphics2D)g;
	      if (!Prefs.antialiasContolFont.toLowerCase().equals("off")) {
	        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	      }
		    frame.getFrameIcon().paintIcon(this,g,2,2);
		    g2d.setFont(this.getFont());
		    FontMetrics fm = this.getFontMetrics(this.getFont());
		    String title = this.getTitle(frame.getTitle(),fm,this.getWidth());
		    g2d.setColor(Color.white);

		    g2d.drawString(title,32,fm.getHeight());
		   }
	}
	
	private class CustomInternalFrameUI extends BasicInternalFrameUI
	{
	
	public CustomInternalFrameUI(JInternalFrame b)
			{
			super(b);
			// TODO Auto-generated constructor stub
			}

	protected JComponent createNorthPane(JInternalFrame w)
		{
		  return new CustomInternalFrameTitlePane(w);
		}
	
	}
	
	}
