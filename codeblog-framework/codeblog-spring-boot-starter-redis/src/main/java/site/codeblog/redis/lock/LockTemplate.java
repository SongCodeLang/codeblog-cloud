package site.codeblog.redis.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁统一入口（S7）：Redisson watchdog + finally unlock
 * <p>
 * 不传 leaseTime 由 watchdog 自动续期（进程存活即持锁，避免业务未完成锁先过期）；
 * finally 释放 + 只释放本线程持有的锁。约定：锁 key 一律
 * {@code codeblog:{service}:lock:{biz}}，禁止业务代码直用 RedissonClient 裸锁。
 *
 * @author codeblog
 */
@Slf4j
@RequiredArgsConstructor
public class LockTemplate {

    /**
     * 默认获取锁等待时长（秒）：拿不到快速失败，避免请求线程堆积
     */
    private static final long DEFAULT_WAIT_SECONDS = 3;

    private final RedissonClient redissonClient;

    /**
     * 加锁执行（有返回值），watchdog 续期
     */
    public <T> T execute(String lockKey, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(DEFAULT_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new IllegalStateException("获取分布式锁失败：" + lockKey);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取分布式锁被中断：" + lockKey, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 加锁执行（无返回值），watchdog 续期
     */
    public void execute(String lockKey, Runnable action) {
        execute(lockKey, () -> {
            action.run();
            return null;
        });
    }

}
