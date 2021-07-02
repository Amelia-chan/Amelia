package pw.mihou.amelia.clients.listeners.interfaces;

public interface Listener {

    default Listener getInstance() {
        return this;
    }

    String type();

}
