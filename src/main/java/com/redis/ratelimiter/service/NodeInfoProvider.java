package com.redis.ratelimiter.service;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;

@Component
public class NodeInfoProvider {

    public String currentNodeId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "|" + ManagementFactory.getRuntimeMXBean().getName();
        } catch (Exception ex) {
            return "unknown-node";
        }
    }
}
