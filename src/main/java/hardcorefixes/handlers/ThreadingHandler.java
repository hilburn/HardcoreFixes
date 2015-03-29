package hardcorefixes.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.client.interfaces.GuiEditMenuItem;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ThreadingHandler
{
    public static Map<GuiEditMenuItem, List<GuiEditMenuItem.Element>> handle = new LinkedHashMap<GuiEditMenuItem, List<GuiEditMenuItem.Element>>();

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent e)
    {
        if (!handle.isEmpty())
        {
            for (Map.Entry<GuiEditMenuItem, List<GuiEditMenuItem.Element>> entry : handle.entrySet())
            {
                setResult(entry.getKey(), entry.getValue());
            }
            handle.clear();
        }
    }

    public static void setResult(GuiEditMenuItem menu, List<GuiEditMenuItem.Element> stackList)
    {
        getSearchItems(menu).clear();
        getSearchItems(menu).addAll(stackList);
    }

    public static List<GuiEditMenuItem.Element> getSearchItems(GuiEditMenuItem menu)
    {
        return new ArrayList<GuiEditMenuItem.Element>();
    }
}
