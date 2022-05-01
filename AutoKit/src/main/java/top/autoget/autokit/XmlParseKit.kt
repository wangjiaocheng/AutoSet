package top.autoget.autokit

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

object XmlParseKit : LoggerKit {
    fun getXmlList(xml: String, clazz: Class<*>, startName: String): MutableList<*>? =
        Xml.newPullParser().apply { StringReader(xml).use { setInput(it) } }.let { xmlPullParser ->
            var mutableList: MutableList<Any>? = null
            try {
                var any: Any? = null
                var eventType = xmlPullParser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_DOCUMENT -> mutableList = mutableListOf()
                        XmlPullParser.START_TAG -> {
                            val name = xmlPullParser.name
                            when (startName) {
                                name -> {
                                    any = clazz.newInstance()
                                    for (i in 0 until xmlPullParser.attributeCount) {
                                        setXmlValue(
                                            any, xmlPullParser.getAttributeName(i),
                                            xmlPullParser.getAttributeValue(i)
                                        )
                                    }
                                }
                                else -> any?.let { setXmlValue(it, name, xmlPullParser.nextText()) }
                            }
                        }
                        XmlPullParser.END_TAG -> if (xmlPullParser.name == startName) any?.let {
                            mutableList?.add(it)
                            any = null
                        }
                    }
                    eventType = xmlPullParser.next()
                }
            } catch (e: Exception) {
                error("xml pull error")
            }
            mutableList
        }

    fun getXmlObject(xml: String, clazz: Class<*>): Any? = Xml.newPullParser()
        .apply { StringReader(xml).use { setInput(it) } }.let { xmlPullParser ->
            var any: Any? = null
            try {
                var mutableList: MutableList<Any>? = null
                var subObject: Any? = null
                var subName: String? = null
                var eventType = xmlPullParser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_DOCUMENT -> any = clazz.newInstance()
                        XmlPullParser.START_TAG -> {
                            val fields: Array<Field> = subObject?.javaClass?.declaredFields
                                ?: any?.let {
                                    for (i in 0 until xmlPullParser.attributeCount) {
                                        setXmlValue(
                                            any, xmlPullParser.getAttributeName(i),
                                            xmlPullParser.getAttributeValue(i)
                                        )
                                    }
                                    any.javaClass.declaredFields
                                } ?: arrayOf()
                            val name = xmlPullParser.name
                            for (field in fields) {
                                if (field.name.equals(name, true)) {
                                    when (field.type.name) {
                                        "java.util.List" -> {
                                            val type = field.genericType
                                            if (type is ParameterizedType) {
                                                subObject =
                                                    (type.actualTypeArguments[0] as Class<*>).newInstance()
                                                subName = field.name
                                                for (i in 0 until xmlPullParser.attributeCount) {
                                                    setXmlValue(
                                                        subObject,
                                                        xmlPullParser.getAttributeName(i),
                                                        xmlPullParser.getAttributeValue(i)
                                                    )
                                                }
                                                mutableList?.let {
                                                    mutableList = mutableListOf()
                                                    field.apply { isAccessible = true }
                                                        .set(any, mutableList)
                                                }
                                            }
                                        }
                                        else -> subObject?.let {
                                            setXmlValue(it, name, xmlPullParser.nextText())
                                        } ?: any?.let {
                                            setXmlValue(it, name, xmlPullParser.nextText())
                                        }
                                    }
                                    break
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> subObject?.let {
                            if (xmlPullParser.name.equals(subName, true)) {
                                mutableList?.add(it)
                                subObject = null
                                subName = null
                            }
                        }
                    }
                    eventType = xmlPullParser.next()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error("xml pull error exception")
            }
            any
        }

    private fun setXmlValue(any: Any, name: String, value: String) = try {
        for (field in any.javaClass.declaredFields) {
            if (field.name.equals(name, true))
                when (field.apply { isAccessible = true }.type) {
                    Byte::class.java -> field.set(any, value.toByte())
                    Short::class.java -> field.set(any, value.toShort())
                    Int::class.java -> field.set(any, value.toInt())
                    Long::class.java -> field.set(any, value.toLong())
                    Float::class.java -> field.set(any, value.toFloat())
                    Double::class.java -> field.set(any, value.toDouble())
                    Boolean::class.java -> field.set(any, value.toBoolean())
                    String::class.java -> field.set(any, value)
                    else -> field.set(any, value)
                }
        }
    } catch (e: Exception) {
        error("xml error")
    }
}