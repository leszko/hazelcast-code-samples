package com.hazelcast.kubernetes;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Application {

    private static Config config;

    @Bean(name = "sharedConfig")
    public Config hazelcastConfigShared() {
        Config config = new Config();
        config.getNetworkConfig().setPort(5701);
        config.getProperties().setProperty("hazelcast.discovery.enabled", "true");
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        HazelcastKubernetesDiscoveryStrategyFactory discoveryStrategyFactory = new HazelcastKubernetesDiscoveryStrategyFactory();
        Map<String, Comparable> properties = new HashMap<>();
        joinConfig.getDiscoveryConfig()
                  .addDiscoveryStrategyConfig(new DiscoveryStrategyConfig(discoveryStrategyFactory, properties));

        return config;
    }

    @Bean(name = "separateConfig")
    public Config hazelcastConfigSeparate() {
        Config config = new Config();
        config.getNetworkConfig().setPort(5702);
        config.getProperties().setProperty("hazelcast.discovery.enabled", "true");
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        HazelcastKubernetesDiscoveryStrategyFactory discoveryStrategyFactory = new HazelcastKubernetesDiscoveryStrategyFactory();
        Map<String, Comparable> properties = new HashMap<>();
        String serviceName = System.getenv("KUBERNETES_SERVICE_NAME");
        properties.put("service-name", serviceName);
        properties.put("service-port", "5702");
        joinConfig.getDiscoveryConfig()
                  .addDiscoveryStrategyConfig(new DiscoveryStrategyConfig(discoveryStrategyFactory, properties));

        GroupConfig groupConfig = new GroupConfig("separate");
        config.setGroupConfig(groupConfig);

        return config;
    }

    @Bean(name = "shared")
    public HazelcastInstance hazelcastInstanceShared(@Qualifier("sharedConfig") Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean(name = "separate")
    public HazelcastInstance hazelcastInstanceSeparate(@Qualifier("separateConfig") Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
