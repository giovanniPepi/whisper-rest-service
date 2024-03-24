import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.test.Test


class WhisperProcessBuilderTest {
    private val inputFile = File("/home/a/Downloads/t.mp4")
    @Test
    fun `Given a command, execute whisper and return the result`() {
        val result = WhisperProcessBuilder().runWhisper(inputFile = inputFile)

        result shouldBe 0
    }
}