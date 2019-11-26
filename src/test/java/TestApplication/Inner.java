package TestApplication;

import org.nova.annotations.Description;

public class Inner
{
	String value="world";
	
	@Description("recursive")
	Outer request;

	@Description("inner recursive")
	Inner inner;
}
