/*******************************************************************************
 * Copyright (C) 2017-2019 Kat Fung Tjew, All Rights Reserved
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package DirectoryService;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.nova.http.server.Context;
import org.nova.http.server.Filter;
import org.nova.http.server.FilterChain;
import org.nova.http.server.JSONContentReader;
import org.nova.http.server.JSONContentWriter;
import org.nova.http.server.Response;
import org.nova.http.server.annotations.ContentReaders;
import org.nova.http.server.annotations.ContentWriters;
import org.nova.tracing.Trace;

@ContentReaders(JSONContentReader.class)
@ContentWriters(JSONContentWriter.class)

public class AuthenticationFilter extends Filter
{
	final private SessionManager sessionManager;
	final private String directoryServiceEndPoint;
	public AuthenticationFilter(SessionManager sessionManager,String directoryServiceEndPoint)
	{
		this.sessionManager=sessionManager;
		this.directoryServiceEndPoint=directoryServiceEndPoint;
	}
	
	@Override
	public Response<?> executeNext(Trace trace, Context context, FilterChain filterChain) throws Throwable
	{
		String token=context.getHttpServletRequest().getHeader("X-Token");
		Session session=this.sessionManager.get(token);
		if (session==null)
		{
			HttpServletResponse response=context.getHttpServletResponse();
			response.addHeader("X-DirectoryService ", this.directoryServiceEndPoint);
			response.setStatus(HttpStatus.UNAUTHORIZED_401);
			return null;
		}
		context.setState(session);
		return filterChain.next(trace, context);
	}


}
