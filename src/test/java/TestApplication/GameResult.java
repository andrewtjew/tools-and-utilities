package TestApplication;

import org.nova.annotations.Description;

class GameResult
{
    @Description("Prize won in cents") 
    public long prize;
    
    @Description("Reelstops positions for display")
    public int[] stops;     
}