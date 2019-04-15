/*
 * Copyright (C) 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateTrigger;

public class FileChangeMonitor implements Runnable
	{
	private static final int POLL_INTERVAL = 1000;

	public enum Flag
		{
		CHANGED,DELETED
		}

	private static ScheduledExecutorService monitorService = Executors.newSingleThreadScheduledExecutor();

	public final File file;
	public final Executor executor;

	private final UpdateRunnable changedRunnable, deletedRunnable;
	private final ScheduledFuture<?> future;

	private final UpdateTrigger trigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,trigger);

	public FileChangeMonitor(File f, Executor e)
		{
		if (!f.exists()) throw new IllegalArgumentException();
		file = f;
		executor = e;
		changedRunnable = new UpdateRunnable(new FileUpdateEvent(updateSource,Flag.CHANGED));
		deletedRunnable = new UpdateRunnable(new FileUpdateEvent(updateSource,Flag.DELETED));
		lastModified = file.lastModified();
		length = file.length();
		future = monitorService.scheduleWithFixedDelay(this,POLL_INTERVAL,POLL_INTERVAL,
				TimeUnit.MILLISECONDS);
		}

	public FileChangeMonitor(String f, Executor e)
		{
		this(new File(f),e);
		}

	public void stop()
		{
		future.cancel(false);
		}

	private long lastModified, length;
	private boolean changed;

	public void run()
		{
		if (!file.exists())
			{
			executor.execute(deletedRunnable);
			future.cancel(false);
			return;
			}
		long m = file.lastModified();
		long l = file.length();
		if (m != lastModified || l != length)
			{
			changed = true;
			lastModified = m;
			length = l;
			}
		else if (changed)
			{
			executor.execute(changedRunnable);
			changed = false;
			}
		}

	public class FileUpdateEvent extends UpdateEvent
		{
		public final Flag flag;

		public FileUpdateEvent(UpdateSource s, Flag f)
			{
			super(s);
			flag = f;
			}
		}

	private class UpdateRunnable implements Runnable
		{
		public final FileUpdateEvent event;

		public UpdateRunnable(FileUpdateEvent e)
			{
			event = e;
			}

		public void run()
			{
			trigger.fire(event);
			}
		}
	}
