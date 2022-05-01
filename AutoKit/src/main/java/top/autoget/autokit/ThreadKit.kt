package top.autoget.autokit

import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import androidx.annotation.IntRange
import java.io.Serializable
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

object ThreadKit : LoggerKit {
    val defaultThreadPoolSize: Int
        get() = (2 * Runtime.getRuntime().availableProcessors() + 1).let { if (it > 8) 8 else it }
    val isMainThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    abstract class SimpleTask<T> : Task<T>() {
        override fun onCancel() {
            error("$loggerTag->onCancel: ${Thread.currentThread()}")
        }

        override fun onFail(t: Throwable) {
            error("$loggerTag->onFail: $t")
        }
    }

    abstract class Task<T> : Runnable {
        companion object {
            private const val NEW = 0
            private const val COMPLETING = 1
            private const val CANCELLED = 2
            private const val EXCEPTIONAL = 3
        }

        @Volatile
        private var state = NEW
        val isCanceled: Boolean
            get() = state == CANCELLED
        var isSchedule: Boolean = false

        @Throws(Throwable::class)
        abstract fun doInBackground(): T?

        abstract fun onSuccess(result: T?)
        abstract fun onCancel()
        abstract fun onFail(t: Throwable)
        override fun run() {
            try {
                doInBackground().let { result ->
                    if (state == NEW) when {
                        isSchedule -> Deliver.post { onSuccess(result) }
                        else -> {
                            state = COMPLETING
                            Deliver.post {
                                onSuccess(result)
                                removeScheduleByTask(this@Task)
                            }
                        }
                    }
                }
            } catch (throwable: Throwable) {
                if (state == NEW) {
                    state = EXCEPTIONAL
                    Deliver.post {
                        onFail(throwable)
                        removeScheduleByTask(this@Task)
                    }
                }
            }
        }

        fun cancel() {
            if (state == NEW) {
                state = CANCELLED
                Deliver.post {
                    onCancel()
                    removeScheduleByTask(this@Task)
                }
            }
        }
    }

    private object Deliver {
        private val mainHandler: Handler? = try {
            Looper.getMainLooper()
        } catch (e: Exception) {
            null
        }?.let { Handler(it) }

        internal fun post(runnable: Runnable): Any = mainHandler?.post(runnable) ?: runnable.run()
    }

    @Synchronized
    private fun removeScheduleByTask(task: Task<*>): MutableList<Runnable>? =
        taskScheduled[task]?.shutdownNow().apply { taskScheduled.remove(task) }

    fun cancel(task: Task<*>) = task.cancel()

    @JvmOverloads
    fun <T> executeBySingleWithDelay(
        task: Task<T>, delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): Any = executeWithDelay(getPool(TYPE_SINGLE, priority), task, delay, unit)

    @JvmOverloads
    fun <T> executeByCachedWithDelay(
        task: Task<T>, delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): Any = executeWithDelay(getPool(TYPE_CACHED, priority), task, delay, unit)

    @JvmOverloads
    fun <T> executeByIOWithDelay(
        task: Task<T>, delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): Any = executeWithDelay(getPool(TYPE_IO, priority), task, delay, unit)

    @JvmOverloads
    fun <T> executeByCPUWithDelay(
        task: Task<T>, delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): Any = executeWithDelay(getPool(TYPE_CPU, priority), task, delay, unit)

    @JvmOverloads
    fun <T> executeByFixedWithDelay(
        @IntRange(from = 1) size: Int, task: Task<T>,
        delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): Any = executeWithDelay(getPool(size, priority), task, delay, unit)

    @JvmOverloads
    fun <T> executeWithDelay(
        pool: ExecutorService?, task: Task<T>,
        delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS
    ): Any = getScheduledByTask(task).run {
        when {
            delay > 0 -> schedule({ pool?.execute(task) }, delay, unit)
            else -> execute { pool?.execute(task) }
        }
    }//设置延迟时间，或者立即执行

