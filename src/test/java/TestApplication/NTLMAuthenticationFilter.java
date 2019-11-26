package TestApplication;

import java.util.Base64;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.nova.http.server.Context;
import org.nova.http.server.Filter;
import org.nova.http.server.FilterChain;
import org.nova.http.server.Response;
import org.nova.tracing.Trace;

import jcifs.ntlmssp.Type3Message;
import waffle.windows.auth.IWindowsAccount;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

public class NTLMAuthenticationFilter extends Filter
{
	public NTLMAuthenticationFilter()
	{
	}


	static String TYPE2_RESPONSE="NTLM " + Base64.getEncoder().encodeToString(
			new byte[]{ (byte) 'N', (byte) 'T', (byte) 'L', (byte) 'M', (byte) 'S', (byte) 'S', (byte) 'P', 0, (byte) 2, 0, 0, 0, 0, 0, 0, 0, (byte) 40, 0, 0, 0,
			(byte) 1, (byte) 130, 0, 0, 0, (byte) 2, (byte) 2, (byte) 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

	
	
	@Override
	public Response<?> executeNext(Trace trace, Context context, FilterChain filterChain) throws Throwable
	{
		String authorization = context.getHttpServletRequest().getHeader("Authorization");
		if (authorization == null)
		{
			HttpServletResponse response = context.getHttpServletResponse();
			response.setHeader("WWW-Authenticate", "NTLM");
			return new Response<Void>(HttpStatus.UNAUTHORIZED_401);
		}
		if (authorization.startsWith("NTLM "))
		{

			
			byte[] bytes = Base64.getDecoder().decode(authorization.substring(5));
			if (bytes[8] == 1)
			{
				HttpServletResponse response = context.getHttpServletResponse();
				response.setHeader("WWW-Authenticate", TYPE2_RESPONSE);
				return new Response<Void>(HttpStatus.UNAUTHORIZED_401);
			}
			else if (bytes[8] == 3)
			{
				Type3Message type3 = new Type3Message(bytes);
				WindowsAuthProviderImpl waffle = new WindowsAuthProviderImpl();
				IWindowsAccount account=waffle.lookupAccount(type3.getUser());
				//Authenticate
				//NTCredentials credentials=new NTCredentials(message.getUser(), message.getDefaultPassword(), message.getWorkstation(),message.getDomain());
				context.setState(type3);
				return filterChain.next(trace, context);
			}
		}
		return new Response<Void>(HttpStatus.FORBIDDEN_403);
	}

}
