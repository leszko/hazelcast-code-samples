package com.hazelcast.kubernetes;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommandController {

    @Autowired
    @Qualifier("shared")
    HazelcastInstance hazelcastInstanceShared;

    @Autowired
    @Qualifier("separate")
    HazelcastInstance hazelcastInstanceSeparate;

    @RequestMapping("/put")
    public CommandResponse put(@RequestParam(value = "key") String key, @RequestParam(value = "value") String value) {
        IMap<String, String> mapShared = hazelcastInstanceShared.getMap("map");
        IMap<String, String> mapSeparate = hazelcastInstanceShared.getMap("map");
        String oldValueShared = mapShared.put(key, value);
        String oldValueSeparate = mapSeparate.put(key, value);
        return new CommandResponse(oldValueShared, oldValueSeparate);
    }

    @RequestMapping("/get")
    public CommandResponse get(@RequestParam(value = "key") String key) {
        IMap<String, String> mapShared = hazelcastInstanceShared.getMap("map");
        IMap<String, String> mapSeparate = hazelcastInstanceSeparate.getMap("map");
        String valueShared = mapShared.get(key);
        String valueSeparate = mapSeparate.get(key);
        return new CommandResponse(valueShared, valueSeparate);
    }

    @RequestMapping("/remove")
    public CommandResponse remove(@RequestParam(value = "key") String key) {
        IMap<String, String> mapShared = hazelcastInstanceShared.getMap("map");
        IMap<String, String> mapSeparate = hazelcastInstanceSeparate.getMap("map");
        String valueShared = mapShared.remove(key);
        String valueSeparate = mapSeparate.remove(key);
        return new CommandResponse(valueShared, valueSeparate);
    }

    @RequestMapping("/size")
    public CommandResponse size() {
        IMap<String, String> mapShared = hazelcastInstanceShared.getMap("map");
        IMap<String, String> mapSeparate = hazelcastInstanceSeparate.getMap("map");
        int sizeShared = mapShared.size();
        int sizeSeparate = mapSeparate.size();
        return new CommandResponse(Integer.toString(sizeShared), Integer.toString(sizeSeparate));
    }

    @RequestMapping("/populate")
    public CommandResponse populate() {
        IMap<String, String> mapShared = hazelcastInstanceShared.getMap("map");
        IMap<String, String> mapSeparate = hazelcastInstanceSeparate.getMap("map");
        for (int i = 0; i < 1000; i++) {
            String s = Integer.toString(i);
            mapShared.put(s, s);
            mapSeparate.put(s, s);
        }
        return new CommandResponse("1000 entry inserted to the map");
    }

    @RequestMapping("/clear")
    public CommandResponse clear() {
        hazelcastInstanceShared.getMap("map").clear();
        hazelcastInstanceSeparate.getMap("map").clear();
        return new CommandResponse("Map cleared");
    }
}
