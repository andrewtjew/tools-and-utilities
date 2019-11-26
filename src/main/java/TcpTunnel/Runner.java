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
package TcpTunnel;

import java.io.InputStream;

public class Runner
{
	public static void read(InputStream inputStream,byte[] bytes,int expected) throws Exception
	{
		int total=0;
		while (total<expected)
		{
			int size=inputStream.read(bytes, total, expected-total);
			if (size<0)
			{
				throw new Exception();
			}
			total+=size;
		}
	}
	
	static public void main(String[] args) throws Throwable
	{
		String tunnelHost=null;
		int tunnelPort=0;
		String clientHost=null;
		int clientPort=0;
		int serverPort=0;
		
		for (String arg:args)
		{
			String[] kv=arg.split("=");
			switch (kv[0])
			{
			case "tunnelHost":
				tunnelHost=kv[1];
				break;
				
			case "clientHost":
				clientHost=kv[1];
				break;
				
			case "tunnelPort":
				tunnelPort=Integer.parseInt(kv[1]);
				break;
				
			case "clientPort":
				clientPort=Integer.parseInt(kv[1]);
				break;
				
			case "serverPort":
				serverPort=Integer.parseInt(kv[1]);
				break;
				
			}
		}
		
		boolean running=false;
		if ((serverPort>0)&&(tunnelPort>0))
		{
			Server server=new Server(serverPort, tunnelPort);
			server.start();
			running=true;
		}
		if ((tunnelHost!=null)&&(tunnelPort>0)&&(clientPort>0)&&(clientHost!=null))
		{
			new Client(clientHost,clientPort,tunnelHost,tunnelPort,1000).start();
			running=true;
		}
		if (running==true)
		{
			Object lock=new Object();
			synchronized (lock)
			{
				lock.wait();
			}
		}
	}
}
