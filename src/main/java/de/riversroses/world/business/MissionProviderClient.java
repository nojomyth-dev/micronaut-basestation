package de.riversroses.world.business;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

@Client(id = "mission-provider")
public interface MissionProviderClient {
    @Get(uri = "{+path}", produces = MediaType.APPLICATION_JSON)
    MissionService.MissionPayload fetchMission(String path);

    @Post(uri = "{+path}", consumes = MediaType.APPLICATION_JSON)
    void sendCompletion(String path, @Body MissionService.MissionCompletionBody body);
}
