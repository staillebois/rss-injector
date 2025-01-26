package com.st;

import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RssKafkaRoute extends RouteBuilder {

    Logger log = Logger.getLogger(RssKafkaRoute.class.getName());

    @ConfigProperty(name = "rss.feeds")
    String rssFeeds;

    @ConfigProperty(name = "kafka.broker")
    String kafkaBroker;

    @ConfigProperty(name = "kafka.topic")
    String kafkaTopic;

    @ConfigProperty(name = "xml.jq.filter")
    String xmlJqFilter;

    @Override
    public void configure() {

        log.info("rssFeeds: " + rssFeeds + " kafkaBroker: " + kafkaBroker + " kafkaTopic: " + kafkaTopic + " xmlJqFilter: " + xmlJqFilter);        

        Arrays.asList(rssFeeds.split(","))
        .forEach(rss -> from("rss:" + rss + "?splitEntries=true&consumer.delay=60000")
                        .marshal().rss()
                        .to("xj:identity?transformDirection=XML2JSON")
                        .transform().jq(xmlJqFilter)
                        .marshal().json()
                        .to("kafka:" + kafkaTopic + "?brokers=" + kafkaBroker));

    }
}