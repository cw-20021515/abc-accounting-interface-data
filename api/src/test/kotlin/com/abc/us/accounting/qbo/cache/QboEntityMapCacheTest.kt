package com.abc.us.accounting.qbo.cache

//class AssociationMapCacheTest : AnnotationSpec() {
//    private val credentials = mockk<Credentials>()
//    private val qboService = mockk<QBOService>()
//    private val repository = mockk<AssociationMapRepository>()
//    private val associationCache = AssociationMapCache(credentials, qboService, repository)
//
//    @BeforeEach
//    fun setUp() {
//        clearMocks(credentials, qboService, repository)
//    }
//
//    @Test
//    fun `should create and save association map for QBO_Account`() {
//        // Given
//        val credential = Credential().apply {
//            realmId = "REALM1"
//            companyCode = "COMPANY1"
//            sandboxName = "SANDBOX1"
//        }
//        val account = Account().apply {
//            id = "ACCOUNT1"
//            acctNum = "12345"
//            name = "Test Account"
//            accountType = AccountTypeEnum.BANK
//            syncToken = "SYNC1"
//        }
//        val expectedAssociation = AssociationMap().apply {
//            associatedId = account.id
//            associatedValue = account.acctNum
//            associatedName = account.name
//            associatedType = account.accountType.name
//            associatedEntity = Account::class.simpleName
//            syncToken = account.syncToken
//            realmId = credential.realmId
//            companyCode = credential.companyCode
//            sandboxName = credential.sandboxName
//            isActive = true
//        }
//        every { credentials.getCredential() } returns credential
//        every { repository.findEntity(any(), any(), any()) } returns Optional.empty()
//        every { repository.save(any()) } answers {
//            val savedEntity = firstArg<AssociationMap>()
//            // 저장 시 자동 생성되는 필드 반영
//            expectedAssociation.apply {
//                createTime = savedEntity.createTime
//                updateTime = savedEntity.updateTime
//            }
//            savedEntity
//        }
//
//        // When
//        val result = associationCache.put(account)
//
//        // Then
//        result shouldNotBe null
//        result shouldBe expectedAssociation
//        verify { repository.save(match { it.associatedId == "ACCOUNT1" }) }
//    }
//
//
//    @Test
//    fun `should update existing association map`() {
//        // Given
//        val credential = Credential().apply {
//            realmId = "REALM1"
//            companyCode = "COMPANY1"
//            sandboxName = "SANDBOX1"
//        }
//        val account = QBO_Account().apply {
//            id = "ACCOUNT1"
//            acctNum = "12345"
//            name = "Updated Account"
//            accountType = AccountTypeEnum.BANK
//            syncToken = "SYNC2"
//        }
//        val existingAssociation = AssociationMap().apply {
//            associatedId = account.id
//            associatedValue = "12345"
//            associatedName = "Old Account"
//            associatedType = "BANK"
//            associatedEntity = QBO_Account::class.simpleName
//            syncToken = "SYNC1"
//            createTime = OffsetDateTime.now().minusDays(1)
//            realmId = credential.realmId
//            companyCode = credential.companyCode
//            sandboxName = credential.sandboxName
//            isActive = true
//        }
//        val updatedAssociation = existingAssociation.apply {
//            associatedName = account.name
//            syncToken = account.syncToken
//            updateTime = OffsetDateTime.now()
//        }
//
//        every { credentials.getCredential() } returns credential
//        every { repository.findEntity(any(), any(), any()) } returns Optional.of(existingAssociation)
//        every { repository.save(any()) } answers { firstArg() }
//
//        // When
//        val result = associationCache.put(account)
//
//        // Then
//        result shouldBe updatedAssociation
//        verify { repository.save(match { it.associatedName == "Updated Account" }) }
//    }
//
//    @Test
//    fun `should count active entities`() {
//        // Given
//        val companyCode = "COMPANY1"
//        val entityClass = QBO_Account::class
//        every { repository.countByCompanyCodeAndAssociatedEntityAndActiveIsTrue(companyCode, entityClass.simpleName!!) } returns 10L
//
//        // When
//        val result = associationCache.count(companyCode, entityClass)
//
//        // Then
//        result shouldBe 10L
//        verify { repository.countByCompanyCodeAndAssociatedEntityAndActiveIsTrue(companyCode, entityClass.simpleName!!) }
//    }
//
//    @Test
//    fun `should configure entities if count is zero`() {
//        // Given
//        val code = "COMPANY1"
//        val entityClass = QBO_Account::class
//        val credential = Credential().apply {
//            realmId = "REALM1"
//            companyCode = code
//            sandboxName = "SANDBOX1"
//        }
//
//        every { credentials.getCredential() } returns credential // Mock 설정 추가
//        every { repository.countByCompanyCodeAndAssociatedEntityAndActiveIsTrue(code, entityClass.simpleName!!) } returns 0L andThen 10L
//        every { qboService.selectAll(entityClass, any()) } answers {
//            val callback = secondArg<(Any) -> Unit>()
//            callback(QBO_Account().apply {
//                id = "ACCOUNT1"
//                acctNum = "12345"
//                name = "Test Account"
//                accountType = AccountTypeEnum.BANK
//                syncToken = "SYNC1"
//            })
//        }
//        every { repository.findEntity(credential.realmId!!, "Account", "12345") } returns Optional.empty() // Mock 설정 추가
//        every { repository.save(any()) } answers { firstArg() }
//
//        // When
//        val result = associationCache.configure(code, entityClass)
//
//        // Then
//        result shouldBe 10L
//        verify { qboService.selectAll(entityClass, any()) }
//        verify { repository.findEntity(credential.realmId!!, "Account", "12345") }
//        verify { repository.save(any()) }
//    }
//}
