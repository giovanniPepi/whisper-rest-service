import java.io.File

internal class WhisperProcessBuilder {
    fun runWhisper(
        inputFile: File,
        language: String = "Portuguese",
        outputFormat: String = "txt",
    ): Int {
        val result = ProcessBuilder(
            "whisper", "$inputFile", "--output_format", outputFormat, "--output_dir", "/home/a/Downloads",
            "--language", language
        )
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()

        return result
    }
}