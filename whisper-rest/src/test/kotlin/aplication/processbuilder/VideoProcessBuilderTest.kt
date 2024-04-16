package aplication.processbuilder

import application.processbuilder.VideoProcessBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VideoProcessBuilderTest {

    @Test
    fun `should download a video`() {
        val video = "https://www.youtube.com/shorts/XU0a_XXCT14"
        val token = "test"

        val result = VideoProcessBuilder().runYtDlp(link = video, token = token)
        assertNotNull(result)
    }
}