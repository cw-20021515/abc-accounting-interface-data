package com.abc.us.accounting.qbo.syncup.account

import com.intuit.ipp.data.Account
import com.intuit.ipp.data.ReferenceType

data class AccountNode(
    var id : String? = null,
    var accountCode : String? = null,
    var accountName : String? = null,
    var accountType : String? = null,
    var accountSubType : String? = null,
    var accountDescription : String? = null,
    var classification : String? = null,
    var parentAccountCode: String? = null,
    var groupName : String? = null,
    var groupCode : String? = null,
    var groupDescription : String? = null,

    var parent : AccountNode? = null,
    var children : MutableList<AccountNode> = mutableListOf()
) {
    fun addChildren(child : AccountNode) {
        child.parent = this
        children.add(child)
    }
    // 계층 구조를 순회하는 재귀 함수
    fun traverse(action: (AccountNode) -> Unit) {
        action(this)  // 현재 노드에 대한 작업 수행
        children.forEach {child ->
            child.traverse(action)
        }  // 자식 노드 순회
    }
    fun traverseAndCreateAccounts(parentRef: ReferenceType? = null): List<Account> {
        val createdAccounts = mutableListOf<Account>()

//        // 현재 노드의 Account 객체 생성
//        val currentAccount = Account().apply {
//            name = this@AccountNode.name
//            acctNum = this@AccountNode.code
//            accountType = AccountTypeEnum.valueOf(this@AccountNode.accountType!!)
//            accountSubType = this@AccountNode.accountSubType
//            if (parentRef != null) {
//                this.parentRef = parentRef
//            }
//        }
//
//        // 현재 생성된 Account 객체를 리스트에 추가
//        createdAccounts.add(currentAccount)
//
//        // 자식 노드를 재귀적으로 순회하여 Account 객체 생성 및 추가
//        children.forEach { childNode ->
//            val childRef = ReferenceType().apply { value = code } // 현재 노드의 code를 ID처럼 참조
//            createdAccounts.addAll(childNode.traverseAndCreateAccounts(childRef))
//        }

        return createdAccounts
    }
}