package application.util

import whisper.application.RESULT_DIR
import application.RestHandler
import java.io.IOException
import java.nio.file.*
import java.util.logging.Logger


internal val logger: Logger = Logger.getLogger(RestHandler::class.java.name)
internal fun createPath(pathString: String): Path? {
    return try {
        Paths.get(pathString)
    } catch (e: InvalidPathException) {
        logger.severe("Error getting path for $pathString: $e")
        null
    } catch (e: IllegalArgumentException) {
        logger.severe("Invalid combination of options specified for $pathString: $e")
        null
    } catch (e: SecurityException) {
        logger.severe("Security exception occurred while getting path for $pathString: $e")
        null
    } catch (e: FileSystemNotFoundException) {
        logger.severe("FileSystem exception occurred while getting path for $pathString: $e")
        null
    }
}

internal fun getPath(pathString: String): Path? {
    return try {
        Paths.get(pathString)
    } catch (e: InvalidPathException) {
        logger.severe("Error getting path for $pathString: $e")
        null
    } catch (e: IllegalArgumentException) {
        logger.severe("Invalid combination of options specified for $pathString: $e")
        null
    } catch (e: SecurityException) {
        logger.severe("Security exception occurred while getting path for $pathString: $e")
        null
    } catch (e: FileSystemNotFoundException) {
        logger.severe("FileSystem exception occurred while getting path for $pathString: $e")
        null
    } catch (e: FileAlreadyExistsException) {
        logger.severe("File already exists at: $pathString")
        null
    } catch (e: IOException) {
        logger.severe("Error creating file: ${e.message}")
        null
    }
}

internal fun getResult(token: String): Path? {
    val directoryPath = Paths.get("$RESULT_DIR/$token")

    try {
        Files.newDirectoryStream(directoryPath).use { stream ->
            for (filePath in stream) {
                if (Files.isRegularFile(filePath)) {
                    val fileName = filePath.fileName.toString()
                    val baseName = fileName.substringBeforeLast(".", fileName)
                    if (baseName == token) {
                        return filePath
                    }
                }
            }
        }
    } catch (e: InvalidPathException) {
        logger.severe("Error getting path for $RESULT_DIR/$token: $e")
        return null
    } catch (e: IllegalArgumentException) {
        logger.severe("Invalid combination of options specified for $RESULT_DIR/$token: $e")
        return null
    } catch (e: SecurityException) {
        logger.severe("Security exception occurred while getting path for $RESULT_DIR/$token: $e")
        return null
    } catch (e: FileSystemNotFoundException) {
        logger.severe("FileSystem exception occurred while getting path for $RESULT_DIR/$token: $e")
        return null
    } catch (e: FileAlreadyExistsException) {
        logger.severe("File already exists at: $RESULT_DIR/$token: $e")
        return null
    } catch (e: IOException) {
        logger.severe("Error creating file $RESULT_DIR/$token:: $e")
        return null
    }
    return null
}
