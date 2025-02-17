package com.brandon3055.draconicevolution.blocks.tileentity;

import com.brandon3055.brandonscore.api.power.OPStorage;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.inventory.TileItemStackHandler;
import com.brandon3055.brandonscore.lib.Vec3I;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.brandonscore.lib.datamanager.ManagedLong;
import com.brandon3055.brandonscore.lib.datamanager.ManagedVec3I;
import com.brandon3055.draconicevolution.api.crafting.IFusionInventory;
import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.brandonscore.api.power.IExtendedRFStorage;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.fusioncrafting.ICraftingInjector;
import com.brandon3055.draconicevolution.api.fusioncrafting.IFusionCraftingInventory;
import com.brandon3055.draconicevolution.blocks.machines.CraftingInjector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;

import static com.brandon3055.brandonscore.lib.datamanager.DataFlags.SAVE_NBT_SYNC_TILE;

/**
 * Created by brandon3055 on 10/06/2016.
 */
public class TileCraftingInjector extends TileBCore implements ICraftingInjector, IExtendedRFStorage {


    private final ManagedLong energy = register(new ManagedLong("energy", SAVE_NBT_SYNC_TILE));
    private final ManagedVec3I lastCorePos = register(new ManagedVec3I("last_core_p-os", new Vec3I(0, 0, 0), SAVE_NBT_SYNC_TILE));
//    public final ManagedByte facing = register(new ManagedByte("facing", SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE));
    public final ManagedBool singleItem = register(new ManagedBool("single_item", SAVE_NBT_SYNC_TILE));

    public IFusionInventory currentCraftingInventory = null;
    private int chargeSpeedModifier = 300;

    public TileItemStackHandler itemHandler = new TileItemStackHandler(1);
    private TechLevel techLevel;

    public TileCraftingInjector() {
        super(DEContent.tile_crafting_injector);

        capManager.setManaged("inventory", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemHandler).saveBoth().syncTile();
        itemHandler.setPerSlotLimit(() -> singleItem.get() ? 1 : 64); //TODO make sure this cant void items
        itemHandler.setContentsChangeListener(this::slotContentsChanged);

        capManager.set(CapabilityOP.OP, new OPStorage(0){
            @Override
            public long receiveOP(long maxReceive, boolean simulate) {
                return TileCraftingInjector.this.receiveEnergy(maxReceive, simulate);
            }

            @Override
            public boolean canReceive() {
                return true;
            }

            @Override
            public long getOPStored() {
                return getExtendedStorage();
            }

            @Override
            public long getMaxOPStored() {
                return getExtendedCapacity();
            }
        });
    }

    public TileCraftingInjector(TechLevel techLevel) {
        this();
        this.techLevel = techLevel;
    }

    @Override
    public void updateBlock() {
        super.updateBlock();
        super.tick();
    }

    //region IEnergy

    public long receiveEnergy(long maxReceive, boolean simulate) {
        validateCraftingInventory();
        if (currentCraftingInventory != null) {
            long maxRFPerTick = currentCraftingInventory.getIngredientEnergyCost() / chargeSpeedModifier;
            long maxAccept = Math.min(maxReceive, Math.min(currentCraftingInventory.getIngredientEnergyCost() - energy.get(), maxRFPerTick));

            if (!simulate) {
                energy.add(maxAccept);
            }

            super.tick();
            return (int) maxAccept;
        }

        return 0;
    }

//    @Override
//    public int getEnergyStored(Direction from) {
//        return (int) Math.min(Integer.MAX_VALUE, getExtendedStorage());
//    }
//
//    @Override
//    public int getMaxEnergyStored(Direction from) {
//        return (int) Math.min(Integer.MAX_VALUE, getExtendedCapacity());
//    }
//
//    @Override
//    public boolean canConnectEnergy(Direction from) {
//        return from != Direction.getFront(facing.get());
//    }
//
//    @Override
//    public boolean hasCapability(Capability<?> capability, Direction facing) {
//        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
//    }
//
//    @Override
//    public <T> T getCapability(Capability<T> capability, Direction facing) {
//        if (capability == CapabilityEnergy.ENERGY) {
//            return CapabilityEnergy.ENERGY.cast(new EnergyHandlerWrapper(this, facing));
//        }
//
//        return super.getCapability(capability, facing);
//    }

    //endregion

    //region ICraftingPedestal

    @Override
    public int getPedestalTier() {
        return techLevel.index;
    }

    @Override
    public ItemStack getStackInPedestal() {
        return itemHandler.getStackInSlot(0);
    }

    @Override
    public void setStackInPedestal(ItemStack stack) {
        itemHandler.setStackInSlot(0, stack);
    }

    @Override
    public boolean setCraftingInventory(IFusionInventory craftingInventory) {
        if (craftingInventory == null) {
            currentCraftingInventory = null;
            return false;
        }
        if (validateCraftingInventory() && !level.isClientSide) {
            return false;
        }
        currentCraftingInventory = craftingInventory;
        lastCorePos.set(new Vec3I(((TileEntity) craftingInventory).getBlockPos()));
        chargeSpeedModifier = 300 - (getPedestalTier() * 80);
        return true;
    }

    @Override
    public Direction getDirection() {
        return getBlockState().getValue(CraftingInjector.FACING);
    }

    @Override
    public long getInjectorCharge() {
        return energy.get();
    }

    private boolean validateCraftingInventory() {
        if (!getStackInPedestal().isEmpty() && currentCraftingInventory != null && currentCraftingInventory.craftingInProgress() && !((TileEntity) currentCraftingInventory).isRemoved()) {
            return true;
        }

        currentCraftingInventory = null;
        return false;
    }


    @Override
    public void onCraft() {
        if (currentCraftingInventory != null) {
            energy.zero();
        }
    }

    //endregion

    public void slotContentsChanged(int index) {
        setChanged();

        TileEntity tile = level.getBlockEntity(lastCorePos.get().getPos());
        if (tile instanceof IFusionCraftingInventory) {
            level.updateNeighborsAt(tile.getBlockPos(), tile.getBlockState().getBlock());
        }

        updateBlock();
    }

    @Override
    public long getExtendedStorage() {
        return energy.get();
    }

    @Override
    public long getExtendedCapacity() {
        validateCraftingInventory();
        if (currentCraftingInventory != null) {
            currentCraftingInventory.getIngredientEnergyCost();
        }
        return 0;
    }


    @Override
    public void writeExtraNBT(CompoundNBT compound) {
        compound.putByte("tech_level", (byte) techLevel.ordinal());
        super.writeExtraNBT(compound);
    }

    @Override
    public void readExtraNBT(CompoundNBT compound) {
        techLevel = TechLevel.values()[compound.getInt("tech_level")];
        super.readExtraNBT(compound);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1));
    }
}
