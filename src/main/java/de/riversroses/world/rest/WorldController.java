package de.riversroses.world.rest;

import de.riversroses.world.business.WorldSnapshotService;
import de.riversroses.world.dto.WorldSnapshotResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.AllArgsConstructor;
import lombok.Data;

@Controller("/world")
@AllArgsConstructor
@Data
public class WorldController {

    private final WorldSnapshotService snapshotService;

    @Get("/snapshot")
    public WorldSnapshotResponse snapshot() {
        return snapshotService.snapshot();
    }
}
