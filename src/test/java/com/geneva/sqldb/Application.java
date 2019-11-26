package com.geneva.sqldb;

import org.nova.frameworks.MinimalApplication;
import org.nova.sqldb.ColumnNameAlias;

public class Application extends MinimalApplication
{
	static public class Test
	{
		@ColumnNameAlias("a")
		String ab;
		int b;
	}

	public Application() throws Throwable
	{
		super();
		// TODO Auto-generated constructor stub
	}

	static public void main(String[] args) throws Throwable
	{
		Application app=new Application();
		
	}

}
