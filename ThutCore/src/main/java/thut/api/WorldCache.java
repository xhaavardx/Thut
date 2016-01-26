package thut.api;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/** This class is used for thread-safe world access.
 * 
 * @author Thutmose */
public class WorldCache implements IBlockAccess
{
    public final World             world;
    ConcurrentHashMap<Long, ChunkCache> chunks = new ConcurrentHashMap<Long, ChunkCache>();

    public WorldCache(World world_)
    {
        world = world_;
    }

    void addChunk(Chunk chunk)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
        chunks.put(key, new ChunkCache(chunk));
    }

    void removeChunk(Chunk chunk)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
        chunks.remove(key);
    }

    public Chunk getChunk(int chunkX, int chunkZ)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
        return chunks.get(key).chunk;
    }

    @Override
    public boolean extendedLevelsInChunkCache()
    {
        return false;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        ChunkCache chunk = chunks.get(key);
        if (chunk == null) return null;
        return chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int p_175626_2_)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        ChunkCache chunk = chunks.get(key);
        if (chunk == null) return null;
        return chunk.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        return getBlockState(pos) == null || getBlockState(pos).getBlock().isAir(this, pos);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public WorldType getWorldType()
    {
        return world.getWorldInfo().getTerrainType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        ChunkCache chunk = chunks.get(key);
        if (chunk == null || chunk.isEmpty()) return _default;
        return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
    }

    public static class ChunkCache
    {
        Chunk chunk;
        private ExtendedBlockStorage[] storageArrays;
        
        public ChunkCache(Chunk chunk)
        {
            this.chunk = chunk;
            update();
        }
        
        public boolean isEmpty()
        {
            return false;
        }

        public IBlockState getBlockState(final BlockPos pos)
        {
            try
            {
                if (pos.getY() >= 0 && pos.getY() >> 4 < this.storageArrays.length)
                {
                    ExtendedBlockStorage extendedblockstorage = this.storageArrays[pos.getY() >> 4];

                    if (extendedblockstorage != null)
                    {
                        int j = pos.getX() & 15;
                        int k = pos.getY() & 15;
                        int i = pos.getZ() & 15;
                        return extendedblockstorage.get(j, k, i);
                    }
                }

                return Blocks.air.getDefaultState();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block state");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
                crashreportcategory.addCrashSectionCallable("Location", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return CrashReportCategory.getCoordinateInfo(pos);
                    }
                });
                throw new ReportedException(crashreport);
            }
        }

        public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType immediate)
        {
            return chunk.getTileEntity(pos, immediate);
        }

        public synchronized void update()
        {
            storageArrays = new ExtendedBlockStorage[chunk.getBlockStorageArray().length];
            for(int i = 0; i<storageArrays.length; i++)
            {
                if(chunk.getBlockStorageArray()[i]!=null)
                {
                    storageArrays[i] = new ExtendedBlockStorage(i, false);
                    storageArrays[i].setData(chunk.getBlockStorageArray()[i].getData().clone());
                }
            }
        }
        
    }
}
