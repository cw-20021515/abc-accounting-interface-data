package com.abc.us.accounting.qbo.domain.repository

import com.abc.us.accounting.qbo.domain.entity.QboItemTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemCreateTemplateRepository : JpaRepository<QboItemTemplate, String>  { }