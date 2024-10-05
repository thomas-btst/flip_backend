package com.flip.skateshop.service

import com.flip.skateshop.mapper.UserMapper
import com.flip.skateshop.repository.UserRepository
import com.flip.skateshop.web.rest.dto.CreateUserDto
import com.flip.skateshop.web.rest.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
) {
    suspend fun addUser(userDto: CreateUserDto): ObjectId {
        return userRepository.save(userMapper.toUser(userDto))._id
    }

    suspend fun getUsers(): Flow<UserDto> {
        return userRepository.findAll().map(userMapper::toUserDto)
    }
}