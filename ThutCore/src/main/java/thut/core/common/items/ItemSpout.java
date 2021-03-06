package thut.core.common.items;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidContainerItem;
import thut.api.ThutItems;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class ItemSpout extends Item
{

    public ItemSpout()
    {
        super();
        this.setHasSubtypes(true);
        this.setUnlocalizedName("spout");
        this.setCreativeTab(ThutCore.tabThut);
        ThutItems.spout = this;
    }

    public ArrayList<ItemStack> getTanks(EntityPlayer player)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        for (ItemStack stack : player.inventory.mainInventory)
        {
            if (stack != null && stack.getItem() instanceof IFluidContainerItem)
            {
                IFluidContainerItem tank = (IFluidContainerItem) stack.getItem();
                if (tank.getFluid(stack) != null) ret.add(stack);
            }
        }

        return ret;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World worldObj, EntityPlayer player)
    {
        if(itemstack.getItemDamage()!=15) return itemstack;
        
        Vector3 v = Vector3.getNewVector();
        int range = 5;
        int num = 1;
        float power = 0.11f;
        for (int i = 0; i < num; i++)
        {
            v.set(player).addTo(range * (Math.random() - 0.5), range * (Math.random() - 0.5),
                    range * (Math.random() - 0.5));
            ExplosionCustom boom = new ExplosionCustom(worldObj, player, v, power);
            boom.doExplosion();
        }
        return itemstack;
    }

    // TODO move this to real method
    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ)
    {
        boolean ret = false;
        int toDrain = 0;
        Vector3 hit = Vector3.getNewVector().set(pos);
        Vector3 next = hit.offset(side);

        boolean full = !player.isSneaking();
        ArrayList<ItemStack> tanks = getTanks(player);
        if (tanks.size() == 0) return ret;

        if (next.getBlockMaterial(worldObj).isReplaceable())
        {
            for (ItemStack stack : tanks)
            {
                IFluidContainerItem tank = (IFluidContainerItem) stack.getItem();
                Fluid f1 = tank.getFluid(stack).getFluid();
                if (!f1.canBePlacedInWorld())
                {
                    continue;
                }
                Block b = f1.getBlock();
                if (b instanceof BlockFluidBase)
                {
                    BlockFluidBase block = (BlockFluidBase) b;
                    int maxMeta = block.getMaxRenderHeightMeta();

                    int metaDiff = full ? 16 : 1;

                    toDrain = maxMeta == 0 ? 1000 : (int) (metaDiff * 1000f / (maxMeta + 1));
                    next.setBlock(worldObj, b, metaDiff - 1, 3);
                    tank.drain(stack, toDrain, !player.capabilities.isCreativeMode);
                    break;
                }
                else
                {
                    tank.drain(stack, 1000, !player.capabilities.isCreativeMode);
                    next.setBlock(worldObj, b, 0, 3);
                }
            }
        }

        return ret;
    }
}
