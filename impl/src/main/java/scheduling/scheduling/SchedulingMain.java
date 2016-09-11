/*
 * Copyright © 2016 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package scheduling.scheduling;

import java.util.*;
import javax.annotation.Nonnull;

import fast.api.FastSystem;
import fast.api.UserHints;
import scheduling.api.FlowSpec;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulingMain implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulingMain.class);

    private FastSystem fast;

    /* the ID for the first function instance */
    private String fid;

    public void onCreate(@Nonnull FastSystem fast,
                        @Nonnull SchedulingExternalEventTrigger trigger) {
        LOG.info("SchedulingMain Session Initiated");
        this.fast = fast;

        /*
         * This is usually where you submit the first FAST function instance.
         *
         * But you can just get the FastSystem instance and submit function instances later.
         * */
        List<FlowSpec> flowList = new ArrayList<>();
        
        fid = this.fast.submit(new RSAFastFunction(flowList));

        /*
         * You can also bind your application to a certain event trigger which will then submit
         * function instances when appropriate.
         * */
        trigger.bind(this);

        LOG.info("SchedulingMain initialized successfully");
    }

    @Override
    public void close() throws Exception {
        this.fast.delete(fid);

        LOG.info("SchedulingMain Closed");
    }

    public void onPacket(PacketReceived packetIn) {
        /*
         * This is the handler of the triggered event.
         *
         * You can submit function instances here.
         * */
    	
    	List<FlowSpec> flowList = new ArrayList<>();
        RSAFastFunction f = new RSAFastFunction(flowList);

        /* You can set the precedence to make sure the first function instance is always executed first */
        String[] precedences = { fid };
        this.fast.submit(f, new UserHints(precedences));
    }
}
