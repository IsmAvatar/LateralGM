package org.lateralgm.main;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;

import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.ProjectFile.ResourceHolder;
import org.lateralgm.file.ProjectFile.SingletonResourceHolder;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.ResourceFrame.ResourceFrameFactory;

public class PluginManager
	{
	private final static ArrayList<URLClassLoader> classLoaders = new ArrayList<URLClassLoader>();

	public static void loadPlugins()
		{
		if (LGM.workDir == null) return;
		File dir = new File(LGM.workDir.getParent(),"plugins"); //$NON-NLS-1$
		if (!dir.exists()) dir = new File(LGM.workDir.getParent(),"Plugins"); //$NON-NLS-1$
		File[] ps = dir.listFiles(new CustomFileFilter(null,".jar")); //$NON-NLS-1$
		if (ps == null) return;
		for (File f : ps)
			{
			if (!f.exists()) continue;
			try
				{
				String pluginEntry = "LGM-Plugin"; //$NON-NLS-1$
				JarFile jar = new JarFile(f);
				Manifest mf = jar.getManifest();
				jar.close();
				String clastr = mf.getMainAttributes().getValue(pluginEntry);
				if (clastr == null)
					throw new Exception(Messages.format("LGM.PLUGIN_MISSING_ENTRY",pluginEntry)); //$NON-NLS-1$
				URLClassLoader ucl = new URLClassLoader(new URL[] { f.toURI().toURL() });
				ucl.loadClass(clastr).newInstance();
				classLoaders.add(ucl);
				}
			catch (Exception e)
				{
				String msgInd = "LGM.PLUGIN_LOAD_ERROR"; //$NON-NLS-1$
				LGM.showDefaultExceptionHandler(new Exception(Messages.format(msgInd,f.getName()), e));
				continue;
				}
			}
		}

	public static void addPluginResource(PluginResource pr)
		{
		ImageIcon i = pr.getIcon();
		if (i != null) ResNode.ICON.put(pr.getKind(),i);
		String p = pr.getPrefix();
		if (p != null) Prefs.prefixes.put(pr.getKind(),p);
		Resource.addKind(pr.getKind(),pr.getName3(),pr.getName(),pr.getPlural());
		LGM.currentFile.resMap.put(pr.getKind(),pr.getResourceHolder());
		ResourceFrame.factories.put(pr.getKind(),pr.getResourceFrameFactory());
		}

	public static interface PluginResource
		{
		Class<? extends Resource<?,?>> getKind();

		/** Can be null, in which case the default icon is used. */
		ImageIcon getIcon();

		String getName3();

		String getName();

		String getPlural();

		String getPrefix();

		ResourceHolder<?> getResourceHolder();

		ResourceFrameFactory getResourceFrameFactory();
		}

	public static abstract class SingletonPluginResource<T extends Resource<T,?>> implements
			PluginResource
		{
		public String getPlural()
			{
			return getName();
			}

		public String getPrefix()
			{
			return null;
			}

		public ResourceHolder<?> getResourceHolder()
			{
			return new SingletonResourceHolder<T>(getInstance());
			}

		public abstract T getInstance();
		}
	}
