package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CreditServiceTest {

    @MockK
    private lateinit var creditRepository: CreditRepository

    @MockK
    private lateinit var customerService: CustomerService

    @InjectMockKs
    private lateinit var creditService: CreditService

    @Test
    fun `should create a credit`() {
        val customerId = 123L
        val initialCustomer = Customer(id = customerId)
        val customer = buildCustomer(id = customerId)
        val credit = buildCredit(customer = initialCustomer)

        every { customerService.findById(customerId) } returns customer
        every { creditRepository.save(any()) } returns credit

        val actual = creditService.save(credit)

        assertThat(actual).isNotNull
        assertThat(actual.customer?.id).isEqualTo(customerId)
        verify(exactly = 1) { creditRepository.save(any()) }
    }

    @Test
    fun `should not create a credit if initial date is more than three months in the future`() {
        val customerId = 123L
        val laterDate = LocalDate.now().plusMonths(4)
        val initialCustomer = Customer(id = customerId)
        val credit = buildCredit(customer = initialCustomer, dayFirstInstallment = laterDate)

        assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { creditService.save(credit) }
            .withMessage("Invalid Date")
    }

    @Test
    fun `should return the credits found of a customer`() {
        val credits = emptyList<Credit>()

        every { creditRepository.findAllByCustomerId(any()) } returns credits

        val actual = creditService.findAllByCustomer(123L)

        assertThat(actual).isEqualTo(credits)
        verify(exactly = 1) { creditRepository.findAllByCustomerId(any()) }
    }

    @Test
    fun `should get a credit using credit code and customer id`() {
        val customerId = 123L
        val initialCustomer = Customer(id = customerId)
        val creditCode = UUID.randomUUID()
        val credit = buildCredit(customer = initialCustomer, creditCode = creditCode)

        every { creditRepository.findByCreditCode(creditCode) } returns credit

        val actual = creditService.findByCreditCode(customerId, creditCode)

        assertThat(actual)
            .isNotNull
            .isSameAs(credit)
        verify(exactly = 1) { creditRepository.findByCreditCode(creditCode) }
    }

    @Test
    fun `should get an error if credit code is invalid`() {
        val customerId = 123L
        val creditCode = UUID.randomUUID()

        every { creditRepository.findByCreditCode(creditCode) } returns null

        assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { creditService.findByCreditCode(customerId, creditCode) }
            .withMessage("CreditCode $creditCode not found")
    }

    @Test
    fun `should not get a credit if customer is not the one of the credit`() {
        val customerId = 123L
        val wrongCustomerId = 321L
        val initialCustomer = Customer(id = customerId)
        val creditCode = UUID.randomUUID()
        val credit = buildCredit(customer = initialCustomer, creditCode = creditCode)

        every { creditRepository.findByCreditCode(creditCode) } returns credit

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { creditService.findByCreditCode(wrongCustomerId, creditCode) }
            .withMessage("Contact admin")
    }

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(500.0),
        dayFirstInstallment: LocalDate = LocalDate.of(2023, Month.APRIL, 22),
        numberOfInstallments: Int = 5,
        creditCode: UUID = UUID.randomUUID(),
        customer: Customer
    ): Credit = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        creditCode = creditCode,
        customer = customer
    )

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