import org.junit.Test
import kotlin.test.assertEquals
import com.adobe.marketing.mobile.internal.eventhub.EventHub

internal class EventHubTests {

    @Test
    fun testVersion() {
        assertEquals(EventHub.version, "2.0.0")
    }
}