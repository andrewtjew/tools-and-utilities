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


import org.nova.frameworks.CoreEnvironment;
import org.nova.frameworks.ServerApplication;
import org.nova.frameworks.ServerApplicationRunner;
import org.nova.html.operator.AjaxQueryResultWriter;
import org.nova.http.server.GzipContentDecoder;
import org.nova.http.server.GzipContentEncoder;
import org.nova.http.server.HtmlContentWriter;
import org.nova.http.server.HttpServer;
import org.nova.http.server.JSONContentReader;
import org.nova.http.server.JSONPatchContentReader;
import org.nova.http.server.Response;
import org.nova.http.server.annotations.ContentDecoders;
import org.nova.http.server.annotations.ContentEncoders;
import org.nova.http.server.annotations.ContentReaders;
import org.nova.http.server.annotations.ContentWriters;
import org.nova.http.server.annotations.POST;
import org.nova.http.server.annotations.Path;
import org.nova.http.server.annotations.QueryParam;
import org.nova.tracing.Trace;

@ContentDecoders(GzipContentDecoder.class)
@ContentEncoders(GzipContentEncoder.class)
@ContentReaders({JSONContentReader.class,JSONPatchContentReader.class})
@ContentWriters({AjaxQueryResultWriter.class,HtmlContentWriter.class})
public class Main extends ServerApplication
{
	final private SessionManager sessionManager;
    public static void main(String[] args) throws Throwable
    {
        new ServerApplicationRunner().run(args,(coreEnvironment,operatorServer)->{return new Main(coreEnvironment,operatorServer);});
    }

	public Main(CoreEnvironment coreEnvironment,HttpServer operatorServer) throws Throwable
	{
		super("DirectoryService",coreEnvironment,operatorServer);
		this.sessionManager=new SessionManager(this.getTimerScheduler(), 10000, 10);
        this.getOperatorServer().getTransformers().add(new AuthenticationFilter(this.sessionManager,"http://localhost"));
        this.getOperatorServer().getTransformers().add(new HtmlContentWriter());
		this.getOperatorVariableManager().register(this);
		this.getOperatorServer().registerHandlers(this);
	}
	
	@POST
	@Path("/authenticate")
	public Response<Void> test(Trace parent,@QueryParam("user") String user,@QueryParam("password") String password) throws Throwable
	{
		String token=System.currentTimeMillis()+"Token";
		this.sessionManager.put(token, new Session(user, token));
		Response<Void> response=new Response<Void>(200);
		response.addHeader("X-SessionToken", token);
		return response;
				
	}


    @Override
    public void onStart(Trace parent) throws Throwable
    {
        // TODO Auto-generated method stub
        
    }
    public void onStop()
    {
    }
}
	

