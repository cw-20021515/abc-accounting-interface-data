package com.abc.us.accounting.qbo.interact


//@ExtendWith(MockKExtension::class)
//class CredentialsTest {

//    private val mockCompanyRepository = mockk<CompanyRepository>(relaxed = true)
//    private val mockCredentialsRepository = mockk<CredentialRepository>(relaxed = true)
//    private val mockDiscovery = mockk<Discovery>(relaxed = true)
//    private val mockCredentials = mockk<Credentials>(relaxed = true)
//    private val mockProperties = mockk<QBOProperties>(relaxed = true)
//
//    private lateinit var credentials: Credentials

//    @AnnotationSpec.BeforeEach
//    fun setup() {
//        credentials = Credentials(
//            properties = mockProperties,
//            discovery = mockDiscovery,
//            companyRepository = mockCompanyRepository,
//            credentialsRepository = mockCredentialsRepository
//        )
//    }
//
//    @Test
//    fun `refreshToken should update credentials with new tokens`() {
//        // Arrange
//        val credential = Credential().apply {
//            realmId = "realm123"
//            refreshToken = "oldRefreshToken"
//        }
//
//        val tokenResponse = BearerTokenResponse().apply {
//            accessToken = "newAccessToken"
//            refreshToken = "newRefreshToken"
//            tokenType = "Bearer"
//            idToken = "newIdToken"
//            expiresIn = 3600L
//            xRefreshTokenExpiresIn = 7200L
//        }
//
//        // Spy를 사용하여 일부 동작만 Mocking
//        val credentials = spyk(
//            Credentials(
//                properties = mockk(),
//                discovery = mockk(),
//                companyRepository = mockk(),
//                credentialsRepository = mockk(relaxed = true)
//            )
//        )
//
//        // Authorizer의 동작 Mocking
//        every { credentials.refreshToken(any()) } returns tokenResponse
//
//        // Act
//        val result = credentials.refreshToken(credential)
//
//        // Assert
//        result.accessToken shouldBe "newAccessToken"
//        result.refreshToken shouldBe "newRefreshToken"
//
//        verify {
//            mockCredentialsRepository.save(match {
//                it.accessToken == "newAccessToken" &&
//                    it.refreshToken == "newRefreshToken" &&
//                    it.tokenType == "Bearer" &&
//                    it.idToken == "newIdToken"
//            })
//        }
//    }
//
//
//    @Test
//    fun `refreshToken should throw AuthenticationException when refreshToken is null`() {
//        // Arrange
//        val credential = Credential().apply {
//            realmId = "realm123"
//            refreshToken = null
//        }
//
//        // Act & Assert
//        shouldThrow<AuthenticationException> {
//            mockCredentials.refreshToken(credential)
//        }
//    }
//
//    @Test
//    fun `refreshTokenWithCondition should refresh tokens for expired credentials`() {
//        // Arrange
//        val credential = Credential().apply {
//            realmId = "realm123"
//            refreshToken = "oldRefreshToken"
//            accessTokenExpireTime = OffsetDateTime.now().minusMinutes(1)
//        }
//
//        val tokenResponse = BearerTokenResponse().apply {
//            accessToken = "newAccessToken"
//            refreshToken = "newRefreshToken"
//            tokenType = "Bearer"
//            idToken = "newIdToken"
//            expiresIn = 3600L
//            xRefreshTokenExpiresIn = 7200L
//        }
//
//        credentials.credentialsByRealmId["realm123"] = credential
//
//        every { mockCredentials.refreshToken(any()) } returns tokenResponse
//        every { mockCredentialsRepository.save(any<Credential>()) } answers { firstArg() }
//
//        // Act
//        credentials.refreshTokenWithCondition(condition = { expire, now -> expire.isBefore(now) })
//
//        // Assert
//        verify {
//            mockCredentialsRepository.save(match {
//                it.accessToken == "newAccessToken" &&
//                    it.refreshToken == "newRefreshToken"
//            })
//        }
//    }
//}
