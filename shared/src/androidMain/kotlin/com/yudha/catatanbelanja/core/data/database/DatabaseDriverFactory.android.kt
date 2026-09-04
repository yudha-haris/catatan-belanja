package com.yudha.catatanbelanja.core.data.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase

actual class DatabaseDriverFactory(private val context: Context) {

    actual fun create(): SqlDriver = AndroidSqliteDriver(
        schema = CatatanBelanjaDatabase.Schema,
        context = context,
        name = DATABASE_NAME,
        callback = object : AndroidSqliteDriver.Callback(CatatanBelanjaDatabase.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.setForeignKeyConstraintsEnabled(true)
            }
        },
    )
}
