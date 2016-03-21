/*
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;

public abstract class RevertableMDIFrame extends MDIFrame
	{
	private static final long serialVersionUID = 1L;

	public RevertableMDIFrame(String title, boolean functional)
		{
		super(title,functional,functional,functional,functional);
		}

	public RevertableMDIFrame(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable)
		{
		super(title,resizable,closable,maximizable,iconifiable);
		}

	public abstract boolean resourceChanged();

	public abstract void updateResource(boolean commit);

	public abstract void setResourceChanged();

	public abstract void revertResource();

	public abstract String getConfirmationName();

	protected void close()
		{
		super.doDefaultCloseAction();
		}

	public void doDefaultCloseAction(final Runnable runnable)
		{
		if (!resourceChanged())
			{
			revertResource();
			close();
			if (runnable != null) runnable.run();
			return;
			}

		// prevent a race condition from ConfirmDialog's buttons leading to NPE.
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
					{
					int ret = JOptionPane.showConfirmDialog(LGM.frame,
							Messages.format("RevertableMDIFrame.KEEPCHANGES",getConfirmationName()), //$NON-NLS-1$
							Messages.getString("RevertableMDIFrame.KEEPCHANGES_TITLE"), //$NON-NLS-1$
							JOptionPane.YES_NO_CANCEL_OPTION);
					if (ret == JOptionPane.YES_OPTION)
						{
						updateResource(false);
						setResourceChanged();
						close();
						}
					else if (ret == JOptionPane.NO_OPTION)
						{
						revertResource();
						close();
						}
						if (runnable != null) runnable.run();
					}
			});
		}

	@Override
	public void doDefaultCloseAction()
		{
			doDefaultCloseAction(null);
		}
	}
