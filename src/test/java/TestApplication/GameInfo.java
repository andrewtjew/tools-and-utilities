package TestApplication;

import org.nova.annotations.Description;

class GameInfo
{
    @Description("The name of the Game") 
    public String name;
    
    @Description("Denominations available")
    public int[] denominations;	    
}