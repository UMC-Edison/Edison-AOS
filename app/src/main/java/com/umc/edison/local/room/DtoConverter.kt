package com.umc.edison.local.room

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import java.util.Date

class DtoConverter {
    @TypeConverter
    fun fromDate(date: Date) = date.time

    @TypeConverter
    fun toDate(time: Long) = Date(time)

    @TypeConverter
    fun fromStringList(list: List<String>) = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()
        .toJson(list)

    @TypeConverter
    fun toStringList(json: String) = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()
        .fromJson(json, Array<String>::class.java)
        .toList()

}