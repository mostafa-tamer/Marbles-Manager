import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AESEncryption {
    private const val ALGORITHM = "AES"
    private const val CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding"
    @Throws(Exception::class)
    fun encrypt(plainText: String, encryptionKey: String): String {
        val secretKey = SecretKeySpec(encryptionKey.toByteArray(), ALGORITHM)
        val cipher: Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes: ByteArray = cipher.doFinal(plainText.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    @Throws(Exception::class)
    fun decrypt(encryptedText: String?, encryptionKey: String): String {
        val encryptedBytes: ByteArray = Base64.getDecoder().decode(encryptedText)
        val secretKey = SecretKeySpec(encryptionKey.toByteArray(), ALGORITHM)
        val cipher: Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(encryptedBytes))
    }
}