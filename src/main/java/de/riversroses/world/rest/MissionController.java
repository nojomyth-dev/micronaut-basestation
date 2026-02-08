package de.riversroses.world.rest;

import java.util.List;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.dto.MissionDto;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.AllArgsConstructor;
import lombok.Data;

@Controller("/missions")
@AllArgsConstructor
@Data
public class MissionController {
  private final WorldRepository worldRepo;

  @Get
  public List<MissionDto> list() {
    return worldRepo.getMissions().stream()
        .map(m -> new MissionDto(
            m.id(),
            m.description(),
            m.target().x(),
            m.target().y(),
            m.reward()))
        .toList();
  }
}
