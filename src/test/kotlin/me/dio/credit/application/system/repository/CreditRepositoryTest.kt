package me.dio.credit.application.system.repository

import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CreditRepositoryTest {

    @Autowired
    lateinit var creditRepository: CreditRepository

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    private lateinit var customer: Customer
    private lateinit var credit1: Credit
    private lateinit var credit2: Credit

    @BeforeEach fun setup () {
        customer = testEntityManager.persist(buildCustomer())
        credit1 = testEntityManager.persist(buildCredit(customer = customer))
        credit2 = testEntityManager.persist(buildCredit(customer = customer))
    }

    @Test
    fun `should find credit by credit code`() {
        val creditCode1 = UUID.fromString("5cb602ec-25bb-444d-ba10-6058c0098a0a")
        val creditCode2 = UUID.fromString("b4191f53-725d-4d86-9305-0b6155c209d1")
        credit1.creditCode = creditCode1
        credit2.creditCode = creditCode2

        val foundCredit1 = creditRepository.findByCreditCode(creditCode1)
        val foundCredit2 = creditRepository.findByCreditCode(creditCode2)

        assertThat(foundCredit1).isNotNull
        assertThat(foundCredit2).isNotNull
        assertThat(foundCredit1).isSameAs(credit1)
        assertThat(foundCredit2).isSameAs(credit2)
    }

    @Test
    fun `should find all credits by customer id`() {
        val customerId = 1L

        val credits = creditRepository.findAllByCustomerId(customerId)

        assertThat(credits).isNotEmpty
        assertThat(credits.size).isEqualTo(2)
        assertThat(credits).contains(credit1, credit2)
    }

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(500.0),
        dayFirstInstallment: LocalDate = LocalDate.of(2023, Month.APRIL, 22),
        numberOfInstallments: Int = 5,
        customer: Customer
    ): Credit = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
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
    ) = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        address = Address(zipCode, street),
        income = income
    )
}