package test.MinimalFramework;

import org.nova.frameworks.MinimalApplication;
import org.nova.logging.Item;
import org.nova.tracing.Trace;

public class Main
{
    public static void main(String[] args) throws Throwable
    {
        MinimalApplication application=new MinimalApplication();
        Trace parent=new Trace(application.getTraceManager(),"testParent");
        try (Trace trace=new Trace(application.getTraceManager(),parent,"testTrace"))
        {
            Exception exception=new Exception();
            application.getLogger().log("testItems",new Item("item1",123),new Item("String","hello"));
        }
    }
}
