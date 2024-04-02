import java.io.File
import java.io.IOError
import java.io.IOException

internal class WhisperProcessBuilder {
    fun runWhisper(
        inputFile: File = File("~/whisperInput/t.mp4"),
        language: String = "Portuguese",
        outputDir: File,
        outputFormat: String = "txt",
    ): Int {

        val result = ProcessBuilder(
            "whisper", "$inputFile", "--output_format", outputFormat, "--output_dir", "$outputDir",
            "--language", language
        )
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()

        return result
    }
}