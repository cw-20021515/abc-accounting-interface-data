package com.abc.us.accounting.supports.utils

import com.abc.us.accounting.config.Constants
import org.apache.commons.codec.binary.Hex
import org.hashids.Hashids
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Hashs {
    val SALT = Constants.APP_NAME
    val MIN_LENGTH = 20
    val hashIds = Hashids(SALT, MIN_LENGTH)

    val HASH_LENGTH = Constants.HASH_LENGTH

    fun hash (vararg items: Any?): String {
        // items의 hashCode는 내용은 같아도 바뀌는 경우가 많음: 문자열로 hashCode 생성하는게 안전함
        val contents = items.mapNotNull { it?.toString() }.joinToString(separator = "|")
        return sha256Hash(contents)
    }

    fun encodeHash(vararg items: Any?): String {
        val decodeValue = items.joinToString(separator = "|") { it.toString() }
        val hexTxId = Hex.encodeHex(decodeValue.toByteArray())
        val encodedTxId = hashIds.encodeHex(String(hexTxId))
        return encodedTxId
    }

    fun decodeHash(encodedTxId: String): String {
        val hexTxId = hashIds.decodeHex(encodedTxId)
        val decodeValue = String(Hex.decodeHex(hexTxId))
        return decodeValue
    }

    private fun salted(input: String): String {
        return input + SALT
    }

    fun md5Hash(input: String, length:Int = HASH_LENGTH): String {
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(salted(input).toByteArray())
        val hash= hashBytes.joinToString("") { "%02x".format(it) }
        return hash.take(length)
    }

    fun sha1Hash(input: String, length:Int = HASH_LENGTH): String {
        val md = MessageDigest.getInstance("SHA-1")
        val hashBytes = md.digest(salted(input).toByteArray())
        val hash= hashBytes.joinToString("") { "%02x".format(it) }
        return hash.take(length)
    }

    // 3. SHA-256 해시 (SHA-2 계열, 현재 많이 사용됨)
    fun sha256Hash(input: String, length:Int = HASH_LENGTH): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(salted(input).toByteArray())
        val hash = hashBytes.joinToString("") { "%02x".format(it) }
        return hash.take(length)
    }
    fun sha256ToInt(input: String): Int {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.fold(0) { acc, byte -> acc * 31 + byte.toInt() }
    }


    // 4. SHA-512 해시 (SHA-2 계열, 더 강력한 보안)
    fun sha512Hash(input: String, length:Int = HASH_LENGTH): String {
        val md = MessageDigest.getInstance("SHA-512")
        val hashBytes = md.digest(salted(input).toByteArray())
        val hash= hashBytes.joinToString("") { "%02x".format(it) }
        return hash.take(length)
    }

    // 5. HMAC-SHA256 (키 기반 해시)
    fun hmacSha256(input: String, key: String, length:Int = HASH_LENGTH): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val hashBytes = mac.doFinal(salted(input).toByteArray())
        val hash= Base64.getEncoder().encodeToString(hashBytes)
        return hash.take(length)
    }


}