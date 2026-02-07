package ru.mikst74.mikstcraft.world.chunk;

import ru.mikst74.mikstcraft.settings.GameProperties;
import ru.mikst74.mikstcraft.util.DelayedRunnable;
import ru.mikst74.mikstcraft.util.array.FirstFitFreeListAllocator2;

import static java.lang.Math.max;
import static ru.mikst74.mikstcraft.render.buffers.PerFaceBuffers.enlargePerFaceBuffers;
import static ru.mikst74.mikstcraft.util.BackgroundExecutor.updateAndRenderRunnables;

/**
 * Created by Mikhail Krinitsyn on 10.01.2026
 */
public class FaceAllocator {
   public static final FirstFitFreeListAllocator2 allocator =
           new FirstFitFreeListAllocator2(16, new FirstFitFreeListAllocator2.OutOfCapacityCallback() {
//           new FirstFitFreeListAllocator2(1024 * 4, new FirstFitFreeListAllocator2.OutOfCapacityCallback() {
        public int onCapacityIncrease(int currentCapacity) {
            int newPerFaceBufferCapacity = max(currentCapacity << 1, GameProperties.INITIAL_PER_FACE_BUFFER_CAPACITY);
            updateAndRenderRunnables.add(new DelayedRunnable(() -> {
                enlargePerFaceBuffers(currentCapacity, newPerFaceBufferCapacity);
                return null;
            }, "Enlarge per-face buffers", 0));
            return newPerFaceBufferCapacity;
        }
    });

}
