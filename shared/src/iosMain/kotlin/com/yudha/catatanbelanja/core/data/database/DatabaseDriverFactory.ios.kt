package com.yudha.catatanbelanja.core.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase

actual class DatabaseDriverFactory {

    actual fun create(): SqlDriver = NativeSqliteDriver(
        schema = CatatanBelanjaDatabase.Schema,
        name = DATABASE_NAME,
        onConfiguration = { config ->
            config.copy(extendedConfig = config.extendedConfig.copy(foreignKeyConstraints = true))
        },
    )
}
