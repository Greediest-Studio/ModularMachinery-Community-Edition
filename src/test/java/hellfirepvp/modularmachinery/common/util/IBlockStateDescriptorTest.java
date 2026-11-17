package hellfirepvp.modularmachinery.common.util;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class IBlockStateDescriptorTest {

    @BeforeClass
    public static void initializeMinecraft() {
        Bootstrap.register();
    }

    @Before
    @After
    public void clearPool() {
        IBlockStateDescriptor.clearPool();
    }

    @Test
    public void 相同方块状态及复制品共用描述符() {
        IBlockState state = Blocks.STONE.getDefaultState();
        IBlockStateDescriptor canonical = IBlockStateDescriptor.of(state);

        assertSame(canonical, IBlockStateDescriptor.of(state));
        assertSame(canonical, canonical.copy().canonicalize());
        assertNotSame(canonical, IBlockStateDescriptor.of(Blocks.DIRT.getDefaultState()));
    }

    @Test
    public void 多状态列表保留顺序和去重语义() {
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState dirt = Blocks.DIRT.getDefaultState();
        IBlockStateDescriptor canonical = descriptor(Arrays.asList(stone, dirt)).canonicalize();

        assertSame(canonical, canonical.copy().canonicalize());
        assertNotSame(canonical, descriptor(Arrays.asList(dirt, stone)).canonicalize());
        assertEquals(Arrays.asList(stone, dirt), canonical.getApplicable());

        IBlockStateDescriptor block = IBlockStateDescriptor.of(Blocks.LOG);
        assertTrue(block.getApplicable().size() > 1);
        assertSame(block, block.copy().canonicalize());
    }

    @Test
    public void 并发首次加入及重复命中始终返回同一实例() throws Exception {
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState dirt = Blocks.DIRT.getDefaultState();
        List<List<IBlockState>> stateLists = Arrays.asList(
            Collections.singletonList(stone), Collections.singletonList(dirt),
            Arrays.asList(stone, dirt), Arrays.asList(dirt, stone));
        int workers = 16;
        CyclicBarrier start = new CyclicBarrier(workers);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            List<Future<IBlockStateDescriptor[]>> results = new ArrayList<>();
            for (int worker = 0; worker < workers; worker++) {
                results.add(executor.submit(() -> {
                    IBlockStateDescriptor[] canonical = new IBlockStateDescriptor[stateLists.size()];
                    start.await(10, TimeUnit.SECONDS);
                    for (int iteration = 0; iteration < 1000; iteration++) {
                        for (int key = 0; key < stateLists.size(); key++) {
                            IBlockStateDescriptor descriptor = descriptor(stateLists.get(key)).canonicalize();
                            if (iteration == 0) {
                                canonical[key] = descriptor;
                            } else {
                                assertSame(canonical[key], descriptor);
                            }
                        }
                    }
                    return canonical;
                }));
            }

            IBlockStateDescriptor[] canonical = results.get(0).get(10, TimeUnit.SECONDS);
            for (Future<IBlockStateDescriptor[]> result : results) {
                IBlockStateDescriptor[] actual = result.get(10, TimeUnit.SECONDS);
                for (int key = 0; key < canonical.length; key++) {
                    assertSame(canonical[key], actual[key]);
                    assertEquals(stateLists.get(key), actual[key].getApplicable());
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void 清池后新实例成为规范实例() {
        IBlockState state = Blocks.STONE.getDefaultState();
        IBlockStateDescriptor old = IBlockStateDescriptor.of(state);
        IBlockStateDescriptor.clearPool();

        IBlockStateDescriptor fresh = IBlockStateDescriptor.of(state);
        assertNotSame(old, fresh);
        assertEquals(old, fresh);
        assertSame(fresh, old.canonicalize());
    }

    @Test
    public void 有方向的方块旋转四次回到原描述符() {
        IBlockState north = Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
        IBlockStateDescriptor original = IBlockStateDescriptor.of(north);
        IBlockStateDescriptor rotated = original;
        EnumFacing facing = EnumFacing.NORTH;
        for (int turn = 0; turn < 4; turn++) {
            AtomicBoolean changed = new AtomicBoolean(false);
            rotated = rotated.copyRotateYCCW(changed);
            facing = facing.rotateYCCW();

            assertTrue(changed.get());
            assertEquals(facing, rotated.getApplicable().get(0).getValue(BlockStairs.FACING));
            assertSame(rotated, rotated.copy().canonicalize());
        }
        assertSame(original, rotated);
        assertSame(north, original.getApplicable().get(0));
    }

    @Test
    public void 无方向的方块旋转不改变累计标记() {
        IBlockStateDescriptor original = IBlockStateDescriptor.of(Blocks.STONE.getDefaultState());
        AtomicBoolean changed = new AtomicBoolean(false);
        assertSame(original, original.copyRotateYCCW(changed));
        assertFalse(changed.get());

        changed.set(true);
        assertSame(original, original.copyRotateYCCW(changed));
        assertTrue(changed.get());
    }

    private static IBlockStateDescriptor descriptor(List<IBlockState> states) {
        // 与生产构造方式一致：单状态用单例列表，多状态用引用列表。
        return states.size() == 1
            ? new IBlockStateDescriptor(states.get(0))
            : new IBlockStateDescriptor(new ReferenceArrayList<>(states));
    }
}
