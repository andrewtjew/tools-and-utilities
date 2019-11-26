package TestApplication;

import org.nova.annotations.Description;

@Description("Class doc")
public class Outer
{
	@Description("name of user")
	String name;
	
	@Description("identifier of user")
	int id;
	
	@Description("arrays")
	long[] ids;


	@Description("inner object")
	Inner inner;
}
