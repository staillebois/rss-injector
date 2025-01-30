package com.st;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ApplicationScoped
public class AvroProcessor implements Processor{

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LINK = "link";
    private static final String PUB_DATE = "pubDate";
    private static final String CATEGORY = "category";

    @Override
    public void process(Exchange exchange) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.readValue(exchange.getIn().getBody(String.class), ObjectNode.class);
        GenericRecord record = new GenericData.Record(Rss.SCHEMA$);
        record.put(TITLE, jsonNode.get(TITLE).asText());
        record.put(DESCRIPTION, jsonNode.get(DESCRIPTION).asText());
        record.put(LINK, jsonNode.get(LINK).asText());
        record.put(PUB_DATE, jsonNode.get(PUB_DATE).asText());
        record.put(CATEGORY, jsonNode.get(CATEGORY).asText());
        exchange.getIn().setBody(record);
    }
    
}
