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
package vault;

import org.nova.security.SecureFileVault;

public class Main
{
	static public void main(String[] args) throws Throwable
	{
		if (args.length>0)
		{
			if (args[0].equals("-e"))
			{
				if (args.length>4)
				{
					String inputFileName=args[1];
					String outputFileName=args[2];
					String password=args[3];
					String salt=args[4];
					SecureFileVault.encrypt(password, salt, inputFileName, outputFileName);
					return;
				}
			}
			else if (args[0].equals("-d"))
			{
				if (args.length>4)
				{
					String inputFileName=args[1];
					String password=args[2];
					String salt=args[3];
					String key=args[4];
					SecureFileVault vault=new SecureFileVault(password, salt, inputFileName);
					System.out.println(vault.get(key));
					return;
				}
			}
		}
		System.out.println("-e <input> <output> <password> <salt>");
		System.out.println("-d <input> <password> <salt> <key>");
		return;
		
				
	}
}
