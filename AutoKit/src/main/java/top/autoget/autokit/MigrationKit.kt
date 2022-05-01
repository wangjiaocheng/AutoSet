package top.autoget.autokit

import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import org.greenrobot.greendao.AbstractDao
import org.greenrobot.greendao.database.Database
import org.greenrobot.greendao.database.StandardDatabase
import org.greenrobot.greendao.internal.DaoConfig
import java.lang.reflect.InvocationTargetException

object MigrationKit : LoggerKit {
    fun migrate(db: SQLiteDatabase, vararg daoClasses: Class<out AbstractDao<*, *>>) =
        migrate(StandardDatabase(db), *daoClasses)
            .apply { printLog("【The Old Database Version】${db.version}") }

    var isDebug: Boolean = false
    private fun printLog(info: String) = if (isDebug) debug("$loggerTag->$info") else Unit
    fun migrate(db: StandardDatabase, vararg daoClasses: Class<out AbstractDao<*, *>>) {
        generateNewTablesIfNotExists(db, *daoClasses)
        generateTempTables(db, *daoClasses).apply { printLog("【Generate temp table】start") }
        printLog("【Generate temp table】complete")
        dropAllTables(db, true, *daoClasses)
        createAllTables(db, true, *daoClasses)
        restoreData(db, *daoClasses).apply { printLog("【Restore data】start") }
        printLog("【Restore data】complete")
    }

    private fun generateNewTablesIfNotExists(
        db: StandardDatabase, vararg daoClasses: Class<out AbstractDao<*, *>>
    ) = reflectMethod(db, "createTable", true, *daoClasses)

    private fun generateTempTables(
        db: StandardDatabase, vararg daoClasses: Class<out AbstractDao<*, *>>
    ) {
        if (daoClasses.isNotEmpty()) for (daoClass in daoClasses) {
            DaoConfig(db, daoClass).let { daoConfig ->
                db.execSQL("CREATE TEMP TABLE ${daoConfig.tablename}_TEMP AS SELECT * FROM ${daoConfig.tablename};")
            }
        }
    }

    private fun dropAllTables(
        db: StandardDatabase, ifExists: Boolean, vararg daoClasses: Class<out AbstractDao<*, *>>
    ) = reflectMethod(db, "dropTable", ifExists, *daoClasses)

    private fun createAllTables(
        db: StandardDatabase, ifNotExists: Boolean, vararg daoClasses: Class<out AbstractDao<*, *>>
    ) = reflectMethod(db, "createTable", ifNotExists, *daoClasses)

    private fun reflectMethod(
        db: StandardDatabase, methodName: String, isExists: Boolean,
        vararg daoClasses: Class<out AbstractDao<*, *>>
    ) {
        if (daoClasses.isNotEmpty()) try {
            for (daoClass in daoClasses) {
                daoClass.getDeclaredMethod(
                    methodName, Database::class.java, Boolean::class.javaPrimitiveType
                ).invoke(null, db, isExists)
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    private fun restoreData(db: StandardDatabase, vararg daoClasses: Class<out AbstractDao<*, *>>) {
        if (daoClasses.isNotEmpty()) for (daoClass in daoClasses) {
            DaoConfig(db, daoClass).let { daoConfig ->
                getColumns(db, "${daoConfig.tablename}_TEMP").let { columns ->
                    ArrayList<String>(columns.size).let { properties ->
                        for (property in daoConfig.properties) {
                            if (columns.contains(property.columnName)) properties.add(property.columnName)
                        }
                        if (properties.size > 0)
                            TextUtils.join(",", properties).let { columnSQL ->
                                db.execSQL("INSERT INTO ${daoConfig.tablename} ($columnSQL) SELECT $columnSQL FROM ${daoConfig.tablename}_TEMP;")
                            }
                    }
                }
                db.execSQL("DROP TABLE ${daoConfig.tablename}_TEMP")
            }
        }
    }

    private fun getColumns(db: StandardDatabase, tableName: String): MutableList<String> = try {
        db.rawQuery("SELECT * FROM $tableName limit 0", null).use { cursor ->
            cursor.run { if (columnCount > 0) columnNames.toMutableList() else mutableListOf() }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        mutableListOf()
    }
}