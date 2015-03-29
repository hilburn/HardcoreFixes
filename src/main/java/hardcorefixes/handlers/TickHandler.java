package hardcorefixes.handlers;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import hardcorefixes.threading.Search;

public class TickHandler
{
    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent event)
    {
        if (event.player != null)
        {
            Search.setItems();
            FMLCommonHandler.instance().bus().unregister(this);
        }
    }
}
