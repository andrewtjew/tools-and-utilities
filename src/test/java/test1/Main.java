package test1;
import java.util.Scanner;
import java.io.*;


class Evaluate
{
    public static int _run_test(RangeModule module, String func, int arg1, int arg2)
    {
        int result = 0;
        if (func.equals("add"))
        {
            module.AddRange(arg1, arg2);
        }
        else if (func.equals("remove"))
        {
            module.DeleteRange(arg1, arg2);
        }
        else if (func.equals("query"))
        {
            boolean check = module.QueryRange(arg1, arg2);
            result = check ? 1 : -1;
        }

        return result;
    }
}


public class Main {
    
    public static void main(String[] args) throws IOException{
        Scanner in = new Scanner(System.in).useDelimiter("\n");
        //final String fileName = System.getenv("OUTPUT_PATH");
        String fileName="d:\\result.txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

        RangeModule rm = new RangeModule();
        String func, line;
        int result, arg1, arg2;
        String[] row;

        while (true) {
            line = in.nextLine();
            if (line.length() == 0 || line.charAt(0) == '#') {
                bw.write(line + "\r\n");
                continue;
            }

            row = line.split(",");
            func = row[0];
            arg1 = Integer.parseInt(row[1]);
            arg2 = Integer.parseInt(row[2]);

            result = Evaluate._run_test(rm, func, arg1, arg2);

            if (result == 0) {
                bw.write(String.format("%s(%d,%d)", func, arg1, arg2));
                bw.flush();
            }
            else {
                bw.write(String.format("%s(%d,%d) == %s", func, arg1, arg2, (result > 0) ? "true" : "false"));
                bw.flush();
            }

            if (in.hasNextLine()) {
                bw.write("\r\n");
                bw.flush();
            }
            else {
                break;
            }
        }

        bw.close();
    }
}

