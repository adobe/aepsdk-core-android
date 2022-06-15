import com.adobe.marketing.mobile.internal.eventhub.EventHub
import kotlin.test.assertEquals
import org.junit.Test

internal class EventHubTests {

    @Test
    fun testVersion() {
        assertEquals(EventHub.version, "2.0.0")
    }
}
