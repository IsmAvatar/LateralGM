package componentRes;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class CustomFileFilter extends FileFilter
	{
	private String extension;
	private String desc;

	public CustomFileFilter(String extension, String desc)
		{
		this.extension = extension;
		this.desc = desc;
		}

	public boolean accept(File f)
		{
		if (f.isDirectory()) return true;
		return f.getPath().endsWith(extension);
		}

	public String getDescription()
		{
		return desc;
		}
	}