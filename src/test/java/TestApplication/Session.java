package TestApplication;

public class Session
{
	private final String user;
	private final String sessionId;
	public Session(String user,String sessionId)
	{
		this.user=user;
		this.sessionId=sessionId;
	}
	public String getUser()
	{
		return user;
	}
	public String getSessionId()
	{
		return sessionId;
	}
	
}
