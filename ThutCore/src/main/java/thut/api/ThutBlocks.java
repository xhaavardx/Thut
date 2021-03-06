package thut.api;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class ThutBlocks extends Blocks
{
    // Thut WorldGen Blocks
    public static Block dust;
    public static Block inactiveDust;

    // Thut Concrete Blocks
    public static Block liquidConcrete;
    public static Block liquidREConcrete;
    public static Block concrete;
    public static Block reConcrete;
    public static Block liquidAsphalt;
    public static Block solidAsphalt;
    public static Block coolCO2;
    public static Block warmCO2;

    public static Block rebar;
    public static Block limekiln;
    public static Block mixer;

    // Thut Tech Blocks
    public static Block liftRail;
    public static Block lift;

    public static Block   volcano;
    public static Block[] solidLavas = new Block[16];
    public static Block[] lavas      = new Block[16];

    private static HashSet<Block> allBlocks = new HashSet<Block>();

    public static HashSet<Block> getAllBlocks()
    {
        if (allBlocks.size() == 0)
        {
            initAllBlocks();
        }
        return allBlocks;
    }

    public static void initAllBlocks()
    {
        allBlocks.clear();
        for (int i = 0; i < 4096; i++)
        {
            if (Block.getBlockById(i) != null) allBlocks.add(Block.getBlockById(i));
        }
    }
}
