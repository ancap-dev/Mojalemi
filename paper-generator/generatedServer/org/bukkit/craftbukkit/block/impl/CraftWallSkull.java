package org.bukkit.craftbukkit.block.impl;

import com.google.common.base.Preconditions;
import io.papermc.paper.generated.GeneratedFrom;
import java.util.Set;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.SkullWall;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

@GeneratedFrom("1.21")
public class CraftWallSkull extends CraftBlockData implements SkullWall {
    private static final DirectionProperty FACING = WallSkullBlock.FACING;

    private static final BooleanProperty POWERED = WallSkullBlock.POWERED;

    public CraftWallSkull(BlockState state) {
        super(state);
    }

    @Override
    public BlockFace getFacing() {
        return this.get(FACING, BlockFace.class);
    }

    @Override
    public void setFacing(final BlockFace blockFace) {
        Preconditions.checkArgument(blockFace != null, "blockFace cannot be null!");
        Preconditions.checkArgument(blockFace.isCartesian() && blockFace.getModY() == 0, "Invalid face, only cartesian horizontal face are allowed for this property!");
        this.set(FACING, blockFace);
    }

    @Override
    public Set<BlockFace> getFaces() {
        return this.getValues(FACING, BlockFace.class);
    }

    @Override
    public boolean isPowered() {
        return this.get(POWERED);
    }

    @Override
    public void setPowered(final boolean powered) {
        this.set(POWERED, powered);
    }
}
