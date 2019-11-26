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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.nova.utils.TypeUtils;

public class Client
{
	static int BUFFER_SIZE = 65536;

	final private int port;
	final private String host;
	final private int tunnelPort;
	final private String tunnelHost;
	private Thread tunnelThread;
	OutputStream tunnelOutputStream;
	private long pause;
	final private HashMap<Integer,Connection> connections;

	static class Connection implements Runnable
	{
		final private Socket clientSocket;
		final private Client client;
		final private OutputStream tunnelOutputStream;
		final private OutputStream clientOutputStream;
		final private int number;
		Connection(int number,Client client,String host,int port,OutputStream tunnelOutputStream) throws UnknownHostException, IOException
		{
			
			System.out.println("Client: Connecting to client...");
			this.clientSocket=new Socket(host,port);
			System.out.println("Client: Connected to client. Number "+number);

			this.clientSocket.setTcpNoDelay(true);
			this.clientSocket.setReceiveBufferSize(BUFFER_SIZE);
			this.clientSocket.setSendBufferSize(BUFFER_SIZE);
			this.clientOutputStream=clientSocket.getOutputStream();
			this.client=client;
			this.tunnelOutputStream=tunnelOutputStream;
			this.number=number;
		}
		@Override
		public void run()
		{
			try
			{
				byte[] buffer = new byte[BUFFER_SIZE];
				byte[] header = new byte[8];
				InputStream clientInputStream=this.clientSocket.getInputStream();
				for (int size = clientInputStream.read(buffer); size >= 0; size = clientInputStream.read(buffer))
				{
				    TypeUtils.bigEndianIntToBytes(this.number, header, 0);
				    TypeUtils.bigEndianIntToBytes(size, header, 4);
					this.tunnelOutputStream.write(header);
					this.tunnelOutputStream.write(buffer,0,size);
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			this.client.close(number);
			System.out.println("Client: Disconnected "+this.number);
		}

		public void close() throws IOException
		{
			this.clientSocket.close();
		}
		public void sendToClient(byte[] buffer,int size) throws IOException
		{
			this.clientOutputStream.write(buffer,0,size);
		}
	}
	
	public Client(String host, int port, String tunnelHost, int tunnelPort, long pause) throws Throwable
	{
		this.port = port;
		this.host = host;
		this.tunnelPort = tunnelPort;
		this.tunnelHost = tunnelHost;
		this.pause = pause;
		this.connections=new HashMap<>();
	}

	public void start()
	{
		synchronized (this)
		{
			this.tunnelThread = new Thread(() ->
			{
				try
				{
					tunnelThread();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			this.tunnelThread.start();
		}
	}

	public void writeToTunnel(byte[] header,byte[] buffer,int size) throws IOException
	{
		synchronized (this.tunnelOutputStream)
		{
			this.tunnelOutputStream.write(header);
			this.tunnelOutputStream.write(buffer,0,size);
		}
	}

	public void close(int number)
	{
		synchronized(this)
		{
			this.connections.remove(number);
		}
	}
	private void tunnelThread() throws IOException, InterruptedException
	{
		for (;;)
		{
			try
			{
				System.out.println("Client: Connecting to tunnel...");
				try (Socket tunnelSocket=new Socket(this.tunnelHost,this.tunnelPort))
				{
					tunnelSocket.setTcpNoDelay(true);
					tunnelSocket.setReceiveBufferSize(BUFFER_SIZE);
					tunnelSocket.setSendBufferSize(BUFFER_SIZE);
					byte[] buffer=new byte[BUFFER_SIZE];
					System.out.println("Client: Connected to tunnel");
					InputStream tunnelInputStream=tunnelSocket.getInputStream();
					this.tunnelOutputStream=tunnelSocket.getOutputStream();

					for (;;)
					{
						Runner.read(tunnelInputStream,buffer,8);
						int number=TypeUtils.bigEndianBytesToInt(buffer, 0);
						int size=TypeUtils.bigEndianBytesToInt(buffer, 4);
						Runner.read(tunnelInputStream,buffer,size);
						if (size>=0)
						{
							Connection connection;
							synchronized(this)
							{
								connection=this.connections.get(number);
								if (connection==null)
								{
									connection=new Connection(number,this, this.host,this.port,tunnelOutputStream);
									this.connections.put(number, connection);
									new Thread(connection).start();
								}
							}
							connection.sendToClient(buffer,size);
						}
						else
						{
							Connection connection;
							synchronized(this)
							{
								connection=this.connections.get(number);
							}
							if (connection!=null)
							{
								connection.close();
							}
						}
					}
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				System.out.println("Client: No tunnel");
			}
			Thread.sleep(this.pause);
		}
	}
}
