package com.biz2.nowfloats.boost.updates.persistance.local


import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.biz2.nowfloats.boost.updates.persistance.dao.CartDao
import com.biz2.nowfloats.boost.updates.persistance.dao.FeaturesDao

import com.biz2.nowfloats.boost.updates.persistance.dao.WidgetDao
import com.boost.upgrades.data.model.CartModel
import com.boost.upgrades.data.model.FeaturesModel
import com.boost.upgrades.data.model.WidgetModel
import com.boost.upgrades.utils.Constants

@Database(entities = [FeaturesModel::class, WidgetModel::class, CartModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Application): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class.java) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context,
                            AppDatabase::class.java, Constants.DATABASE_NAME
                        ).build()
                    }
                }
            }
            return instance
        }
    }

    abstract fun widgetDao(): WidgetDao

    abstract fun featuresDao(): FeaturesDao

    abstract fun cartDao(): CartDao
}