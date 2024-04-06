import io.kotest.matchers.shouldBe
import whisper_consumer_executor.WhisperProcessBuilder
import java.io.File
import kotlin.test.Test


class WhisperProcessBuilderTest {
    private val inputFile = File("/home/a/Downloads/t.mp4")

    @Test
    fun `Given a command, execute whisper and return the result`() {
        val result = WhisperProcessBuilder().runWhisper(
            inputFile = inputFile,
            outputDir = File("/home/a/Downloads")
        )

        result shouldBe 0
    }
}
