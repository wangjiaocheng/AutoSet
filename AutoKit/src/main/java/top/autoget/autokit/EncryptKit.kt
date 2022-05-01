package top.autoget.autokit

import top.autoget.autokit.ConvertKit.bytes2HexString
import top.autoget.autokit.ConvertKit.hexString2Bytes
import top.autoget.autokit.EncodeKit.base64Decode
import top.autoget.autokit.EncodeKit.base64Encode
import top.autoget.autokit.StringKit.isSpace
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.security.*
import java.security.spec.*
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.*
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptKit {
    fun xorEncode(string: String, privateKey: String): String = IntArray(string.length).apply {
        var i = 0
        for ((index, char) in string.withIndex()) {
            if (i == privateKey.length) i = 0
            this[index] = char.toInt() xor privateKey[i++].toInt()
        }
    }.let { ints ->
        var result = ""
        for (i in string.indices) {
            result += when {
                ints[i] < 10 -> "00${ints[i]}"
                ints[i] < 100 -> "0${ints[i]}"
                else -> "${ints[i]}"
            }
        }
        return result
    }//异或加密

    fun xorDecode(string: String, privateKey: String): String = CharArray(string.length / 3).apply {
        var index = 0
        var i = 0
        while (index < string.length / 3) {
            if (i == privateKey.length) i = 0
            this[index] =
                (string.substring(
                    index * 3,
                    index * 3 + 3
                ).toInt() xor privateKey[i++].toInt()).toChar()
            index++
        }
    }.let { chars ->
        var result = ""
        for (i in 0 until string.length / 3) {
            result += chars[i]
        }
        return result
    }//异或解密

    fun checkMD2(data: String?, md2: String): Boolean = encryptMD2ToString(data).equals(md2, true)
    fun encryptMD2ToString(data: String?): String =
        data?.let { if (it.isEmpty()) "" else encryptMD2ToString(it.toByteArray()) } ?: ""

    fun encryptMD2ToString(data: ByteArray): String = bytes2HexString(encryptMD2(data))
    fun encryptMD2(data: ByteArray): ByteArray? = hashTemplate(data, "MD2")
    fun checkMD5(data: String?, md5: String): Boolean = encryptMD5ToString(data).equals(md5, true)
    fun encryptMD5ToString(data: String?): String =
        data?.let { if (it.isEmpty()) "" else encryptMD5ToString(it.toByteArray()) } ?: ""

    fun encryptMD5ToString(data: ByteArray): String = bytes2HexString(encryptMD5(data))
    fun encryptMD5(data: ByteArray): ByteArray? = hashTemplate(data, "MD5")
    fun encryptMD5ToStringWithSalt(data: String?, salt: String?): String = when {
        data != null && salt != null -> bytes2HexString(encryptMD5(("$data$salt").toByteArray()))
        data != null && salt == null -> bytes2HexString(encryptMD5(data.toByteArray()))
        data == null && salt != null -> bytes2HexString(encryptMD5(salt.toByteArray()))
        else -> ""
    }

    fun encryptMD5ToStringWithSalt(data: ByteArray?, salt: ByteArray?): String = when {
        data != null && salt != null -> ByteArray(data.size + salt.size).apply {
            System.arraycopy(data, 0, this, 0, data.size)
            System.arraycopy(salt, 0, this, data.size, salt.size)
        }.let { bytes2HexString(encryptMD5(it)) }
        data != null && salt == null -> bytes2HexString(encryptMD5(data))
        data == null && salt != null -> bytes2HexString(encryptMD5(salt))
        else -> ""
    }

    fun checkSHA1(data: String?, sha1: String): Boolean =
        encryptSHA1ToString(data).equals(sha1, true)

    fun encryptSHA1ToString(data: String?): String =
        data?.let { if (it.isEmpty()) "" else encryptSHA1ToString(it.toByteArray()) } ?: ""

    fun encryptSHA1ToString(data: ByteArray): String = bytes2HexString(encryptSHA1(data))
    fun encryptSHA1(data: ByteArray): ByteArray? = hashTemplate(data, "SHA-1")
    fun checkSHA224(data: String?, sha224: String): Boolean =
        encryptSHA224ToString(data).equals(sha224, true)

    fun encryptSHA224ToString(data: String?): String =
        data?.let { if (it.isEmpty()) "" else encryptSHA224ToString(it.toByteArray()) } ?: ""

    fun encryptSHA224ToString(data: ByteArray): String = bytes2HexString(encryptSHA224(data))
    fun encryptSHA224(data: ByteArray): ByteArray? = hashTemplate(data, "SHA224")
    fun checkSHA256(data: String?, sha256: String): Boolean =
        encryptSHA256ToString(data).equals(sha256, true)

    fun encryptSHA256ToString(data: String?): String =
        data?.let { if (it.isEmpty()) "" else encryptSHA256ToString(it.toByteArray()) } ?: ""

    fun encryptSHA256ToString(data: ByteArray): String = bytes2HexString(encryptSHA256(data))
    fun encryptSHA256(data: ByteArray): ByteArray? = hashTemplate(data, "SHA-256")
    fun checkSHA384(data: String?, sha384: String): Boolean =
        encryptSHA384ToString(data).equals(sha384, true)

    fun encryptSHA384ToString(data: String?): String =
        data?.let { if (it.isEmpty()) "" else encryptSHA384ToString(it.toByteArray()) } ?: ""

    fun encryptSHA384ToString(data: ByteArray): String = bytes2HexString(encryptSHA384(data))
    fun encryptSHA384(data: ByteArray): ByteArray? = hashTemplate(data, "SHA-384")
    fun checkSHA512(data: String?, sha512: String): Boolean =
        encryptSHA512ToString(data).equals(sha512, true)

    fun encryptSHA512ToString(data: String?): String =
        data?.let { if (it.isEmpty()) "" else encryptSHA512ToString(it.toByteArray()) } ?: ""

    fun encryptSHA512ToString(data: ByteArray): String = bytes2HexString(encryptSHA512(data))
    fun encryptSHA512(data: ByteArray): ByteArray? = hashTemplate(data, "SHA-512")
    private fun hashTemplate(data: ByteArray?, algorithm: String): ByteArray? = data?.let {
        when {
            it.isEmpty() -> null
            else -> try {
                MessageDigest.getInstance(algorithm).apply { update(it) }.digest()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                null
            }
        }
    }

    fun checkHmacMD5(data: String?, key: String?, hmacMD5: String): Boolean =
        encryptHmacMD5ToString(data, key).equals(hmacMD5, true)

    fun encryptHmacMD5ToString(data: String?, key: String?): String = when {
        data == null || data.isEmpty() || key == null || key.isEmpty() -> ""
        else -> encryptHmacMD5ToString(data.toByteArray(), key.toByteArray())
    }

    fun encryptHmacMD5ToString(data: ByteArray, key: ByteArray): String =
        bytes2HexString(encryptHmacMD5(data, key))

    fun encryptHmacMD5(data: ByteArray, key: ByteArray): ByteArray? =
        hmacTemplate(data, key, "HmacMD5")

    fun checkHmacSHA1(data: String?, key: String?, hmacSHA1: String): Boolean =
        encryptHmacSHA1ToString(data, key).equals(hmacSHA1, true)

    fun encryptHmacSHA1ToString(data: String?, key: String?): String = when {
        data == null || data.isEmpty() || key == null || key.isEmpty() -> ""
        else -> encryptHmacSHA1ToString(data.toByteArray(), key.toByteArray())
    }

    fun encryptHmacSHA1ToString(data: ByteArray, key: ByteArray): String =
        bytes2HexString(encryptHmacSHA1(data, key))

    fun encryptHmacSHA1(data: ByteArray, key: ByteArray): ByteArray? =
        hmacTemplate(data, key, "HmacSHA1")

    fun checkHmacSHA224(data: String?, key: String?, hmacSHA224: String): Boolean =
        encryptHmacSHA224ToString(data, key).equals(hmacSHA224, true)

    fun encryptHmacSHA224ToString(data: String?, key: String?): String = when {
        data == null || data.isEmpty() || key == null || key.isEmpty() -> ""
        else -> encryptHmacSHA224ToString(data.toByteArray(), key.toByteArray())
    }

    fun encryptHmacSHA224ToString(data: ByteArray, key: ByteArray): String =
        bytes2HexString(encryptHmacSHA224(data, key))

    fun encryptHmacSHA224(data: ByteArray, key: ByteArray): ByteArray? =
        hmacTemplate(data, key, "HmacSHA224")

    fun checkHmacSHA256(data: String?, key: String?, hmacSHA256: String): Boolean =
        encryptHmacSHA256ToString(data, key).equals(hmacSHA256, true)

    fun encryptHmacSHA256ToString(data: String?, key: String?): String = when {
        data == null || data.isEmpty() || key == null || key.isEmpty() -> ""
        else -> encryptHmacSHA256ToString(data.toByteArray(), key.toByteArray())
    }

    fun encryptHmacSHA256ToString(data: ByteArray, key: ByteArray): String =
        bytes2HexString(encryptHmacSHA256(data, key))

    fun encryptHmacSHA256(data: ByteArray, key: ByteArray): ByteArray? =
        hmacTemplate(data, key, "HmacSHA256")

    fun checkHmacSHA384(data: String?, key: String?, hmacSHA384: String): Boolean =
        encryptHmacSHA384ToString(data, key).equals(hmacSHA384, true)

    fun encryptHmacSHA384ToString(data: String?, key: String?): String = when {
        data == null || data.isEmpty() || key == null || key.isEmpty() -> ""
        else -> encryptHmacSHA384ToString(data.toByteArray(), key.toByteArray())
    }

    fun encryptHmacSHA384ToString(data: ByteArray, key: ByteArray): String =
        bytes2HexString(encryptHmacSHA384(data, key))

    fun encryptHmacSHA384(data: ByteArray, key: ByteArray): ByteArray? =
        hmacTemplate(data, key, "HmacSHA384")

    fun checkHmacSHA512(data: String?, key: String?, hmacSHA512: String): Boolean =
        encryptHmacSHA512ToString(data, key).equals(hmacSHA512, true)

    fun encryptHmacSHA512ToString(data: String?, key: String?): String = when {
        data == null || data.isEmpty() || key == null || key.isEmpty() -> ""
        else -> encryptHmacSHA512ToString(data.toByteArray(), key.toByteArray())
    }

    fun encryptHmacSHA512ToString(data: ByteArray, key: ByteArray): String =
        bytes2HexString(encryptHmacSHA512(data, key))

    fun encryptHmacSHA512(data: ByteArray, key: ByteArray): ByteArray? =
        hmacTemplate(data, key, "HmacSHA512")

    private fun hmacTemplate(data: ByteArray?, key: ByteArray?, algorithm: String): ByteArray? =
        when {
            data == null || data.isEmpty() || key == null || key.isEmpty() -> null
            else -> try {
                Mac.getInstance(algorithm).apply { init(SecretKeySpec(key, algorithm)) }
                    .doFinal(data)
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
                null
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                null
            }
        }

    fun checkFile(filePath: String?, algorithm: String, string: String): Boolean =
        encryptFile2String(filePath, algorithm).equals(string, true)

    fun encryptFile2String(filePath: String?, algorithm: String): String =
        bytes2HexString(encryptFile(filePath, algorithm))

    fun encryptFile(filePath: String?, algorithm: String): ByteArray? =
        encryptFile(filePath?.let { if (isSpace(it)) null else File(it) }, algorithm)

    fun checkFile(file: File, algorithm: String, string: String): Boolean =
        encryptFile2String(file, algorithm).equals(string, true)

    fun encryptFile2String(file: File?, algorithm: String): String =
        bytes2HexString(encryptFile(file, algorithm))

    fun encryptFile(file: File?, algorithm: String): ByteArray? = file?.let {
        try {
            MessageDigest.getInstance(algorithm).apply {
                FileInputStream(file).use { fileInputStream ->
                    ByteArray(256 * 1024).let { bytes ->
                        while (true) {
                            if (fileInputStream.read(bytes) != -1) update(bytes) else break
                        }
                    }
                }
            }.digest()
/*            FileInputStream(file).use { fileInputStream ->
                DigestInputStream(
                    fileInputStream, MessageDigest.getInstance(algorithm)
                ).use { digestInputStream ->
                    digestInputStream.apply {
                        ByteArray(256 * 1024).let { bytes ->
                            while (true) {
                                if (read(bytes) <= 0) break
                            }//while (read(bytes) > 0);
                        }
                    }.messageDigest.digest()
                }
            }*/
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Throws(NoSuchAlgorithmException::class)
    fun initKeyDES(): ByteArray =
        KeyGenerator.getInstance("DES").apply { init(56) }.generateKey().encoded

    fun encryptDES2Base64(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray = base64Encode(encryptDES(data, key, transformation, iv))

    fun encryptDES2HexString(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): String = bytes2HexString(encryptDES(data, key, transformation, iv))

    fun encryptDES(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = symmetricTemplate(data, key, "DES", transformation, iv, true)//8字节密钥

    fun decryptDES4Base64(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = decryptDES(base64Decode(data), key, transformation, iv)

    fun decryptDES4HexString(
        data: String, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = decryptDES(hexString2Bytes(data), key, transformation, iv)

    fun decryptDES(
        data: ByteArray?, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = symmetricTemplate(data, key, "DES", transformation, iv, false)//8字节密钥

    @Throws(NoSuchAlgorithmException::class)
    fun initKey3DES(): ByteArray =
        KeyGenerator.getInstance("DESede").apply { init(168) }.generateKey().encoded

    fun encrypt3DES2Base64(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray = base64Encode(encrypt3DES(data, key, transformation, iv))

    fun encrypt3DES2HexString(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): String = bytes2HexString(encrypt3DES(data, key, transformation, iv))

    fun encrypt3DES(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = symmetricTemplate(data, key, "DESede", transformation, iv, true)//24字节密钥

    fun decrypt3DES4Base64(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = decrypt3DES(base64Decode(data), key, transformation, iv)

    fun decrypt3DES4HexString(
        data: String, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = decrypt3DES(hexString2Bytes(data), key, transformation, iv)

    fun decrypt3DES(
        data: ByteArray?, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = symmetricTemplate(data, key, "DESede", transformation, iv, false)//24字节密钥

    @Throws(NoSuchAlgorithmException::class)
    fun initKeyAES(): ByteArray =
        KeyGenerator.getInstance("AES").apply { init(256) }.generateKey().encoded

    fun encryptAES2Base64(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray = base64Encode(encryptAES(data, key, transformation, iv))

    fun encryptAES2HexString(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): String = bytes2HexString(encryptAES(data, key, transformation, iv))

    fun encryptAES(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = symmetricTemplate(data, key, "AES", transformation, iv, true)//16、24、32字节密钥

    fun decryptAES4Base64(
        data: ByteArray, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = decryptAES(base64Decode(data), key, transformation, iv)

    fun decryptAES4HexString(
        data: String, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = decryptAES(hexString2Bytes(data), key, transformation, iv)

    fun decryptAES(
        data: ByteArray?, key: ByteArray, transformation: String, iv: ByteArray
    ): ByteArray? = symmetricTemplate(data, key, "AES", transformation, iv, false)//16、24、32字节密钥

    var AES_Transformation = "AES/ECB/PKCS5Padding"
    var DES_Transformation = "DES/ECB/PKCS5Padding"
    var TripleDES_Transformation = "DESede/ECB/PKCS5Padding"
    private fun symmetricTemplate(
        data: ByteArray?, key: ByteArray?, algorithm: String,
        transformation: String, iv: ByteArray?, isEncrypt: Boolean
    ): ByteArray? = when {
        data == null || data.isEmpty() || key == null || key.isEmpty() -> null
        else -> try {
            when (algorithm) {
                "DES" -> SecretKeyFactory.getInstance(algorithm).generateSecret(DESKeySpec(key))
                else -> SecretKeySpec(key, algorithm)
            }.let { secretKey ->
                (if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE).let { mode ->
                    Cipher.getInstance(transformation).apply {
                        iv?.let {
                            when {
                                iv.isEmpty() -> init(mode, secretKey)
                                else -> init(mode, secretKey, IvParameterSpec(iv))//SecureRandom()
                            }
                        } ?: init(mode, secretKey)
                    }.doFinal(data)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    @Throws(NoSuchAlgorithmException::class)
    fun initKeyRSA(isPublic: Boolean): ByteArray = KeyPairGenerator.getInstance("RSA").apply {
        initialize(
            1024, SecureRandom(
                SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).toByteArray()
            )
        )
    }.generateKeyPair().run { (if (isPublic) public else private).encoded }

    @Throws(NoSuchAlgorithmException::class)
    fun initKeyRSA(
        modulus: ByteArray, exponent: ByteArray, isPublic: Boolean
    ): ByteArray = KeyFactory.getInstance("RSA").run {
        when {
            isPublic -> generatePublic(RSAPublicKeySpec(BigInteger(modulus), BigInteger(exponent)))
            else -> generatePrivate(RSAPrivateKeySpec(BigInteger(modulus), BigInteger(exponent)))
        }.encoded
    }

    fun encryptRSA2Base64(
        data: ByteArray, key: ByteArray, isPublicKey: Boolean, transformation: String
    ): ByteArray = base64Encode(encryptRSA(data, key, isPublicKey, transformation))

    fun encryptRSA2HexString(
        data: ByteArray, key: ByteArray, isPublicKey: Boolean, transformation: String
    ): String = bytes2HexString(encryptRSA(data, key, isPublicKey, transformation))

    fun encryptRSA(
        data: ByteArray, key: ByteArray, isPublicKey: Boolean, transformation: String
    ): ByteArray? = rsaTemplate(data, key, isPublicKey, transformation, true)

    fun decryptRSA4Base64(
        data: ByteArray, key: ByteArray, isPublicKey: Boolean, transformation: String
    ): ByteArray? = decryptRSA(base64Decode(data), key, isPublicKey, transformation)

    fun decryptRSA4HexString(
        data: String, key: ByteArray, isPublicKey: Boolean, transformation: String
    ): ByteArray? = decryptRSA(hexString2Bytes(data), key, isPublicKey, transformation)

    fun decryptRSA(
        data: ByteArray?, key: ByteArray, isPublicKey: Boolean, transformation: String
    ): ByteArray? = rsaTemplate(data, key, isPublicKey, transformation, false)

    private fun rsaTemplate(
        data: ByteArray?, key: ByteArray?, isPublic: Boolean,
        transformation: String, isEncrypt: Boolean
    ): ByteArray? {
        when {
            data == null || data.isEmpty() || key == null || key.isEmpty() -> return null
            else -> {
                try {
                    KeyFactory.getInstance("RSA").run {
                        when {
                            isPublic -> generatePublic(X509EncodedKeySpec(key))
                            else -> generatePrivate(PKCS8EncodedKeySpec(key))
                        }
                    }?.let {
                        Cipher.getInstance(transformation).apply {
                            init(if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, it)
                        }.run {
                            (if (isEncrypt) 117 else 128).let { maxLength ->
                                (data.size / maxLength).let { count ->
                                    when {
                                        count > 0 -> {
                                            var bytes = ByteArray(0)
                                            var buff = ByteArray(maxLength)
                                            var index = 0
                                            for (i in 0 until count) {
                                                System.arraycopy(data, index, buff, 0, maxLength)
                                                bytes = joins(bytes, doFinal(buff))
                                                index += maxLength
                                            }
                                            if (data.size != index) (data.size - index).let {
                                                buff = ByteArray(it)
                                                System.arraycopy(data, index, buff, 0, it)
                                                bytes = joins(bytes, doFinal(buff))
                                            }
                                            return bytes
                                        }
                                        else -> return doFinal(data)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                } catch (e: NoSuchPaddingException) {
                    e.printStackTrace()
                } catch (e: InvalidKeyException) {
                    e.printStackTrace()
                } catch (e: BadPaddingException) {
                    e.printStackTrace()
                } catch (e: IllegalBlockSizeException) {
                    e.printStackTrace()
                } catch (e: InvalidKeySpecException) {
                    e.printStackTrace()
                }
                return null
            }
        }
    }//android平台RSA默认Provider是“org.bouncycastle.jce.provider.BouncyCastleProvider”

    private fun joins(prefix: ByteArray, suffix: ByteArray): ByteArray =
        ByteArray(prefix.size + suffix.size).apply {
            System.arraycopy(prefix, 0, this, 0, prefix.size)
            System.arraycopy(suffix, 0, this, prefix.size, suffix.size)
        }
}//算法/加密（电子密码本ECB、加密块链CBC、加密反馈CFB、输出反馈OFB）/填充（NoPadding、ZerosPadding、PKCS5Padding）