package com.eferraz.filestore

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
internal actual fun writeCsv(content: String) {
    val documentDirectoryUrl = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val basePath = requireNotNull(documentDirectoryUrl?.path)

    val filePath = "$basePath/$RENDA_FIXA_CSV_FILE_NAME"
    if (NSFileManager.defaultManager.fileExistsAtPath(filePath)) {
        NSFileManager.defaultManager.removeItemAtPath(filePath, error = null)
    }
    val data = (content as NSString).dataUsingEncoding(NSUTF8StringEncoding) as NSData
    data.writeToFile(filePath, atomically = true)
}
