package hardcorefixes.threading;

import hardcorefixes.HardcoreFixes;
import hardcorefixes.handlers.ThreadingHandler;
import hardcorequesting.client.interfaces.GuiEditMenuItem;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Search implements Runnable
{
    public static List<SearchEntry> searchItems = new ArrayList<SearchEntry>();
    public static List<SearchEntry> searchFluids = new ArrayList<SearchEntry>();

    private String search;
    private GuiEditMenuItem menu;

    public Search(String search, GuiEditMenuItem menu)
    {
        this.search = search;
        this.menu = menu;
    }

    @Override
    public void run()
    {
        List<GuiEditMenuItem.Element> elements = new ArrayList<GuiEditMenuItem.Element>();
        if (!search.isEmpty())
        {
            Pattern pattern = Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE);
            boolean advanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
            for (SearchEntry entry : searchItems) entry.search(pattern, elements, advanced);
            if (showFluid(menu))
                for (SearchEntry entry : searchFluids) entry.search(pattern, elements, advanced);
        }
        setResult(this.menu, elements);
    }

    public static boolean showFluid(GuiEditMenuItem menu)
    {
        return false;
    }

    public static void setResult(GuiEditMenuItem menu, List<GuiEditMenuItem.Element> stackList)
    {
        ThreadingHandler.handle.put(menu, stackList);
    }

    public static void setItems()
    {
        if (searchItems.isEmpty())
        {
            List<ItemStack> stacks = new ArrayList<ItemStack>();
            for (Object anItemRegistry : Item.itemRegistry)
            {
                try
                {
                    Item item = (Item)anItemRegistry;
                    if (HardcoreFixes.hideFluidBlocks && item instanceof ItemBlock)
                    {
                        ItemBlock itemBlock = (ItemBlock)item;
                        if (itemBlock.field_150939_a == Blocks.lava || itemBlock.field_150939_a == Blocks.water || itemBlock.field_150939_a instanceof BlockLiquid || itemBlock.field_150939_a instanceof IFluidBlock)
                            continue;
                    }
                    item.getSubItems(item, item.getCreativeTab(), stacks);
                } catch (Exception ignore)
                {
                }
            }
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            for (ItemStack stack : stacks)
            {
                try
                {
                    List tooltipList = stack.getTooltip(player, false);
                    List advTooltipList = stack.getTooltip(player, true);
                    String searchString = "";
                    for (Object string : tooltipList)
                    {
                        if (string != null)
                            searchString += string + "\n";
                    }
                    String advSearchString = "";
                    for (Object string : advTooltipList)
                    {
                        if (string != null)
                            advSearchString += string + "\n";
                    }
                    searchItems.add(new SearchEntry(searchString, advSearchString, getItemElement(stack)));
                } catch (Throwable ignore)
                {
                }
            }
            for (Fluid fluid : FluidRegistry.getRegisteredFluids().values())
            {
                String search = fluid.getLocalizedName();
                searchFluids.add(new SearchEntry(search, search, getFluidElement(fluid)));
            }
        }
    }

    public static void clear()
    {
        searchFluids.clear();
        searchItems.clear();
    }

    public static GuiEditMenuItem.ElementItem getItemElement(ItemStack stack)
    {
        return null;
    }

    public static GuiEditMenuItem.ElementFluid getFluidElement(Fluid fluid)
    {
        return null;
    }

    public static class SearchEntry
    {
        private String toolTip;
        private String advToolTip;
        private GuiEditMenuItem.Element element;

        public SearchEntry(String searchString, String advSearchString, GuiEditMenuItem.Element element)
        {
            this.toolTip = searchString;
            this.advToolTip = advSearchString;
            this.element = element;
        }

        public void search(Pattern pattern, List<GuiEditMenuItem.Element> stackList, boolean advanced)
        {
            if (pattern.matcher(advanced? advToolTip : toolTip).find()) stackList.add(element);
        }
    }
}
