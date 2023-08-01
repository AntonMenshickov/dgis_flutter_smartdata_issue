import io.flutter.plugin.common.EventChannel

class TrafficScoreHandler:  EventChannel.StreamHandler {
    private var eventSink: EventChannel.EventSink? = null

    fun sink(): EventChannel.EventSink? {
        return eventSink;
    } ;
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.eventSink = events;
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null;
    }

}