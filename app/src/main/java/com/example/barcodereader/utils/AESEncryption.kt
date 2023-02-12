package com.example.barcodereader.utils


import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AESEncryption {
    companion object {
        private const val ALGORITHM = "AES"
        private const val CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding"
        fun encrypt(plainText: String, encryptionKey: String): String {
            val secretKey = SecretKeySpec(encryptionKey.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray())

            return Base64.getEncoder().encodeToString(encryptedBytes)
        }

        fun decrypt(encryptedText: String?, encryptionKey: String): String {
            val encryptedBytes = Base64.getDecoder().decode(encryptedText)
            val secretKey = SecretKeySpec(encryptionKey.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return String(cipher.doFinal(encryptedBytes))
        }
    }
}
