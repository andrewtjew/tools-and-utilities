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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.nova.utils.TypeUtils;

public class Server
{
	final private int serverPort;
	final private int tunnelPort;
	private Thread listenThread;
	private Thread tunnelThread;
	final private HashMap<Integer,Connection> connections; 
	private OutputStream tunnelOutputStream;
	private int number;
	static int BUFFER_SIZE = 65536;

	public Server(int serverPort, int tunnelPort) throws Throwable
	{
		this.serverPort = serverPort;
		this.tunnelPort = tunnelPort;
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
			this.listenThread = new Thread(() ->
			{
				try
				{
					serverThread();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			this.tunnelThread.start();
			this.listenThread.start();
		}
	}

	static class Connection implements Runnable
	{
		final private int number;
		final private Socket serverSocket;
		final private OutputStream tunnelOutputStream;
		final private OutputStream serverOutputStream;
		final private Server server;
		
		public Connection(Server server,int number,Socket serverSocket,OutputStream tunnelOutputStream) throws IOException
		{
			this.server=server;
			serverSocket.setReceiveBufferSize(BUFFER_SIZE);
			serverSocket.setSendBufferSize(BUFFER_SIZE);
			serverSocket.setTcpNoDelay(true);
			this.number=number;
			this.serverSocket=serverSocket;
			this.tunnelOutputStream=tunnelOutputStream;
			this.serverOutputStream=serverSocket.getOutputStream();
		}
		
		public void writeToClient(byte[] buffer,int size) throws IOException
		{
			this.serverOutputStream.write(buffer,0,size);
		}
		
		
		@Override
		public void run()
		{
			try
			{
				byte[] buffer = new byte[BUFFER_SIZE];
				byte[] header=new byte[8];
				InputStream serverInputStream = this.serverSocket.getInputStream();
				for (;;)
				{
					int size = serverInputStream.read(buffer); 
					if (size>=0)
					{
					    TypeUtils.bigEndianIntToBytes(number, header, 0);
					    TypeUtils.bigEndianIntToBytes(size, header, 4);
						this.tunnelOutputStream.write(header);
						this.tunnelOutputStream.write(buffer, 0, size);
					}
					else
					{
					    TypeUtils.bigEndianIntToBytes(number, header, 0);
					    TypeUtils.bigEndianIntToBytes(-1, header, 4);
						this.tunnelOutputStream.write(header);
					}
				}
			}
			catch (Throwable t)
			{
				try
				{
					this.serverSocket.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			System.out.println("Server: Connection close "+this.number);
			this.server.close(this.number);
		}
		
	}
	
	private void close(int number)
	{
		synchronized (this)
		{
			this.connections.remove(number);
		}
	}
	
	private void serverThread() throws IOException
	{
		for (;;)
		{
			ServerSocket acceptSocket = new ServerSocket(this.serverPort);
			try
			{
				for (;;)
				{
					System.out.println("Server: Server listen " + this.serverPort);
					Socket serverSocket = acceptSocket.accept();
					String host = serverSocket.getInetAddress().getHostAddress();
					synchronized (this)
					{
						if (this.tunnelOutputStream == null)
						{
							System.out.println("Server: No tunnel. Client rejected "+host);
							serverSocket.close();
							continue;
						}
					}
					Connection connection=new Connection(this,this.number, serverSocket, tunnelOutputStream);
					synchronized (this)
					{
						System.out.println("Server: new connection " + host+". Number "+this.number);
						this.connections.put(this.number, connection);
						new Thread(connection).start();
						this.number++;
					}
				}
			}
			catch (Throwable t)
			{
				acceptSocket.close();
			}
		}
	}

	private void tunnelThread() throws IOException
	{
		for (;;)
		{
			ServerSocket acceptSocket = new ServerSocket(this.tunnelPort);
			try
			{
				for (;;)
				{
					System.out.println("Server: Tunnel listen " + this.tunnelPort);
					Socket tunnelSocket = acceptSocket.accept();
					try
					{
						String host = tunnelSocket.getInetAddress().getHostAddress() + ":" + tunnelSocket.getPort();
						synchronized (this)
						{
							this.tunnelOutputStream=tunnelSocket.getOutputStream();
						}
						tunnelSocket.setReceiveBufferSize(BUFFER_SIZE);
						tunnelSocket.setSendBufferSize(BUFFER_SIZE);
						tunnelSocket.setTcpNoDelay(true);
						System.out.println("Server: Tunnel connect " + host);
						byte[] buffer = new byte[BUFFER_SIZE];
						InputStream tunnelInputStream = tunnelSocket.getInputStream();
						for (;;)
						{
							Runner.read(tunnelInputStream,buffer,8);
							int number=TypeUtils.bigEndianBytesToInt(buffer, 0);
							int size=TypeUtils.bigEndianBytesToInt(buffer, 4);
							Runner.read(tunnelInputStream,buffer,size);
							
							Connection connection;
							synchronized(this)
							{
								connection=this.connections.get(number);
							}
							if (connection==null)
							{
								System.out.println("Server: Fatal error "+number);
							}
							else
							{
								
								connection.writeToClient(buffer,size);
							}
						}
					}
					finally
					{
						System.out.println("Server: Tunnel closed");
						tunnelSocket.close();
					}
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				acceptSocket.close();
			}
		}
	}
}
