package com.st ;

import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RssKafkaRoute extends RouteBuilder {

    Logger log = Logger.getLogger(RssKafkaRoute.class.getName());

    @Inject
    AvroProcessor avroProcessor;
    
    @ConfigProperty(name = "rss.feeds")
    String rssFeeds;

    @ConfigProperty(name = "xml.jq.filter")
    String xmlJqFilter;

    @Override
    public void configure() {
        Arrays.asList(rssFeeds.split(","))
        .forEach(rss -> from("rss:" + rss + "?splitEntries=true&consumer.delay=60000")
                        .marshal().rss()
                        .to("xj:identity?transformDirection=XML2JSON")
                        .transform().jq(xmlJqFilter)
                        .process(avroProcessor)
                        // .log("Processing ${body.get('title')}")
                        .idempotentConsumer((simple("${body.get('title')}")), MemoryIdempotentRepository.memoryIdempotentRepository(200))
                        .to("kafka:{{kafka.topic}}?brokers={{kafka.broker}}&schemaRegistryURL={{schema.registry.url}}&valueSerializer={{kafka.value.serializer}}"));

    }
}