package com.enlightenment.data.local.converter

import androidx.room.TypeConverter
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Question
import com.enlightenment.domain.model.StoryCategory
import com.enlightenment.security.AuditCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromAgeGroup(ageGroup: AgeGroup): String {
        return ageGroup.name
    }
    
    @TypeConverter
    fun toAgeGroup(name: String): AgeGroup {
        return AgeGroup.valueOf(name)
    }
    
    @TypeConverter
    fun fromStoryCategory(category: StoryCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toStoryCategory(name: String): StoryCategory {
        return StoryCategory.valueOf(name)
    }
    
    @TypeConverter
    fun fromQuestionList(questions: List<Question>): String {
        return gson.toJson(questions)
    }
    
    @TypeConverter
    fun toQuestionList(json: String): List<Question> {
        val type = object : TypeToken<List<Question>>() {}.type
        return gson.fromJson(json, type)
    }
    
    @TypeConverter
    fun fromCategoryList(categories: List<StoryCategory>): String {
        return categories.joinToString(",") { it.name }
    }
    
    @TypeConverter
    fun toCategoryList(data: String): List<StoryCategory> {
        return if (data.isEmpty()) {
            emptyList()
        } else {
            data.split(",").map { StoryCategory.valueOf(it) }
        }
    }
    
    @TypeConverter
    fun fromStringSet(set: Set<String>): String {
        return set.joinToString(",")
    }
    
    @TypeConverter
    fun toStringSet(data: String): Set<String> {
        return if (data.isEmpty()) {
            emptySet()
        } else {
            data.split(",").toSet()
        }
    }
    
    @TypeConverter
    fun fromAuditCategory(category: AuditCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toAuditCategory(name: String): AuditCategory {
        return AuditCategory.valueOf(name)
    }
    
    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }
    
    @TypeConverter
    fun toStringMap(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}