    private val taskScheduled: MutableMap<Task<*>, ScheduledExecutorService> = mutableMapOf()

    @Synchronized
    private fun getScheduledByTask(task: Task<*>): ScheduledExecutorService =
        taskScheduled[task] ?: Executors.newScheduledThreadPool(
            1, ThreadFactoryWithAtomic("scheduled", Thread.MAX_PRIORITY)
        ).apply { taskScheduled[task] = this }//各线程池转计划线程池

    @JvmOverloads
    fun <T> executeBySingleAtFixRate(
        task: Task<T>, period: Long,
        initialDelay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): ScheduledFuture<*> =
        executeAtFixedRate(getPool(TYPE_SINGLE, priority), task, period, initialDelay, unit)

    @JvmOverloads
    fun <T> executeByCachedAtFixRate(
        task: Task<T>, period: Long,
        initialDelay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): ScheduledFuture<*> =
        executeAtFixedRate(getPool(TYPE_CACHED, priority), task, period, initialDelay, unit)

    @JvmOverloads
    fun <T> executeByIOAtFixRate(
        task: Task<T>, period: Long,
        initialDelay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): ScheduledFuture<*> =
        executeAtFixedRate(getPool(TYPE_IO, priority), task, period, initialDelay, unit)

    @JvmOverloads
    fun <T> executeByCPUAtFixRate(
        task: Task<T>, period: Long,
        initialDelay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): ScheduledFuture<*> =
        executeAtFixedRate(getPool(TYPE_CPU, priority), task, period, initialDelay, unit)

    @JvmOverloads
    fun <T> executeByFixedAtFixRate(
        @IntRange(from = 1) size: Int, task: Task<T>, period: Long,
        initialDelay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): ScheduledFuture<*> =
        executeAtFixedRate(getPool(size, priority), task, period, initialDelay, unit)

    @JvmOverloads
    fun <T> executeAtFixedRate(
        pool: ExecutorService?, task: Task<T>, period: Long,
        initialDelay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS
    ): ScheduledFuture<*> = task.apply { isSchedule = true }.let {
        getScheduledByTask(it)
            .scheduleAtFixedRate({ pool?.execute(it) }, initialDelay, period, unit)
    }//固定执行间隔，设置首次延迟

    val poolSingle: ExecutorService?
        get() = getPool(TYPE_SINGLE)

