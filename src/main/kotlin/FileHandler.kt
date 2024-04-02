import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.util.logging.Logger

internal class FileHandler {
    private val inputDir = File("${System.getProperty("user.home")}/whisperInput")
    private val outputDir = File("${System.getProperty("user.home")}/whisperResult")

    private val logger = Logger.getLogger("WhisperLogger")

    init {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        if (!inputDir.exists()) {
            inputDir.mkdirs()
        }
    }

    fun uploadFileToInputDir(file: File): Boolean {
        try {
            Files.copy(file.toPath(), inputDir.toPath().resolve(file.name))
            return true
        } catch (e: IOException) {
            logger.severe("An I/O error occurred trying to upload file: ${e.message}")
            return false
        }
    }

    fun fetchFileResultFromToken(token: String): File? {
        try {
            return inputDir.listFiles { file ->
                file.isFile && file.name.contains(token, ignoreCase = true)
            }?.first()
        } catch (e: IOException) {
            logger.severe("An I/O error occurred trying to fetch results from token: ${e.message}")
        } catch (e: FileNotFoundException) {
            logger.severe("File not found trying to fetch results from token: ${e.message}")
        }
        return null
    }
}