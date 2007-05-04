package org.lateralgm.subframes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.NameDocument;
import org.lateralgm.components.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.resources.Resource;



// Provides common functionality and structure to Resource editing frames
public abstract class ResourceFrame<R extends Resource> extends JInternalFrame implements DocumentListener,
		ActionListener
	{
	private static final long serialVersionUID = 1L;
	public JTextField name; // The Resource's name - setup automatically to update the title of the frame and
	// the ResNode's text
	public JButton save; // automatically set up to save and close the frame
	public R res; // the resource this frame is editing
	public R resOriginal; // backup of res as it was before changes were made
	public String titlePrefix = ""; //$NON-NLS-1$
	public String titleSuffix = ""; //$NON-NLS-1$
	public ResNode node; // node this frame is linked to

	public ResourceFrame(R res, ResNode node)
		{
		super("",true,true,true,true); //$NON-NLS-1$
		this.res = res;
		this.node = node;
		resOriginal = (R) res.copy(false,null);
		setTitle(res.name);
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		name = new JTextField();
		name.setDocument(new NameDocument());
		name.setText(res.name);
		name.getDocument().addDocumentListener(this);
		save = new JButton();
		save.addActionListener(this);
		}

	public abstract void updateResource();

	public abstract void revertResource();

	public abstract boolean resourceChanged();

	public void changedUpdate(DocumentEvent e)
		{
		// Not used
		}

	public void insertUpdate(DocumentEvent e)
		{
		if (e.getDocument() == name.getDocument())
			{
			res.name = name.getText();
			setTitle(name.getText());
			node.setUserObject(name.getText());
			LGM.tree.updateUI();
			}
		}

	public void removeUpdate(DocumentEvent e)
		{
		if (e.getDocument() == name.getDocument())
			{
			res.name = name.getText();
			setTitle(name.getText());
			node.setUserObject(name.getText());
			LGM.tree.updateUI();
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == save)
			{
			updateResource();
			dispose();
			}
		}

	public void setTitle(String title)
		{
		super.setTitle(titlePrefix + title + titleSuffix);
		}

	public void dispose()
		{
		super.dispose();
		node.frame = null;// allows a new frame to open
		}

	protected void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING)
			{
			if (resourceChanged())
				{
				switch (JOptionPane.showConfirmDialog(LGM.frame,String.format(Messages
						.getString("ResourceFrame.KEEPCHANGES"),res.name),Messages //$NON-NLS-1$
						.getString("ResourceFrame.KEEPCHANGES_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION)) //$NON-NLS-1$
					{
					case 0: // yes
						updateResource();
						node.setUserObject(res.name);
						dispose();
						LGM.tree.updateUI();
						break;
					case 1: // no
						revertResource();
						node.setUserObject(resOriginal.name);
						dispose();
						LGM.tree.updateUI();
						break;
					}
				}
			else
				{
				updateResource();
				dispose();
				}
			}
		super.fireInternalFrameEvent(id);
		}
	}
