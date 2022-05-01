package top.autoget.autokit

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import top.autoget.autokit.VersionKit.aboveJellyBean

object FragmentKit : LoggerKit {
    @JvmOverloads
    fun addFragment(
        fragmentManager: FragmentManager, add: Fragment, @IdRes containerId: Int,
        isHide: Boolean = false, isAddStack: Boolean = true
    ): Fragment? {
        putArgs(add, Args(containerId, isHide, isAddStack))
        return operateFragment(fragmentManager, TYPE_ADD_FRAGMENT, null, add)
    }

    data class SharedElement(var sharedElement: View, var name: String)

    @JvmOverloads
    fun addFragment(
        fragmentManager: FragmentManager, add: Fragment, @IdRes containerId: Int,
        isHide: Boolean = false, isAddStack: Boolean = true, vararg sharedElement: SharedElement
    ): Fragment? {
        putArgs(add, Args(containerId, isHide, isAddStack))
        return operateFragment(fragmentManager, TYPE_ADD_FRAGMENT, null, add, *sharedElement)
    }

    fun addFragments(
        fragmentManager: FragmentManager, add: MutableList<Fragment>, @IdRes containerId: Int,
        showIndex: Int
    ): Fragment? {
        for ((index, fragment) in add.withIndex()) {
            addFragment(fragmentManager, fragment, containerId, index != showIndex, true)
        }
        return add[showIndex]
    }

    fun addFragments(
        fragmentManager: FragmentManager, add: MutableList<Fragment>, @IdRes containerId: Int,
        showIndex: Int, vararg lists: MutableList<SharedElement>
    ): Fragment? {
        for ((index, fragment) in add.withIndex()) {
            putArgs(fragment, Args(containerId, index != showIndex, true))
            return operateFragment(
                fragmentManager, TYPE_ADD_FRAGMENT, null, fragment, *lists[index].toTypedArray()
            )
        }
        return add[showIndex]
    }

    @JvmOverloads
    fun hideAddFragment(
        fragmentManager: FragmentManager, hide: Fragment, add: Fragment, @IdRes containerId: Int,
        isHide: Boolean = false, isAddStack: Boolean = true
    ): Fragment? {
        putArgs(add, Args(containerId, isHide, isAddStack))
        return operateFragment(fragmentManager, TYPE_HIDE_ADD_FRAGMENT, hide, add)
    }

    @JvmOverloads
    fun hideAddFragment(
        fragmentManager: FragmentManager, hide: Fragment, add: Fragment, @IdRes containerId: Int,
        isHide: Boolean = false, isAddStack: Boolean = true, vararg sharedElement: SharedElement
    ): Fragment? {
        putArgs(add, Args(containerId, isHide, isAddStack))
        return operateFragment(fragmentManager, TYPE_HIDE_ADD_FRAGMENT, hide, add, *sharedElement)
    }

    @JvmOverloads
    fun popAddFragment(
        fragmentManager: FragmentManager, add: Fragment, @IdRes containerId: Int,
        isAddStack: Boolean = true
    ): Fragment? {
        putArgs(add, Args(containerId, false, isAddStack))
        return operateFragment(fragmentManager, TYPE_POP_ADD_FRAGMENT, null, add)
    }

    @JvmOverloads
    fun popAddFragment(
        fragmentManager: FragmentManager, add: Fragment, @IdRes containerId: Int,
        isAddStack: Boolean = true, vararg sharedElements: SharedElement
    ): Fragment? {
        putArgs(add, Args(containerId, false, isAddStack))
        return operateFragment(fragmentManager, TYPE_POP_ADD_FRAGMENT, null, add, *sharedElements)
    }

    @JvmOverloads
    fun replaceFragment(src: Fragment, dest: Fragment, isAddStack: Boolean = true): Fragment? =
        src.arguments?.let { bundle ->
            when (val containerId = bundle.getInt(ARGS_ID)) {
                0 -> null
                else -> src.fragmentManager
                    ?.let { replaceFragment(it, dest, containerId, isAddStack) }
            }
        }

