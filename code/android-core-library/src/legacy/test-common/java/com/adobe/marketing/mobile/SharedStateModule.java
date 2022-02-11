package com.adobe.marketing.mobile;

/**
 * Created by jgeng on 5/9/17.
 */
public class SharedStateModule extends InternalModule {
	public SharedStateModule(final EventHub hub, final PlatformServices services, String sharedStateName) {
		super(sharedStateName, hub, services);
		this.registerListener(EventType.CUSTOM, EventSource.NONE, CreateStateListener.class);
	}

	public SharedStateModule(final EventHub hub, final PlatformServices services) {
		super("default", hub, services);
		this.registerListener(EventType.CUSTOM, EventSource.NONE, CreateStateListener.class);
	}

	public void setSharedStateName(String stateName) {
		super.setModuleName(stateName);
	}

	public static class CreateStateListener extends ModuleEventListener<SharedStateModule> {
		public CreateStateListener(final SharedStateModule module, final EventType type, final EventSource source) {
			super(module, type, source);
		}

		public void hear(final Event e) {
			// create the initial null state so we can update it after background task completes.
			final int createdVersion = e.getEventNumber();
			parentModule.createSharedState(createdVersion, null);
			// fire background task
			new Thread(new AsyncBackgroundTask(createdVersion, parentModule)).run();
		}
	}

	// emulates a network request that responds in 1 second
	private static class AsyncBackgroundTask implements Runnable {
		private final int updateVersion;
		private final SharedStateModule module;
		AsyncBackgroundTask(final int updateVersion, final SharedStateModule module) {
			this.updateVersion = updateVersion;
			this.module = module;
		}

		public void run() {
			//            this.module.updateSharedState(updateVersion, new EventData().putString("state",
			//                    "updated" + updateCount.incrementAndGet()));
		}
	}
}
