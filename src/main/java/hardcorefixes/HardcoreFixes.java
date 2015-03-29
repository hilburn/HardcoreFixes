package hardcorefixes;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

import hardcorefixes.handlers.TickHandler;
import hardcorefixes.reference.Reference;
import hardcorefixes.handlers.ThreadingHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;


@Mod(modid = Reference.ID, name = Reference.NAME, version = Reference.VERSION_FULL, dependencies = "required-after:HardcoreQuesting")
public class HardcoreFixes
{
    @Mod.Instance(value = Reference.ID)
    public static HardcoreFixes INSTANCE;

    @Mod.Metadata(Reference.ID)
    public static ModMetadata metadata;

    public static Logger log = LogManager.getLogger(Reference.ID);

    public static boolean hideFluidBlocks = true;

    @NetworkCheckHandler
    public final boolean networkCheck(Map<String, String> remoteVersions, Side side)
    {
        return true;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        metadata = hardcorefixes.reference.Metadata.init(metadata);
        if (event.getSide() == Side.CLIENT)
        {
            FMLCommonHandler.instance().bus().register(new TickHandler());
            MinecraftForge.EVENT_BUS.register(new ThreadingHandler());
            config(event.getSuggestedConfigurationFile());
        }
    }

    public static void config(File file)
    {
        Configuration config = new Configuration(file);

        Property hide = config.get("General", "hideFluidBlocks", hideFluidBlocks);
        hide.comment = "Setting true hides Fluid Block items from searches.";
        hideFluidBlocks = hide.getBoolean();

        config.save();
    }
}