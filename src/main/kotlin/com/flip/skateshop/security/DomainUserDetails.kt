package com.flip.skateshop.security

import com.flip.skateshop.domain.User
import org.bson.types.ObjectId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*


class DomainUserDetails(private val user: User) : UserDetails {

    val id: ObjectId = user._id

    //    val firstName: String = user.firstName
//    val lastName: String = user.lastName
    val enabled: Boolean = user.enabled

    private val authorities = Collections.unmodifiableList(user.roles.map { SimpleGrantedAuthority(it.name) })

    override fun getUsername(): String {
        return user.email
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities
    }
}
