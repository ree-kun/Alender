package com.example.customalarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.customalarm.data.converter.DateTimeConverter
import com.example.customalarm.data.converter.GeneratorConverter
import com.example.customalarm.data.entity.*

@Database(entities = [AlarmSettingEntity::class, HolidayEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeConverter::class, GeneratorConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmSettingDao(): AlarmSettingDao
    abstract fun holidayDao(): HolidayDao

    // https://somachob.com/android-room/
    companion object {
        private const val DBNAME = "custom_alarm_db.sqlite"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DBNAME
                )
                    // TODO 祝日の取得APIが未作成のため、初期データの投入で代えている。
                    //  APIを作成次第、この処理は削除する。
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            val sql = "INSERT INTO 'holiday' VALUES " +
                                    "(19299, '文化の日')," +
                                    "(19319, '勤労感謝の日')," +
                                    "(19358, '元日')," +
                                    "(19359, '休日')," +
                                    "(19366, '成人の日')," +
                                    "(19399, '建国記念の日')," +
                                    "(19411, '天皇誕生日')," +
                                    "(19437, '春分の日')," +
                                    "(19476, '昭和の日')," +
                                    "(19480, '憲法記念日')," +
                                    "(19481, 'みどりの日')," +
                                    "(19482, 'こどもの日')," +
                                    "(19555, '海の日')," +
                                    "(19580, '山の日')," +
                                    "(19618, '敬老の日')," +
                                    "(19623, '秋分の日')," +
                                    "(19639, 'スポーツの日')," +
                                    "(19664, '文化の日')," +
                                    "(19684, '勤労感謝の日')"
                            db.execSQL(sql)
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}