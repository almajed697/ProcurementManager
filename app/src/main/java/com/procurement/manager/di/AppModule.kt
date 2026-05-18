package com.procurement.manager.di

import android.content.Context
import androidx.room.Room
import com.procurement.manager.data.local.dao.BudgetDao
import com.procurement.manager.data.local.dao.ReceiptDao
import com.procurement.manager.data.local.database.ProcurementDatabase
import com.procurement.manager.data.repository.BudgetRepository
import com.procurement.manager.data.repository.BudgetRepositoryImpl
import com.procurement.manager.data.repository.ReceiptRepository
import com.procurement.manager.data.repository.ReceiptRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ProcurementDatabase {
        return Room.databaseBuilder(
            context,
            ProcurementDatabase::class.java,
            ProcurementDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBudgetDao(database: ProcurementDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideReceiptDao(database: ProcurementDatabase): ReceiptDao = database.receiptDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindReceiptRepository(impl: ReceiptRepositoryImpl): ReceiptRepository
}
