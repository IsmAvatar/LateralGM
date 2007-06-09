package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JList;

import org.lateralgm.components.EventKeyInput;
import org.lateralgm.components.EventNode;
import org.lateralgm.components.ResourceMenu;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

import static org.lateralgm.subframes.ResourceFrame.addDim;

public class EventFrame extends JInternalFrame implements MouseListener,ActionListener,
		ContainerListener,InternalFrameListener,DocumentListener
	{
	private static final long serialVersionUID = 1L;

	public EventKeyInput keySelect;
	public ResourceMenu collisionSelect;
	public JTextField frameName;
	public JButton frameChoose;
	public GmObjectFrame linkedFrame;
	private JPopupMenu menu = new JPopupMenu();
	public EventNode root;
	public JTree events;

	public EventFrame()
		{
		super("Event Selector",true,true,false,true);
		setSize(290,310);
		setMinimumSize(new Dimension(290,260));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		JPanel side1 = new JPanel();
		side1.setLayout(new BorderLayout());

		root = new EventNode("Root",-1);

		root.add(MainEvent.EV_CREATE);

		root.add(MainEvent.EV_DESTROY);

		EventNode alarm = new EventNode(Messages.getString("MainEvent.EVENT2"),-1);
		root.add(alarm);
		for (int i = 0; i <= 11; i++)
			alarm.add(new EventNode(String.format(Messages.getString("Event.EVENT2_X"),i),
					MainEvent.EV_ALARM,i));

		EventNode step = new EventNode(Messages.getString("MainEvent.EVENT3"),-1);
		root.add(step);
		for (int i = Event.EV_STEP_NORMAL; i <= Event.EV_STEP_END; i++)
			step.add(MainEvent.EV_STEP,i);

		root.add(MainEvent.EV_COLLISION);

		root.add(MainEvent.EV_KEYBOARD);

		EventNode mouse = new EventNode(Messages.getString("MainEvent.EVENT6"),-1);
		root.add(mouse);
		for (int i = Event.EV_LEFT_BUTTON; i <= Event.EV_MOUSE_LEAVE; i++)
			mouse.add(MainEvent.EV_MOUSE,i);
		mouse.add(MainEvent.EV_MOUSE,Event.EV_MOUSE_WHEEL_UP);
		mouse.add(MainEvent.EV_MOUSE,Event.EV_MOUSE_WHEEL_DOWN);

		EventNode global = new EventNode("Global Mouse",-1);
		mouse.add(global);
		for (int i = Event.EV_GLOBAL_LEFT_BUTTON; i <= Event.EV_GLOBAL_MIDDLE_RELEASE; i++)
			global.add(MainEvent.EV_MOUSE,i);

		EventNode joy = new EventNode("Joystick 1",-1);
		mouse.add(joy);
		for (int i = Event.EV_JOYSTICK1_LEFT; i <= Event.EV_JOYSTICK1_BUTTON8; i++)
			if (i != 20) joy.add(MainEvent.EV_MOUSE,i);

		joy = new EventNode("Joystick 2",-1);
		mouse.add(joy);
		for (int i = Event.EV_JOYSTICK2_LEFT; i <= Event.EV_JOYSTICK2_BUTTON8; i++)
			if (i != 35) joy.add(MainEvent.EV_MOUSE,i);

		EventNode other = new EventNode(Messages.getString("MainEvent.EVENT7"),-1);
		root.add(other);
		for (int i = 0; i <= 8; i++)
			other.add(MainEvent.EV_OTHER,i);

		EventNode user = new EventNode("User Defined",-1);
		other.add(user);
		for (int i = 0; i <= 14; i++)
			user.add(new EventNode(String.format(Messages.getString("Event.EVENT7_X"),i),
					MainEvent.EV_OTHER,Event.EV_USER0 + i));

		root.add(MainEvent.EV_DRAW);
		root.add(MainEvent.EV_KEYPRESS);
		root.add(MainEvent.EV_KEYRELEASE);

		events = new JTree(root);
		events.setRootVisible(false);
		events.setShowsRootHandles(true);
		JScrollPane scroll = new JScrollPane(events);
		scroll.setMinimumSize(new Dimension(130,260));
		side1.add(scroll,"Center");
		add(side1);

		JPanel side2 = new JPanel();
		side2.setLayout(new FlowLayout());
		side2.setPreferredSize(new Dimension(150,200));
		side2.setMaximumSize(new Dimension(150,200));
		side2.setMinimumSize(new Dimension(150,200));

		addDim(side2,new JLabel("Key Selector"),140,16);
		keySelect = new EventKeyInput();
		addDim(keySelect,140,20);

		addDim(side2,new JLabel("Collision Object"),140,16);
		collisionSelect = new ResourceMenu(Resource.GMOBJECT,"<choose an object>",true,140);
		side2.add(collisionSelect);

		addDim(side2,new JLabel("Frame Link"),140,16);
		frameName = new JTextField();
		frameName.setEditable(false);
		addDim(frameName,110,20);
		frameChoose = new JButton(Resource.ICON[Resource.GMOBJECT]);
		frameChoose.addMouseListener(this);
		addDim(frameChoose,20,20);

		JInternalFrame frames[] = LGM.mdi.getAllFrames();
		for (JInternalFrame frame : frames)
			if (frame instanceof GmObjectFrame)
				{
				GmObjectFrame f = (GmObjectFrame) frame;
				GmObjectFrameItem item = new GmObjectFrameItem(f);
				f.addInternalFrameListener(this);
				item.addActionListener(this);
				menu.add(item);
				}
		LGM.mdi.addContainerListener(this);

		add(side2);
		setVisible(true);
		}

	private class GmObjectFrameItem extends JMenuItem implements DocumentListener
		{
		private static final long serialVersionUID = 1L;
		GmObjectFrame frame;

		GmObjectFrameItem(GmObjectFrame frame)
			{
			this.frame = frame;
			setIcon(frame.getFrameIcon());
			setText(frame.name.getText());
			frame.name.getDocument().addDocumentListener(this);
			}

		public void changedUpdate(DocumentEvent e)
			{
			// TODO Auto-generated method stub

			}

		public void insertUpdate(DocumentEvent e)
			{
			setText(frame.name.getText());
			}

		public void removeUpdate(DocumentEvent e)
			{
			setText(frame.name.getText());
			}
		}

	public void mouseClicked(MouseEvent e)
		{
		if (e.getSource() == frameChoose && menu.getComponentCount() > 0)
			{

			menu.show(e.getComponent(),e.getX(),e.getY());
			}
		}

	public void internalFrameActivated(InternalFrameEvent e)
		{
		if (e.getInternalFrame() instanceof GmObjectFrame && !e.getInternalFrame().isIcon())
			{
			if (linkedFrame != null) linkedFrame.name.removeActionListener(this);
			linkedFrame = (GmObjectFrame) e.getInternalFrame();
			linkedFrame.name.getDocument().addDocumentListener(this);
			frameName.setText(linkedFrame.name.getText());
			}
		}

	public void internalFrameDeiconified(InternalFrameEvent e)
		{
		internalFrameActivated(e);
		}

	public void componentAdded(ContainerEvent e)
		{
		if (e.getChild() instanceof GmObjectFrame)
			{
			GmObjectFrameItem item = new GmObjectFrameItem((GmObjectFrame) e.getChild());
			item.addActionListener(this);
			((GmObjectFrame) e.getChild()).addInternalFrameListener(this);
			menu.add(item);
			}
		}

	public void componentRemoved(ContainerEvent e)
		{
		if (e.getChild() instanceof GmObjectFrame)
			{
			for (int i = 0; i < getComponentCount(); i++)
				if (((GmObjectFrameItem) menu.getComponent(i)).frame == e.getChild())
					{
					if (linkedFrame == e.getChild())
						{
						linkedFrame.name.getDocument().removeDocumentListener(this);
						linkedFrame = null;
						frameName.setText("");
						}
					menu.remove(i);
					break;
					}
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() instanceof GmObjectFrameItem)
			{
			GmObjectFrameItem item = (GmObjectFrameItem) e.getSource();
			linkedFrame = item.frame;
			linkedFrame.toFront();
			toFront();
			frameName.setText(item.getText());
			}
		}

	public void insertUpdate(DocumentEvent e)
		{
		frameName.setText(linkedFrame.name.getText());
		}

	public void removeUpdate(DocumentEvent e)
		{
		frameName.setText(linkedFrame.name.getText());
		}

	//unused
	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void mousePressed(MouseEvent e)
		{
		}

	public void mouseReleased(MouseEvent e)
		{
		}

	public void internalFrameClosed(InternalFrameEvent e)
		{
		}

	public void internalFrameClosing(InternalFrameEvent e)
		{
		}

	public void internalFrameDeactivated(InternalFrameEvent e)
		{
		}

	public void internalFrameIconified(InternalFrameEvent e)
		{
		}

	public void internalFrameOpened(InternalFrameEvent e)
		{
		}

	public void changedUpdate(DocumentEvent e)
		{
		}
	}