    @JvmOverloads
    fun replaceFragment(
        src: Fragment, dest: Fragment,
        isAddStack: Boolean = true, vararg sharedElement: SharedElement
    ): Fragment? = src.arguments?.let { bundle ->
        when (val containerId = bundle.getInt(ARGS_ID)) {
            0 -> null
            else -> src.fragmentManager
                ?.let { replaceFragment(it, dest, containerId, isAddStack, *sharedElement) }
        }
    }

    @JvmOverloads
    fun replaceFragment(
        fragmentManager: FragmentManager, dest: Fragment, @IdRes containerId: Int,
        isAddStack: Boolean = true
    ): Fragment? {
        putArgs(dest, Args(containerId, false, isAddStack))
        return operateFragment(fragmentManager, TYPE_REPLACE_FRAGMENT, null, dest)
    }

    @JvmOverloads
    fun replaceFragment(
        fragmentManager: FragmentManager, dest: Fragment, @IdRes containerId: Int,
        isAddStack: Boolean = true, vararg sharedElement: SharedElement
    ): Fragment? {
        putArgs(dest, Args(containerId, false, isAddStack))
        return operateFragment(fragmentManager, TYPE_REPLACE_FRAGMENT, null, dest, *sharedElement)
    }

    fun showFragment(show: Fragment): Fragment? {
        getArgs(show)?.let { putArgs(show, Args(it.id, false, it.isAddStack)) }
        return show.fragmentManager?.let { operateFragment(it, TYPE_SHOW_FRAGMENT, null, show) }
    }

    fun showFragments(fragmentManager: FragmentManager) = getFragments(fragmentManager).let {
        if (it.isNotEmpty()) for (fragment in it.reversed()) {
            showFragment(fragment)
        }
    }

    fun hideFragment(hide: Fragment): Fragment? {
        getArgs(hide)?.let { putArgs(hide, Args(it.id, true, it.isAddStack)) }
        return hide.fragmentManager?.let { operateFragment(it, TYPE_HIDE_FRAGMENT, null, hide) }
    }

    fun hideFragments(fragmentManager: FragmentManager) = getFragments(fragmentManager).let {
        if (it.isNotEmpty()) for (fragment in it.reversed()) {
            hideFragment(fragment)
        }
    }

    fun hideAllShowFragment(show: Fragment): Fragment? = show.fragmentManager?.let {
        hideFragments(it)
        return operateFragment(it, TYPE_SHOW_FRAGMENT, null, show)
    }

    fun hideOthersShowFragment(hide: MutableList<Fragment>, show: Fragment): Fragment? {
        if (hide.isNotEmpty()) for (fragment in hide.reversed()) {
            hideFragment(fragment)
        }
        return show.fragmentManager?.let { operateFragment(it, TYPE_SHOW_FRAGMENT, null, show) }
    }

    fun hideShowFragment(hide: Fragment, show: Fragment): Fragment? {
        getArgs(hide)?.let { putArgs(hide, Args(it.id, true, it.isAddStack)) }
        getArgs(show)?.let { putArgs(show, Args(it.id, false, it.isAddStack)) }
        return show.fragmentManager?.let {
            operateFragment(it, TYPE_HIDE_SHOW_FRAGMENT, hide, show)
        }
    }

    fun removeFragment(remove: Fragment): Fragment? =
        remove.fragmentManager?.let { operateFragment(it, TYPE_REMOVE_FRAGMENT, null, remove) }

    @JvmOverloads
    fun removeToFragment(remove: Fragment, isIncludeSelf: Boolean = true): Fragment? =
        remove.fragmentManager?.let {
            operateFragment(
                it, TYPE_REMOVE_TO_FRAGMENT, (if (isIncludeSelf) remove else null), remove
            )
        }

