package hardcorefixes.handlers;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import hardcorefixes.threading.Search;
import net.minecraft.client.Minecraft;

public class FMLHandler
{
    private static Tick tick = new Tick();

    @SubscribeEvent
    public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        Search.clear();
        FMLCommonHandler.instance().bus().register(tick);
    }

    public static class Tick
    {
        @SubscribeEvent
        public void tick(TickEvent.ClientTickEvent event)
        {
            if (Minecraft.getMinecraft().thePlayer != null)
            {
                Search.setItems();
                FMLCommonHandler.instance().bus().unregister(tick);
            }
        }
    }
}
