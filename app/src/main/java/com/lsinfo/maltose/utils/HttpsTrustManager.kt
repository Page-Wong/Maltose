package com.lsinfo.maltose.utils

import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Created by G on 2018-04-02.
 */
class HttpsTrustManager : X509TrustManager {

    @Throws(java.security.cert.CertificateException::class)
    override fun checkClientTrusted(
            x509Certificates: Array<java.security.cert.X509Certificate>, s: String) {

    }

    @Throws(java.security.cert.CertificateException::class)
    override fun checkServerTrusted(
            x509Certificates: Array<java.security.cert.X509Certificate>, s: String) {

    }

    fun isClientTrusted(chain: Array<X509Certificate>): Boolean {
        return true
    }

    fun isServerTrusted(chain: Array<X509Certificate>): Boolean {
        return true
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return _AcceptedIssuers
    }

    companion object {

        private var trustManagers: Array<TrustManager>? = null
        private val _AcceptedIssuers = arrayOf<X509Certificate>()

        fun allowAllSSL() {
            HttpsURLConnection.setDefaultHostnameVerifier { arg0, arg1 -> true }

            var context: SSLContext? = null
            if (trustManagers == null) {
                trustManagers = arrayOf(HttpsTrustManager())
            }

            try {
                context = SSLContext.getInstance("TLS")
                context!!.init(null, trustManagers, SecureRandom())
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: KeyManagementException) {
                e.printStackTrace()
            }

            HttpsURLConnection.setDefaultSSLSocketFactory(context!!
                    .socketFactory)
        }
    }

}