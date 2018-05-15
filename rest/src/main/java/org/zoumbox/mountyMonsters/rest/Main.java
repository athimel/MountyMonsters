package org.zoumbox.mountyMonsters.rest;

import com.google.gson.Gson;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.immutables.gson.stream.GsonMessageBodyProvider;
import org.immutables.gson.stream.GsonProviderOptionsBuilder;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {

    public static final String BASE_URI = "http://0.0.0.0:8080/mountyMonsters/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("org.zoumbox.mountyMonsters.rest");
        GsonMessageBodyProvider provider = new GsonMessageBodyProvider(
                new GsonProviderOptionsBuilder()
                        .gson(new Gson()) // build custom Gson instance using GsonBuilder methods
                        .addMediaTypes(MediaType.APPLICATION_JSON_TYPE) // specify custom media types
                        .addMediaTypes(MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8"))
                        .build()) {
        };
        rc.register(provider);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        return server;
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }
}

