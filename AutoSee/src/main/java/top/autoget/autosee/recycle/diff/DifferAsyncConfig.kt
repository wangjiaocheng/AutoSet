package top.autoget.autosee.recycle.diff

import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.DiffUtil
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DifferAsyncConfig<T>(
    @RestrictTo(RestrictTo.Scope.LIBRARY) val mainThreadExecutor: Executor?,
    val backgroundThreadExecutor: Executor?, val diffCallback: DiffUtil.ItemCallback<T>
) {
    class Builder<T>(private val diffCallback: DiffUtil.ItemCallback<T>) {
        private var mMainThreadExecutor: Executor? = null
        fun setMainThreadExecutor(executor: Executor?): Builder<T> =
            apply { mMainThreadExecutor = executor }

        private var mBackgroundThreadExecutor: Executor? = null
        fun setBackgroundThreadExecutor(executor: Executor?): Builder<T> =
            apply { mBackgroundThreadExecutor = executor }

        companion object {
            private val executorLock = Any()
            private var diffExecutor: Executor? = null//库自有以后删除
        }

        fun build(): DifferAsyncConfig<T> {
            if (mBackgroundThreadExecutor == null) {
                synchronized(executorLock) {
                    if (diffExecutor == null) diffExecutor = Executors.newFixedThreadPool(2)
                }
                mBackgroundThreadExecutor = diffExecutor
            }
            return DifferAsyncConfig(mMainThreadExecutor, mBackgroundThreadExecutor, diffCallback)
        }
    }
}//backgroundThreadExecutor不会为空