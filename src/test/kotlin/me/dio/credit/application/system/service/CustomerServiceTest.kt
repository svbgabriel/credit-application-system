package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    lateinit var customerRepository: CustomerRepository

    @InjectMockKs
    lateinit var customerService: CustomerService

    @Test
    fun `should create customer`() {
        val customer = buildCustomer()

        every { customerRepository.save(any()) } returns customer

        val actual = customerService.save(customer)

        assertThat(actual).isNotNull
        assertThat(actual).isSameAs(customer)
        verify(exactly = 1) { customerRepository.save(any()) }
    }

    @Test
    fun `should find customer by id`() {
        val id = Random.nextLong()
        val customer = buildCustomer(id = id)

        every { customerRepository.findById(id) } returns Optional.of(customer)

        val actual = customerService.findById(id)

        assertThat(actual).isNotNull
        assertThat(actual).isExactlyInstanceOf(Customer::class.java)
        assertThat(actual).isSameAs(customer)
        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `should not find customer by invalid id throw BusinessException`() {
        val id = Random.nextLong()

        every { customerRepository.findById(id) } returns Optional.empty()

        assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { customerService.findById(id) }
            .withMessage("Id $id not found")
        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `should delete customer by id`() {
        val id = Random.nextLong()
        val customer = buildCustomer(id = id)

        every { customerRepository.findById(id) } returns Optional.of(customer)
        every { customerRepository.delete(customer) } just runs

        customerService.delete(id)

        verify(exactly = 1) { customerRepository.findById(id) }
        verify(exactly = 1) { customerRepository.delete(customer) }
    }

    private fun buildCustomer(
        firstName: String = "First",
        lastName: String = "Last",
        cpf: String = "11111111111",
        email: String = "test@test.com",
        password: String = "122345",
        zipCode: String = "123456",
        street: String = "Rua Test",
        income: BigDecimal = BigDecimal(1000),
        id: Long = 1L
    ) = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        address = Address(zipCode, street),
        income = income,
        id = id
    )
}