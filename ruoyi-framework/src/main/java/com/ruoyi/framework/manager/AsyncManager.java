package com.ruoyi.framework.manager;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.ruoyi.common.utils.Threads;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 异步任务管理器
 * 
 * @author ruoyi
 */
public class AsyncManager
{
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池
     */
    private ScheduledExecutorService executor;

    /**
     * 单例模式
     */
    private AsyncManager()
    {
    }

    private static class AsyncManagerHolder
    {
        private static final AsyncManager INSTANCE = new AsyncManager();
    }

    public static AsyncManager me()
    {
        return AsyncManagerHolder.INSTANCE;
    }

    /**
     * 获取执行器 (保证 SpringUtils 已就绪)
     */
    private ScheduledExecutorService getExecutor()
    {
        if (executor == null)
        {
            // 额外安全校验：确保 beanFactory 已被注入
            try {
                executor = SpringUtils.getBean("scheduledExecutorService");
            } catch (Exception e) {
                // 如果 Spring 还没准备好，暂时返回 null 或抛出更清晰的异常，而不是毒化类加载
                return null;
            }
        }
        return executor;
    }

    /**
     * 执行任务
     * 
     * @param task 任务
     */
    public void execute(TimerTask task)
    {
        ScheduledExecutorService exec = getExecutor();
        if (exec != null)
        {
            exec.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 停止任务线程池
     */
    public void shutdown()
    {
        ScheduledExecutorService exec = getExecutor();
        if (exec != null)
        {
            Threads.shutdownAndAwaitTermination(exec);
        }
    }


}
