package com.example.foodie.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Chạy N tác vụ thật sự đồng thời (không phải tuần tự) và thu kết quả của tất cả, dùng chung
 * cho mọi test Phase 6 -- tránh 7 test class tự chép lại y hệt bốn quy tắc bắt buộc: (1) đồng
 * bộ điểm xuất phát bằng CountDownLatch để mọi luồng thật sự cùng chạm DB một lúc, (2) trả về
 * List thay vì Future đơn lẻ để test assert trên KẾT QUẢ TỔNG HỢP thay vì luồng nào thắng,
 * (3) luôn có timeout ở cả doneGate.await lẫn Future.get để deadlock/khoá treo thật (InnoDB
 * lock_wait_timeout mặc định 50s) không làm treo build, (4) exception ở bất kỳ luồng nào cũng
 * được ném lại ngay (không nuốt) để không nhầm "race không xảy ra" với "race ném lỗi".
 */
public final class ConcurrentRace {

    private ConcurrentRace() {
    }

    public static <T> List<T> run(List<Callable<T>> tasks, long timeoutSeconds) throws InterruptedException {
        int n = tasks.size();
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(n);
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(pool.submit(() -> {
                    try {
                        startGate.await();
                        return task.call();
                    } finally {
                        doneGate.countDown();
                    }
                }));
            }

            startGate.countDown();

            if (!doneGate.await(timeoutSeconds, TimeUnit.SECONDS)) {
                throw new AssertionError(n + " luồng không hoàn thành trong " + timeoutSeconds
                        + "s -- nghi ngờ deadlock hoặc khoá bị treo");
            }

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(timeoutSeconds, TimeUnit.SECONDS));
            }
            return results;
        } catch (ExecutionException e) {
            throw new AssertionError("Một luồng ném exception ngoài dự kiến", e.getCause());
        } catch (TimeoutException e) {
            throw new AssertionError("Future.get() vượt quá " + timeoutSeconds + "s", e);
        } finally {
            pool.shutdownNow();
        }
    }
}
