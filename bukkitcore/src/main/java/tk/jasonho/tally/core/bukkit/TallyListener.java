package tk.jasonho.tally.core.bukkit;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * The class every event listener that uses Tally should extend
 */
public abstract class TallyListener implements Listener {

    public final static List<TallyListener> HOOKS = new ArrayList<>();

    protected final TallyOperationHandler operationHandler;
    protected TallyListener(TallyOperationHandler operationHandler) {
        this.operationHandler = operationHandler;
        HOOKS.add(this);
    }

}
