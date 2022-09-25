package com.besza;

import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Path("/chordie")
public class ChordieResource {

    @Inject
    Chordie chordie;

    @GET
    @Produces("image/svg+xml")
    public CompletionStage<RestResponse<byte[]>> chord(
            @NotBlank @QueryParam("name") String name,
            @Pattern(regexp = "[0-9Xx]{6}") @QueryParam("q") String notation,
            @QueryParam("labels") String labels
    ) {
        return CompletableFuture.supplyAsync(() -> chordie.transform(name, notation, labels))
                .thenApply(bytes -> RestResponse.ResponseBuilder
                        .ok(bytes)
                        .header("X-Content-Type-Options", "nosniff")
                        .build())
                .whenCompleteAsync((response, ex) -> chordie.cleanUp());
    }
}