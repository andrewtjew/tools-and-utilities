package test.html.pages;

import java.io.File;

import org.nova.frameworks.MinimalApplication;

public class Main
{
	public static void main(String[] args) throws Throwable
	{
		MinimalApplication application=new MinimalApplication();
		File f=new File(".");
		System.out.println(f.getCanonicalPath());
	}
}
