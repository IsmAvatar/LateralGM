/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.util;

import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

public final class SwingExecutor implements Executor
	{
	public static final SwingExecutor INSTANCE = new SwingExecutor();

	private SwingExecutor()
		{
		}

	public void execute(Runnable command)
		{
		SwingUtilities.invokeLater(command);
		}
	}
