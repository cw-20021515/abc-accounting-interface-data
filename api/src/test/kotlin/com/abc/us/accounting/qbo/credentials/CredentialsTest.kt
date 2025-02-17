package com.abc.us.accounting.qbo.credentials
//
//import com.abc.us.accounting.qbo.domain.entity.Company
//import com.abc.us.accounting.qbo.domain.entity.Credential
//import com.abc.us.accounting.qbo.domain.repository.CompanyRepository
//import com.abc.us.accounting.qbo.domain.repository.CredentialRepository
//import com.abc.us.accounting.qbo.interact.Credentials
//import com.abc.us.accounting.qbo.interact.Discovery
//import com.abc.us.accounting.supports.properties.QBOProperties
//import com.abc.us.accounting.supports.properties.SchedulingProperties
//import com.intuit.oauth2.data.BearerTokenResponse
//import io.kotest.core.spec.style.AnnotationSpec
//import io.kotest.matchers.shouldBe
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.spyk
//import io.mockk.verify
//import org.junit.jupiter.api.DisplayName
//import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
//import java.time.OffsetDateTime
//
//@SpringJUnitConfig
//class CredentialsTest : AnnotationSpec() {
//
//    private val qboProperties: QBOProperties = mockk(relaxed = true)
//    private val schedulingProperties: SchedulingProperties = mockk(relaxed = true)
//    private val discovery: Discovery = mockk(relaxed = true)
//    private val companyRepository: CompanyRepository = mockk(relaxed = true)
//    private val credentialsRepository: CredentialRepository = mockk(relaxed = true)
//
//    private lateinit var credentials: Credentials
//
//    @BeforeEach
//    fun setup() {
//        credentials = spyk(Credentials(qboProperties, schedulingProperties, discovery, companyRepository, credentialsRepository))
//    }
//
//    @Test
//    @DisplayName("Configure company credentials")
//    fun `should configure company credentials`() {
//        // Given
//        val company = Company().apply {
//            code = "ABC"
//            name = "Company ABC"
//            isActive = true
//        }
//        every { companyRepository.findAll() } returns listOf(company)
//
//        // When
//        val companyCredentials = credentials.configureCompanyCredentials()
//
//        // Then
//        companyCredentials.size shouldBe 1
//        companyCredentials["ABC"]?.name shouldBe "Company ABC"
//        verify { companyRepository.findAll() }
//    }
//
//    @Test
//    @DisplayName("Update credential tokens")
//    fun `should update credential tokens`() {
//        // Given
//        val credential = Credential().apply {
//            accessToken = "oldToken"
//        }
//        val tokens = BearerTokenResponse().apply {
//            accessToken = "newToken"
//            refreshToken = "newRefreshToken"
//            tokenType = "Bearer"
//            idToken = "newIdToken"
//            expiresIn = 3600
//            xRefreshTokenExpiresIn = 7200
//        }
//
//        // When
//        credentials.updateCredential(tokens, credential)
//
//        // Then
//        credential.accessToken shouldBe "newToken"
//        credential.refreshToken shouldBe "newRefreshToken"
//        credential.tokenType shouldBe "Bearer"
//    }
//
//    @Test
//    @DisplayName("Configure credentials by realm ID")
//    fun `should configure credentials by realm ID`() {
//        // Given: CompanyCredential 초기화
//        val companyCredential = Credentials.CompanyCredential().apply {
//            code = "COMPANY1"
//        }
//        val companyCredentials = mutableMapOf("COMPANY1" to companyCredential)
//
//        // Given: Credential 초기화
//        val credential = Credential().apply {
//            companyCode = "COMPANY1"
//            realmId = "REALM1"
//            sandboxName = "SANDBOX1"
//        }
//
//        // Mock 설정
//        every { credentialsRepository.findCompanyCredentialsByCompanyCodeIn(listOf("COMPANY1")) } returns listOf(credential)
//
//        // credentials.companies 초기화
//        credentials.companies = companyCredentials
//
//        // When
//        val credentialsByRealmId = credentials.configureCredentialsByRealmId(companyCredentials)
//
//        // Then
//        credentialsByRealmId.size shouldBe 1
//        credentialsByRealmId["REALM1"] shouldBe credential
//
//        // Verify: 적절한 호출 여부 확인
//        verify { credentialsRepository.findCompanyCredentialsByCompanyCodeIn(listOf("COMPANY1")) }
//
//        // Additional Verification
//        companyCredentials["COMPANY1"]!!.credentials["SANDBOX1"] shouldBe credential
//    }
//
//
//
//    @Test
//    @DisplayName("Flush updated tokens to repository")
//    fun `should flush updated tokens to repository`() {
//        // Given
//        val credential = Credential().apply {
//            accessToken = "oldToken"
//        }
//        val tokens = BearerTokenResponse().apply {
//            accessToken = "newToken"
//            refreshToken = "newRefreshToken"
//            tokenType = "Bearer"
//            idToken = "newIdToken"
//            expiresIn = 3600
//            xRefreshTokenExpiresIn = 7200
//        }
//        every { credentialsRepository.save(any()) } answers { firstArg() }
//
//        // When
//        credentials.flush(tokens, credential)
//
//        // Then
//        credential.accessToken shouldBe "newToken"
//        verify { credentialsRepository.save(credential) }
//    }
//
//    @Test
//    @DisplayName("Refresh token with valid condition")
//    fun `should refresh token if condition met`() {
//        // Given
//        val credential = Credential().apply {
//            realmId = "REALM1"
//            accessTokenExpireTime = OffsetDateTime.now().plusMinutes(1) // 만료 시간 설정
//            refreshToken = "validRefreshToken"
//        }
//        credentials.credentialsByRealmId["REALM1"] = credential
//
//        val tokens = BearerTokenResponse().apply {
//            accessToken = "newAccessToken"
//            refreshToken = "newRefreshToken"
//            tokenType = "Bearer"
//            expiresIn = 3600
//            xRefreshTokenExpiresIn = 7200
//        }
//
//        // Mock refreshToken 호출
//        every { credentials.refreshToken(credential) } answers {
//            credential.apply {
//                accessToken = tokens.accessToken
//                refreshToken = tokens.refreshToken
//                accessTokenExpireTime = OffsetDateTime.now().plusSeconds(tokens.expiresIn)
//            }
//            tokens
//        }
//
//        // When
//        credentials.refreshTokenWithCondition({ expire, now -> expire.isBefore(now.plusMinutes(2)) }, beforeMinute = 1)
//
//        // Then
//        credential.accessToken shouldBe "newAccessToken" // 업데이트된 토큰 확인
//        credential.refreshToken shouldBe "newRefreshToken"
//        verify { credentials.refreshToken(credential) } // refreshToken 호출 검증
//    }
//
//
//
//}
