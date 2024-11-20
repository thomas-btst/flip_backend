package com.flip.skateshop.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(Product.DOCUMENT_NAME)
sealed class Product(
    @Id
    val _id: ObjectId,
    val name: String,
    val description: String,
    val price: Long,
    val picture: String,
) {
    companion object {
        const val DOCUMENT_NAME = "products"
    }

    @TypeAlias(Skate.CLASS_NAME)
    class Skate(
        _id: ObjectId,
        name: String,
        description: String,
        price: Long,
        picture: String,
    ) : Product(_id, name, description, price, picture) {
        companion object {
            const val CLASS_NAME = "SKATE"
        }
    }

    @TypeAlias(Deck.CLASS_NAME)
    class Deck(
        _id: ObjectId,
        name: String,
        description: String,
        price: Long,
        picture: String,
    ) : Product(_id, name, description, price, picture) {
        companion object {
            const val CLASS_NAME = "DECK"
        }
    }

    @TypeAlias(Wheel.CLASS_NAME)
    class Wheel(
        _id: ObjectId,
        name: String,
        description: String,
        price: Long,
        picture: String,
    ) : Product(_id, name, description, price, picture) {
        companion object {
            const val CLASS_NAME = "WHEEL"
        }
    }

    @TypeAlias(Bearing.CLASS_NAME)
    class Bearing(
        _id: ObjectId,
        name: String,
        description: String,
        price: Long,
        picture: String,
    ) : Product(_id, name, description, price, picture) {
        companion object {
            const val CLASS_NAME = "BEARING"
        }
    }

    @TypeAlias(GridTape.CLASS_NAME)
    class GridTape(
        _id: ObjectId,
        name: String,
        description: String,
        price: Long,
        picture: String,
    ) : Product(_id, name, description, price, picture) {
        companion object {
            const val CLASS_NAME = "GRID_TAPE"
        }
    }

    @TypeAlias(Truck.CLASS_NAME)
    class Truck(
        _id: ObjectId,
        name: String,
        description: String,
        price: Long,
        picture: String,
    ) : Product(_id, name, description, price, picture) {
        companion object {
            const val CLASS_NAME = "TRUCK"
        }
    }
}

enum class ProductType {
    SKATE,
    DECK,
    WHEEL,
    BEARING,
    GRID_TAPE,
    TRUCK,
}
