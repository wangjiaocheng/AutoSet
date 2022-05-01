package top.autoget.autokit

import java.io.IOException
import java.io.InputStream
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import javax.net.ssl.*

object SslKit {
    fun getSslSocketFactory(vararg certificates: InputStream): SSLSocketFactory? =
        getSslSocketFactory(null, *certificates)

    fun getSslSocketFactory(
        keyManagers: Array<KeyManager>?, vararg certificates: InputStream
    ): SSLSocketFactory? = try {
        KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null) }.let { keyStore ->
            CertificateFactory.getInstance("X.509").run {
                for ((index, certificate) in certificates.withIndex()) {
                    certificate.use {
                        keyStore.setCertificateEntry(index.toString(), generateCertificate(it))
                    }
                }
            }
            SSLContext.getInstance("TLS").run {
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                    .apply { init(keyStore) }
                    .let { init(keyManagers, it.trustManagers, SecureRandom()) }
                socketFactory
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun getKeyManagers(bks: InputStream, keystorePass: String): Array<KeyManager>? {
        try {
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                init(KeyStore.getInstance("BKS").apply {
                    bks.use { load(it, keystorePass.toCharArray()) }
                }, keystorePass.toCharArray())
            }.keyManagers
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: UnrecoverableKeyException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}//“keytool -printcert -rfc -file srca.cer”证书转字符串，再转输入流(okhttp“Buffer().writeUtf8(string).inputStream”)