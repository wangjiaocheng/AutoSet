package top.autoget.autokit

object SingletonKit {
    abstract class Singleton<T> {
        private var instance: T? = null
        protected abstract fun newInstance(): T
        fun getInstance(): T? {
            if (instance == null) {
                synchronized(Singleton<*>::javaClass) {
                    if (instance == null) instance = newInstance()
                }
            }
            return instance
        }
    }
}