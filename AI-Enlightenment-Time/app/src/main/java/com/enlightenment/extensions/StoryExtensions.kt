package com.enlightenment.extensions

import com.enlightenment.domain.model.StoryCategory

val StoryCategory.name: String
    get() = this.toString()
    
val StoryCategory.icon: String
    get() = this.displayName
