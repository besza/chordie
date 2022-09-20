package com.besza;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/chordie")
public class ChordieResource {

    @Inject
    Chordie chordie;

    @GET
    @Produces("application/pdf")
    public Response chord(@NotBlank @QueryParam("name") String name,
                          @Pattern(regexp = "[0-9Xx]{6}") @QueryParam("q") String shorthandNotation,
                          @QueryParam("p") Integer position,
                          @QueryParam("labels") String labels) {
        return Response.ok()
                .header("Content-Disposition", "inline")
                .entity(chordie.transform(name, shorthandNotation, position, labels))
                .build();
    }
}