package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase

class DatabaseProvider(private val driverFactory: DatabaseDriverFactory) {

    fun create(): CatatanBelanjaDatabase = CatatanBelanjaDatabase(driverFactory.create())
}
