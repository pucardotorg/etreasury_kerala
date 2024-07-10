package org.egov.eTreasury.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// NOTE: If tracer is disabled change CustomKafkaTemplate to KafkaTemplate in autowiring

@Service
public class Producer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void push(String topic, Object value) {
        kafkaTemplate.send(topic, value);
    }
}