package top.autoget.autokit

import java.lang.reflect.*

object ReflectionKit {
    fun getClassListByPackage(packageName: String): Class<*> =
        Package.getPackage(packageName).javaClass

    fun isInstance(clazz: Class<*>, any: Any): Boolean = clazz.isInstance(any)

    @Throws(
        NoSuchMethodException::class, SecurityException::class,
        ClassNotFoundException::class, InstantiationException::class,
        IllegalAccessException::class, IllegalArgumentException::class,
        InvocationTargetException::class
    )
    fun newInstance(className: String, vararg args: Any): Any =
        Class.forName(className).getDeclaredConstructor(*getArgsType(args))
            .apply { accessible(this) }.newInstance(*args)

    private fun getArgsType(vararg args: Any): Array<Class<*>?> =
        arrayOfNulls<Class<*>>(args.size).apply {
            for ((index, arg) in args.withIndex()) {
                this[index] = arg.javaClass
            }
        }

    private fun <T : AccessibleObject> accessible(accessible: T): T = accessible.apply {
        if (this !is Member || !Modifier.isPublic(modifiers) ||
            !Modifier.isPublic(declaringClass.modifiers) || !isAccessible
        ) isAccessible = true
        if (this is Field && modifiers and Modifier.FINAL == Modifier.FINAL)
            Field::class.java.getDeclaredField("modifiers")
                .setInt(this, modifiers and Modifier.FINAL.inv())
    }

    @Throws(Exception::class)
    fun invokeMethod(any: Any, methodName: String, vararg args: Any): Any? =
        any.javaClass.getDeclaredMethod(methodName, *getArgsType(args))
            .apply { accessible(this) }.invoke(any, *args)

    @Throws(Exception::class)
    fun invokeStaticMethod(className: String, methodName: String, vararg args: Any): Any? =
        Class.forName(className).getDeclaredMethod(methodName, *getArgsType(args))
            .apply { accessible(this) }.invoke(null, *args)

    @Throws(Exception::class)
    fun getProperty(any: Any, fieldName: String): Any? = any.javaClass.getDeclaredField(fieldName)
        .apply { accessible(this) }.get(any)

    @Throws(Exception::class)
    fun getStaticProperty(className: String, fieldName: String): Any? = Class.forName(className)
        .let { it.getDeclaredField(fieldName).apply { accessible(this) }.get(it) }

    @Throws(Exception::class)
    fun setProperty(any: Any, fieldName: String, value: Any?): Any =
        any.javaClass.getDeclaredField(fieldName).apply { accessible(this) }.set(any, value)

    @Throws(Exception::class)
    fun setStaticProperty(className: String, fieldName: String, value: Any?): Any =
        Class.forName(className).let {
            it.getDeclaredField(fieldName).apply { accessible(this) }.set(it, value)
        }
}