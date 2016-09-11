
package org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.impl.rev160902;

import fast.api.FastSystem;

import scheduling.scheduling.SchedulingExternalEventTrigger;
import scheduling.scheduling.SchedulingMain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulingModule extends org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.impl.rev160902.AbstractSchedulingModule {
    private static final Logger LOG = LoggerFactory.getLogger(SchedulingMain.class);

    public SchedulingModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public SchedulingModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.fast.app.scheduling.impl.rev160902.SchedulingModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        FastSystem fast = getFastSystemDependency();
        if (fast == null) {
            LOG.error("Error loading SchedulingMain: No FAST system found.");
            return null;
        }

        final SchedulingExternalEventTrigger trigger = new SchedulingExternalEventTrigger();
        try {
            getBrokerDependency().registerProvider(trigger);
        } catch (Exception e) {
            LOG.error("Error loading SchedulingMain: Cannot register external event trigger");
            return null;
        }

        final SchedulingMain main = new SchedulingMain();
        main.onCreate(fast, trigger);

        return new AutoCloseable() {
            @Override
            public void close() {
                try {
                    trigger.close();
                } catch (Exception e) {
                }
                try {
                    main.close();
                } catch (Exception e) {
                }
            }
        };
    }

}
