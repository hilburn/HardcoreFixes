package hardcorefixes.asm;

import hardcorefixes.threading.Search;
import hardcorequesting.client.interfaces.GuiEditMenuItem;

public class FixesHooks
{
    public static void updateSearch(GuiEditMenuItem menu, String search)
    {
        Thread thread = new Thread(new Search(search, menu));
        thread.start();
    }
}
