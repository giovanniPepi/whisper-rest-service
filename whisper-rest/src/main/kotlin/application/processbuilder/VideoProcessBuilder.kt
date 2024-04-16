package application.processbuilder

import DOWNLOAD_DIR
import WhisperUtils
import WhisperUtils.Companion.logger
import java.nio.file.Path

internal class VideoProcessBuilder {
    fun runYtDlp(
        link: String,
        token: String,
        outputDir: Path? = (WhisperUtils().getPath(DOWNLOAD_DIR)),
    ): Int {
        WhisperUtils().deleteAllDownloads()

        val result = ProcessBuilder(
            "yt-dlp", "--extract-audio", "-o", "$outputDir/$token", link
        )
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()

        logger.info { "YT-DLP: $result" }

        WhisperUtils().moveDownloadedAudioToUploadDir(token)
        return result
    }
}