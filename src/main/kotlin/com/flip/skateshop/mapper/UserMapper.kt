package com.flip.skateshop.mapper

import com.flip.skateshop.domain.User
import com.flip.skateshop.web.rest.dto.CreateUserDto
import com.flip.skateshop.web.rest.dto.UserDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class UserMapper {
    fun toUser(userDto: CreateUserDto) = userDto.run {
        User(ObjectId(), username, email, password)
    }

    fun toUserDto(user: User): UserDto = user.run {
        UserDto(_id.toHexString(), username, email)
    }
}