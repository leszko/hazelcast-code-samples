package com.hazelcast.ocp.configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
@Profile("k8s")
public class KubernetesConfig {

    @Bean
    @Scope(value = "prototype")
    public HazelcastInstance hazelcastInstance() {
        return HazelcastClient.newHazelcastClient();
    }
}
