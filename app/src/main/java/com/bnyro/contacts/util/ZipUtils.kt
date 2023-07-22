package com.bnyro.contacts.util

import net.lingala.zip4j.io.inputstream.ZipInputStream
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.InputStream
import java.io.OutputStream


object ZipUtils {
    fun writeToEncryptedZip(password: String, bytesToExport: ByteArray?, outputStream: OutputStream) {
        val zipParameters = ZipParameters()
        zipParameters.isEncryptFiles = true
        zipParameters.fileNameInZip = "contacts.vcf"
        zipParameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD
        val zipOutputStream = ZipOutputStream(outputStream, password.toCharArray())
        zipOutputStream.putNextEntry(zipParameters)
        zipOutputStream.write(bytesToExport)
        zipOutputStream.closeEntry()
    }

    fun getPlainInputStream(password: String, inputStream: InputStream?): InputStream {
        val zipInputStream = ZipInputStream(inputStream, password.toCharArray())
        zipInputStream.nextEntry
        return zipInputStream
    }
}