    @JvmOverloads
    fun removeFragments(fragmentManager: FragmentManager, isAll: Boolean = true) {
        getFragments(fragmentManager).let { list ->
            if (list.isNotEmpty()) for (fragment in list.reversed()) {
                if (isAll) removeFragments(fragment.childFragmentManager)
                removeFragment(fragment)
            }
        }
    }

    @JvmOverloads
    fun popFragment(fragmentManager: FragmentManager, isImmediate: Boolean = true) {
        when {
            isImmediate -> fragmentManager.popBackStackImmediate()
            else -> fragmentManager.popBackStack()
        }
    }

    @JvmOverloads
    fun popToFragment(
        fragmentManager: FragmentManager, clz: Class<out Fragment>,
        isIncludeSelf: Boolean = true, isImmediate: Boolean = true
    ) = (if (isIncludeSelf) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0).let {
        when {
            isImmediate -> fragmentManager.popBackStackImmediate(clz.name, it)
            else -> fragmentManager.popBackStack(clz.name, it)
        }
    }

    @JvmOverloads
    fun popFragments(
        fragmentManager: FragmentManager, isAll: Boolean = true, isImmediate: Boolean = true
    ) {
        fragmentManager.run {
            if (isAll) getFragments(this).let { list ->
                if (list.isNotEmpty()) for (fragment in list.reversed()) {
                    popFragments(fragment.childFragmentManager)
                }
            }
            if (backStackEntryCount > 0) getBackStackEntryAt(0).let {
                when {
                    isImmediate ->
                        popBackStackImmediate(it.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    else -> popBackStack(it.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
            }
        }
    }

    private const val TYPE_ADD_FRAGMENT = 0x01
    private const val TYPE_HIDE_ADD_FRAGMENT = 0x01 shl 1
    private const val TYPE_POP_ADD_FRAGMENT = 0x01 shl 2
    private const val TYPE_REPLACE_FRAGMENT = 0x01 shl 3
    private const val TYPE_SHOW_FRAGMENT = 0x01 shl 4
    private const val TYPE_HIDE_FRAGMENT = 0x01 shl 5
    private const val TYPE_HIDE_SHOW_FRAGMENT = 0x01 shl 6
    private const val TYPE_REMOVE_FRAGMENT = 0x01 shl 7
    private const val TYPE_REMOVE_TO_FRAGMENT = 0x01 shl 8
    private fun operateFragment(
        fragmentManager: FragmentManager, type: Int, src: Fragment?, dest: Fragment,
        vararg sharedElements: SharedElement
    ): Fragment? = when {
        src === dest -> null
        src != null && src.isRemoving -> {
            error("$loggerTag->${src.javaClass.name} is isRemoving")
            null
        }
        else -> fragmentManager.beginTransaction().apply {
            when {
                sharedElements.isEmpty() -> setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                else -> for (sharedElement in sharedElements) {
                    addSharedElement(sharedElement.sharedElement, sharedElement.name)
                }
            }
        }.run {
            dest.javaClass.name.let { name ->
                dest.arguments?.let { args ->
                    var destFragment = dest
                    when (type) {
                        TYPE_ADD_FRAGMENT -> {
                            destFragment = fragmentManager.findFragmentByTag(name) ?: dest
                            if (destFragment.isAdded) remove(destFragment)
                            add(args.getInt(ARGS_ID), destFragment, name)
                            if (args.getBoolean(ARGS_IS_HIDE)) hide(destFragment)
                            if (args.getBoolean(ARGS_IS_ADD_STACK)) addToBackStack(name)
                        }
                        TYPE_HIDE_ADD_FRAGMENT -> {
                            src?.let { hide(it) }
                            destFragment = fragmentManager.findFragmentByTag(name) ?: dest
                            if (destFragment.isAdded) remove(destFragment)
                            add(args.getInt(ARGS_ID), destFragment, name)
                            if (args.getBoolean(ARGS_IS_HIDE)) hide(destFragment)
                            if (args.getBoolean(ARGS_IS_ADD_STACK)) addToBackStack(name)
                        }
                        TYPE_POP_ADD_FRAGMENT -> {
                            popFragment(fragmentManager)
                            destFragment = fragmentManager.findFragmentByTag(name) ?: dest
                            if (destFragment.isAdded) remove(destFragment)
                            add(args.getInt(ARGS_ID), destFragment, name)
                            if (args.getBoolean(ARGS_IS_ADD_STACK)) addToBackStack(name)
                        }
                        TYPE_REPLACE_FRAGMENT -> {
                            destFragment = fragmentManager.findFragmentByTag(name) ?: dest
                            replace(args.getInt(ARGS_ID), destFragment, name)
                            if (args.getBoolean(ARGS_IS_ADD_STACK)) addToBackStack(name)
                        }
                        TYPE_SHOW_FRAGMENT -> show(destFragment)
                        TYPE_HIDE_FRAGMENT -> hide(destFragment)
                        TYPE_HIDE_SHOW_FRAGMENT -> src?.let { hide(it).show(destFragment) }
                        TYPE_REMOVE_FRAGMENT -> remove(destFragment)
                        TYPE_REMOVE_TO_FRAGMENT -> for (fragment in getFragments(fragmentManager).reversed()) {
                            if (fragment === destFragment) {
                                src?.let { remove(fragment) }
                                break
                            }
                            remove(fragment)
                        }
                    }
                    commitAllowingStateLoss()
                    destFragment
                }
            }
        }
    }

    data class Args(
        var id: Int, var isHide: Boolean, var isAddStack: Boolean, var tag: String? = null
    )

    private const val ARGS_ID = "args_id"
    private const val ARGS_IS_HIDE = "args_is_hide"
    private const val ARGS_IS_ADD_STACK = "args_is_add_stack"
    private const val ARGS_TAG = "args_tag"
    private fun putArgs(fragment: Fragment, args: Args): Bundle? = fragment.apply {
        if (arguments == null) arguments = Bundle()
    }.arguments?.apply {
        putInt(ARGS_ID, args.id)
        putBoolean(ARGS_IS_HIDE, args.isHide)
        putBoolean(ARGS_IS_ADD_STACK, args.isAddStack)
        putString(ARGS_TAG, args.tag)
    }

    private fun getArgs(fragment: Fragment): Args? = fragment.apply {
        if (arguments == null) arguments = Bundle.EMPTY
    }.arguments?.run {
        Args(getInt(ARGS_ID, fragment.id), getBoolean(ARGS_IS_HIDE), getBoolean(ARGS_IS_ADD_STACK))
    }

    @JvmOverloads
    fun getLastAddFragment(fragmentManager: FragmentManager, isInStack: Boolean = true): Fragment? =
        fragmentManager.fragments.let { list ->
            when {
                list.isEmpty() -> null
                else -> {
                    for (fragment in list.reversed()) {
                        fragment?.let {
                            return when {
                                isInStack -> fragment.arguments?.let { args ->
                                    when {
                                        args.getBoolean(ARGS_IS_ADD_STACK) -> fragment
                                        else -> null
                                    }
                                }
                                else -> fragment
                            }
                        }
                    }
                    null
                }
            }
        }

    @JvmOverloads
    fun getTopShowFragment(
        fragmentManager: FragmentManager, isInStack: Boolean = true,
        parentFragment: Fragment? = null
    ): Fragment? = fragmentManager.fragments.let { list ->
        when {
            list.isEmpty() -> parentFragment
            else -> {
                for (fragment in list.reversed()) {
                    fragment?.run {
                        if (isResumed && isVisible && userVisibleHint) return when {
                            isInStack -> arguments?.let { args ->
                                when {
                                    args.getBoolean(ARGS_IS_ADD_STACK) ->
                                        getTopShowFragment(childFragmentManager, true, this)
                                    else -> null
                                }
                            }
                            else -> getTopShowFragment(childFragmentManager, false, this)
                        }
                    }
                }
                parentFragment
            }
        }
    }

    @JvmOverloads
    fun getFragments(
        fragmentManager: FragmentManager, isInStack: Boolean = true
    ): MutableList<Fragment> = fragmentManager.fragments.let { list ->
        when {
            list.isEmpty() -> mutableListOf()
            else -> mutableListOf<Fragment>().apply {
                for (fragment in list.reversed()) {
                    fragment?.let {
                        when {
                            isInStack -> it.arguments
                                ?.let { args -> if (args.getBoolean(ARGS_IS_ADD_STACK)) add(it) }
                            else -> add(it)
                        }
                    }
                }
            }
        }
    }

    data class FragmentNode(var fragment: Fragment, var next: MutableList<FragmentNode>?) {
        override fun toString(): String =
            "${fragment.javaClass.simpleName}->${if (next?.isEmpty() != false) "no child" else next.toString()}"
    }

    @JvmOverloads
    fun getNodes(
        fragmentManager: FragmentManager, isInStack: Boolean = true
    ): MutableList<FragmentNode> = fragmentManager.fragments.let { list ->
        when {
            list.isEmpty() -> mutableListOf()
            else -> mutableListOf<FragmentNode>().apply {
                for (fragment in list.reversed()) {
                    fragment?.let {
                        when {
                            isInStack -> it.arguments?.let { args ->
                                if (args.getBoolean(ARGS_IS_ADD_STACK))
                                    add(FragmentNode(it, getNodes(it.childFragmentManager, true)))
                            }
                            else -> add(FragmentNode(it, getNodes(it.childFragmentManager, false)))
                        }
                    }
                }
            }
        }
    }

    fun getPreFragment(destFragment: Fragment): Fragment? = destFragment.fragmentManager?.let {
        var flag = false
        for (fragment in it.fragments.reversed()) {
            when {
                flag -> return fragment
                fragment === destFragment -> flag = true
            }
        }
        null
    }

    fun getFragment(fragmentManager: FragmentManager, findClazz: Class<out Fragment>): Fragment? =
        getFragment(fragmentManager, findClazz.name)

    fun getFragment(fragmentManager: FragmentManager, tag: String): Fragment? =
        if (fragmentManager.fragments.isEmpty()) null else fragmentManager.findFragmentByTag(tag)

    fun dispatchBackPress(fragment: Fragment): Boolean = dispatchBackPress(fragment.fragmentManager)
    interface OnBackClickListener {
        fun onBackClick(): Boolean
    }

    fun dispatchBackPress(fragmentManager: FragmentManager?): Boolean =
        fragmentManager?.fragments?.let { list ->
            when {
                list.isEmpty() -> return false
                else -> {
                    for (fragment in list.reversed()) {
                        fragment?.run {
                            if (isResumed && isVisible && userVisibleHint &&
                                this is OnBackClickListener && (this as OnBackClickListener).onBackClick()
                            ) return true
                        }
                    }
                    return false
                }
            }
        } ?: false

    fun setBackgroundColor(fragment: Fragment, @ColorInt color: Int): View? =
        fragment.view?.apply { setBackgroundColor(color) }

    fun setBackgroundResource(fragment: Fragment, @DrawableRes resId: Int): View? =
        fragment.view?.apply { setBackgroundResource(resId) }

    fun setBackgroundDrawable(fragment: Fragment, drawable: Drawable): View? =
        fragment.view?.apply {
            if (aboveJellyBean) background = drawable else ViewCompat.setBackground(this, drawable)
        }

    fun getSimpleName(fragment: Fragment?): String = fragment?.javaClass?.simpleName ?: "null"
}