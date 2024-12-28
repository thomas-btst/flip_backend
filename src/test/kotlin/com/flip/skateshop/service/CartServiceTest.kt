package com.flip.skateshop.service

import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.domain.VerificationKey
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.mapper.CartMapper
import com.flip.skateshop.repository.ProductRepository
import com.flip.skateshop.repository.UserRepository
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.util.ServicesCleaner
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CartServiceTest(
    @Autowired
    private val userRepository: UserRepository,
    @Autowired
    private val productRepository: ProductRepository,
    @Autowired
    private val cartMapper: CartMapper,
) : ServicesCleaner() {
    private val userService = mockk<UserServiceInterface>()
    private val securityUtils = mockk<SecurityUtils>()
    private val cartService = CartService(userService, securityUtils, userRepository, productRepository, cartMapper)

    suspend fun createUser(
        id: ObjectId = ObjectId(),
        firstName: String = "Firstname",
        lastName: String = "LASTNAME",
        email: String = "test@test.fr",
        password: String = "password",
        roles: Set<RoleEnum> = emptySet(),
        activationKey: VerificationKey? = null,
        resetPasswordKey: VerificationKey? = null,
        enabled: Boolean = true,
        refreshTokens: Map<String, Instant> = emptyMap(),
        cart: Map<ObjectId, Long> = emptyMap(),
    ) = userRepository.save(
        User(
            ObjectId(),
            firstName,
            lastName,
            email,
            "0786713311",
            Address(
                "27 rue Flip",
                "",
                "13000",
                "Marseille",
            ),
            password,
            roles,
            null,
            activationKey,
            resetPasswordKey,
            enabled,
            refreshTokens,
            cart,
        ),
    )

    suspend fun createProduct(
        name: String = "Name",
        description: String = "Description",
        price: Long = 8,
        picture: String = "/path/to/file",
    ): Product {
        val product =
            Product.Skate(
                ObjectId(),
                name,
                description,
                price,
                picture,
            )
        productRepository.save(product)
        return product
    }

    @Test
    fun `should add a product to cart`() =
        runTest {
            val user = createUser(cart = emptyMap())
            val product = createProduct()

            val quantity = 8L
            coEvery { securityUtils.getCurrentUserId() } returns user._id
            cartService.addProduct(product._id, quantity)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            assertEquals(quantity, updatedUser.cart[product._id])
        }

    @Test
    fun `should throw error if product does not exist`() =
        runTest {
            val user = createUser(cart = emptyMap())
            coEvery { securityUtils.getCurrentUserId() } returns user._id
            assertThrows<ResponseStatusException> { cartService.addProduct(ObjectId(), 8L) }.let {
                assertEquals(it.statusCode, HttpStatus.NOT_FOUND)
            }
        }

    @Test
    fun `should remove a product from cart correctly`() =
        runTest {
            val productId = ObjectId()
            val user = createUser(cart = mapOf(Pair(productId, 8L)))
            coEvery { securityUtils.getCurrentUserId() } returns user._id
            cartService.removeProduct(productId)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            assertNull(updatedUser.cart[productId])
        }

    @Test
    fun `should throw exception if delete a product that does not exists from cart`() =
        runTest {
            val user = createUser(cart = emptyMap())
            coEvery { securityUtils.getCurrentUserId() } returns user._id
            assertThrows<ResponseStatusException> { cartService.removeProduct(ObjectId()) }.let {
                assertEquals(it.statusCode, HttpStatus.NOT_FOUND)
            }
        }

    @Test
    fun `should retrieve product quantity in the cart successfully`() =
        runTest {
            val productId = ObjectId()
            val quantity = 8L
            val user =
                createUser(
                    cart =
                        mapOf(
                            Pair(productId, quantity),
                        ),
                )
            coEvery { userService.getCurrentUser() } returns user
            assertEquals(quantity, cartService.getCartQuantity(productId).quantity)
            assertEquals(0L, cartService.getCartQuantity(ObjectId()).quantity)
        }

    @Test
    fun `should retrieve all products of a cart successfully`() =
        runTest {
            val product = createProduct()
            val cartQuantity = 8L
            val user =
                createUser(
                    cart =
                        mapOf(
                            Pair(product._id, cartQuantity),
                        ),
                )
            coEvery { userService.getCurrentUser() } returns user
            val cart = cartService.getCart()
            assertEquals(cart.products.size, 1)
            cart.products.first().run {
                assertEquals(id, product._id.toHexString())
                assertEquals(name, product.name)
                assertEquals(price, product.price)
                assertEquals(quantity, cartQuantity)
            }
        }

    @Test
    fun `should clear cart correcty`() =
        runTest {
            val user =
                createUser(
                    cart =
                        mapOf(
                            Pair(ObjectId(), 7L),
                            Pair(ObjectId(), 8L),
                            Pair(ObjectId(), 3L),
                            Pair(ObjectId(), 1L),
                        ),
                )
            coEvery { securityUtils.getCurrentUserId() } returns user._id
            cartService.clearCart()
            val foundUser = userRepository.findById(user._id)
            assertNotNull(foundUser)
            assert(foundUser.cart.isEmpty())
        }
}
