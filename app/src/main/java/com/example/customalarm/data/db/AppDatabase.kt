package com.example.customalarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.customalarm.data.converter.DateConverter
import com.example.customalarm.data.entity.*

@Database(entities = [AlarmSettingEntity::class, HolidayEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
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
                                    "(1667401200000, '文化の日')," +
                                    "(1669129200000, '勤労感謝の日')," +
                                    "(1672498800000, '元日')," +
                                    "(1672585200000, '休日')," +
                                    "(1673190000000, '成人の日')," +
                                    "(1676041200000, '建国記念の日')," +
                                    "(1677078000000, '天皇誕生日')," +
                                    "(1679324400000, '春分の日')," +
                                    "(1682694000000, '昭和の日')," +
                                    "(1683039600000, '憲法記念日')," +
                                    "(1683126000000, 'みどりの日')," +
                                    "(1683212400000, 'こどもの日')," +
                                    "(1689519600000, '海の日')," +
                                    "(1691679600000, '山の日')," +
                                    "(1694962800000, '敬老の日')," +
                                    "(1695394800000, '秋分の日')," +
                                    "(1696777200000, 'スポーツの日')," +
                                    "(1698937200000, '文化の日')," +
                                    "(1700665200000, '勤労感謝の日')"
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