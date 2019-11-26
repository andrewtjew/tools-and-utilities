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
package TestDeploy;


import org.nova.configuration.Configuration;
import org.nova.frameworks.CoreEnvironment;
import org.nova.frameworks.ServerApplication;
import org.nova.frameworks.ServerApplicationRunner;
import org.nova.http.server.GzipContentDecoder;
import org.nova.http.server.GzipContentEncoder;
import org.nova.http.server.HttpServer;
import org.nova.http.server.JSONContentReader;
import org.nova.http.server.JSONContentWriter;
import org.nova.http.server.JSONPatchContentReader;
import org.nova.http.server.annotations.ContentDecoders;
import org.nova.http.server.annotations.ContentEncoders;
import org.nova.http.server.annotations.ContentReaders;
import org.nova.http.server.annotations.ContentWriters;
import org.nova.http.server.annotations.GET;
import org.nova.http.server.annotations.Path;
import org.nova.sqldb.Connector;
import org.nova.sqldb.MySqlConfiguration;
import org.nova.tracing.Trace;

@ContentDecoders(GzipContentDecoder.class)
@ContentEncoders(GzipContentEncoder.class)
@ContentReaders({JSONContentReader.class,JSONPatchContentReader.class})
@ContentWriters(JSONContentWriter.class)

public class Main extends ServerApplication
{
	private Connector sqlServerConnector;
	
    public static void main(String[] args) throws Throwable
    {
        new ServerApplicationRunner().run(args,(coreEnvironment,operatorServer)->{return new Main(coreEnvironment,operatorServer);});
    }

    public Main(CoreEnvironment coreEnvironment,HttpServer operatorServer) throws Throwable
    {
		super("TestDeploy",coreEnvironment,operatorServer);
		Configuration configuration=coreEnvironment.getConfiguration();
		
		{
			String host=configuration.getValue("Database.host","localhost");
			int port=configuration.getIntegerValue("Database.port",3306);
			String user=configuration.getValue("Database.user","root");
			String password=configuration.getValue("Database.password","mysql12mysql12");
			int poolSize=configuration.getIntegerValue("Database.poolSize",10);
			int connectionKeepAlive=configuration.getIntegerValue("Database.connectionKeepAlive",10000);
			MySqlConfiguration mysqlConfiguration=new MySqlConfiguration(host,port,"jobsend",poolSize,connectionKeepAlive);
			
		}
		
		this.getOperatorServer().registerHandlers(this);
	}

	/*
	@GET
	@Path("/test/json/{path}")
	@Consumes("application/json")
	@Produces("application/json")
	@Description("This is a test doc")
	public Response<JString> testJson(@PathParam("path") @Description("test param") int path,@HeaderParam("header") @Default("hello") @Description("header doc") String header
			,@QueryParam("q") @Default("1") int queryParam,@ContentParam Request input) throws Throwable
	{
		return new Response<JString>(new JString("hello"));
	}
	*/

	
	@GET
	@Path("/test/test")
	public void exception() throws Exception
	{
		throw new Exception("Test");
	}

	@GET
	@Path("/test/sleep")
	public void sleep() throws Exception
	{
		Thread.currentThread().sleep(100000);
	}


    @Override
    public void onStart(Trace parent) throws Throwable
    {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void onStop()
    {
    }
	
}
	

