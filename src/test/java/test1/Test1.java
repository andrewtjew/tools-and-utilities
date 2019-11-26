package test1;

import java.util.Map.Entry;
import java.util.TreeMap;

class RangeModule
{
    TreeMap<Integer,Integer> map=new TreeMap<>();
    
    
    public void AddRange(int lower, int upper)
    {
        
        for (;;)
        {
            Entry<Integer,Integer> entry=map.higherEntry(lower);
            if (entry==null)
            {
                break;
            }
            if (entry.getKey()>upper)
            {
                break;
            }
            map.remove(entry.getKey());
            int value=entry.getValue();
            if (value>upper)
            {
                upper=value;
                break;
            }
        }
        
        Entry<Integer,Integer> entry=map.floorEntry(lower);
        if (entry!=null)
        {
            int key=entry.getKey();
            int value=entry.getValue();
            if (value>=upper)
            {
                return;
            }
            if (value>=lower)
            {
                map.remove(entry.getKey());
                lower=key;
            }
        }
        map.put(lower,upper);
    }

    public boolean QueryRange(int lower, int upper)
    {
        Entry<Integer,Integer> entry=map.floorEntry(lower);
        return entry==null? false:entry.getValue()>=upper;
    }

    public void DeleteRange(int lower, int upper)
    {
        Entry<Integer,Integer> entry=map.floorEntry(lower);
        if (entry!=null)
        {
            int value=entry.getValue();
            if (value>=lower)
            {
                map.put(entry.getKey(),lower-1);
                if (upper<value)
                {
                    map.put(upper+1,value);
                    return;
                }
            }
        }

        for (;;)
        {
            entry=map.ceilingEntry(lower);
            if (entry==null)
            {
                return;
            }
            int value=entry.getValue();
            map.remove(entry.getKey());
            if (value>=upper)
            {
                map.put(upper+1, value);
                return;
            }
        }
    }
}