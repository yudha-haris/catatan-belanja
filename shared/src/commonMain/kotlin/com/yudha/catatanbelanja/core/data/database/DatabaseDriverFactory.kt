package com.yudha.catatanbelanja.core.data.database

import app.cash.sqldelight.db.SqlDriver

internal const val DATABASE_NAME = "catatan_belanja.db"

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}
