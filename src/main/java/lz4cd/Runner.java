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
package lz4cd;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

public class Runner
{
	public static void main(String[] args) throws IOException
	{
		if (args.length==0)
		{
			return;
		}
		String input=args[0];
		if (input.equals("-?")||(input.equals("-help")))
		{
            System.out.println("args: <file>.lz4 = decompress lz4 file to standard out.");
            System.out.println("args: <file>.lz4 <file> = decompress lz4 file to another file.");
            System.out.println("args: <file> <lz4 file> = compress file to lz4 file.");
            return;
        }
		if (input.lastIndexOf(".lz4")==input.length()-4)
		{
			if (args.length==1)
			{
				try (LZ4BlockInputStream inputStream=new LZ4BlockInputStream(new FileInputStream(input)))
				{
					byte[] buffer=new byte[65536];
					for (int size=inputStream.read(buffer);size>=0;size=inputStream.read(buffer))
					{
						System.out.write(buffer,0,size);
					}
				}
			}
			else
			{
				String output=args[1];
				try (LZ4BlockInputStream inputStream=new LZ4BlockInputStream(new FileInputStream(input)))
				{
					try(FileOutputStream outputStream=new FileOutputStream(output))
					{
						byte[] buffer=new byte[65536];
						for (int size=inputStream.read(buffer);size>=0;size=inputStream.read(buffer))
						{
							outputStream.write(buffer,0,size);
						}
						return;
					}
				}
			}
		}
		else
		{
			String output=args[1];
			try (FileInputStream inputStream=new FileInputStream(input))
			{
				try (LZ4BlockOutputStream outputStream=new LZ4BlockOutputStream(new FileOutputStream(output)))
				{
					byte[] buffer=new byte[65536];
					for (int size=inputStream.read(buffer);size>=0;size=inputStream.read(buffer))
					{
						outputStream.write(buffer,0,size);
					}
					return;
				}
			}
			
		}
				
		
	}
}