    fun getPoolSingle(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? =
        getPool(TYPE_SINGLE, priority)

    val poolCached: ExecutorService?
        get() = getPool(TYPE_CACHED)

    fun getPoolCached(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? =
        getPool(TYPE_CACHED, priority)

    val poolIo: ExecutorService?
        get() = getPool(TYPE_IO)

    fun getPoolIo(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? =
        getPool(TYPE_IO, priority)

    val poolCpu: ExecutorService?
        get() = getPool(TYPE_CPU)

    fun getPoolCpu(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? =
        getPool(TYPE_CPU, priority)

    @JvmOverloads
    fun getPoolFixed(
        @IntRange(from = 1) size: Int,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY
    ): ExecutorService? = getPool(size, priority)

    private val typePriorityPools = SparseArray<SparseArray<ExecutorService>>()

    @JvmOverloads
    @Synchronized
    fun getPool(type: Int, priority: Int = Thread.NORM_PRIORITY): ExecutorService? =
        typePriorityPools.get(type)?.let {
            it.get(priority) ?: createPool(type, priority).apply { it.put(priority, this) }
        } ?: createPool(type, priority).apply {
            SparseArray<ExecutorService>().let {
                it.put(priority, this)
                typePriorityPools.put(type, it)
            }
        }

    private const val TYPE_SINGLE: Int = -1
    private const val TYPE_CACHED: Int = -2
    private const val TYPE_IO: Int = -4
    private const val TYPE_CPU: Int = -8
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private fun createPool(typeOrCount: Int, priority: Int): ExecutorService = when (typeOrCount) {
        TYPE_SINGLE -> Executors.newSingleThreadExecutor(
            ThreadFactoryWithAtomic("single", priority)
        ).apply { println("单身狗线程") }//单线程
        TYPE_CACHED -> Executors.newCachedThreadPool(
            ThreadFactoryWithAtomic("cached", priority)
        )//带缓冲
        TYPE_IO -> ThreadPoolExecutor(
            2 * CPU_COUNT + 1, 2 * CPU_COUNT + 1, 30,
            TimeUnit.SECONDS, LinkedBlockingQueue(128), ThreadFactoryWithAtomic("io", priority)
        )
        TYPE_CPU -> ThreadPoolExecutor(
            CPU_COUNT + 1, 2 * CPU_COUNT + 1, 30,
            TimeUnit.SECONDS, LinkedBlockingQueue(128), ThreadFactoryWithAtomic("cpu", priority)
        )
        else -> Executors.newFixedThreadPool(
            typeOrCount, ThreadFactoryWithAtomic("fixed($typeOrCount)", priority)
        )//定数目
    }

    private class ThreadFactoryWithAtomic(prefix: String, private val priorityLevel: Int) :
        ThreadFactory, AtomicLong(), Serializable {
        companion object {
            private val poolNumber = AtomicInteger(1)
            private const val serialVersionUID = 1L
        }

        private val threadGroup: ThreadGroup? =
            System.getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup
        private val namePrefix: String = "$prefix-pool-${poolNumber.andIncrement}-thread-"
        override fun newThread(runnable: Runnable): Thread =
            object : Thread(threadGroup, runnable, "$namePrefix$andIncrement", 0) {
                override fun run() {
                    try {
                        super.run()
                    } catch (t: Throwable) {
                        error("$loggerTag->Request threw uncaught throwable$t")
                    }
                }
            }.apply {
                if (isDaemon) isDaemon = false
                priority = priorityLevel
            }

        override fun toByte(): Byte = poolNumber.toByte()
        override fun toChar(): Char = poolNumber.toChar()
        override fun toShort(): Short = poolNumber.toShort()
    }

    var executorService: ExecutorService? = null
    fun shutDown() = executorService?.shutdown()//已提交执行完，不接受新提交
    fun shutDownNow(): MutableList<Runnable>? = executorService?.shutdownNow()//已提交也停止，返回等待列表
    val isShutDown: Boolean?
        get() = executorService?.isShutdown
    val isTerminated: Boolean?
        get() = executorService?.isTerminated//关闭后判断是否全部完成

    @Throws(InterruptedException::class)//请求关闭、超时、线程中断，阻塞到所有任务执行完成
    fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean? =
        executorService?.awaitTermination(timeout, unit)

    fun submit(task: Runnable): Future<*>? = executorService?.submit(task)
    fun <T> submit(task: Runnable, result: T): Future<T>? = executorService?.submit(task, result)
    fun <T> submit(task: Callable<T>): Future<T>? = executorService?.submit(task)//再get()获取result

    @Throws(InterruptedException::class, ExecutionException::class)//正常异常返回取消未完成任务
    fun <T> invokeAny(tasks: Collection<Callable<T>>): T? = executorService?.invokeAny(tasks)

    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T? =
        executorService?.invokeAny(tasks, timeout, unit)//正常异常返回取消未完成任务

    @Throws(InterruptedException::class)//返回列表顺序与给定列表顺序同
    fun <T> invokeAll(tasks: Collection<Callable<T>>): MutableList<Future<T>>? =
        executorService?.invokeAll(tasks)

    @Throws(InterruptedException::class)//未超时已完成所有任务，已超时未完成所有任务
    fun <T> invokeAll(
        tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit
    ): MutableList<Future<T>>? = executorService?.invokeAll(tasks, timeout, unit)
}