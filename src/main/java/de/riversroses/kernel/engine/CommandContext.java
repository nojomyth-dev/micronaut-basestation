package de.riversroses.kernel.engine;

import de.riversroses.infra.error.DomainException;
import de.riversroses.infra.error.ErrorCode;
import de.riversroses.ship.db.ShipRepository;
import de.riversroses.ship.model.Ship;
import de.riversroses.team.db.TeamRepository;
import de.riversroses.team.model.Team;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.ore.OreRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CommandContext {
  public final ShipRepository shipRepo;
  public final TeamRepository teamRepo;
  public final WorldRepository worldRepo;
  public final GameProperties props;
  public final OreRegistry oreRegistry;

  public Ship requireShipByToken(String token) {
    return shipRepo.findByToken(token)
        .orElseThrow(() -> new DomainException(ErrorCode.UNAUTHORIZED, "Unknown Token"));
  }

  public Team requireTeam(String teamId) {
    return teamRepo.findById(teamId)
        .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "Unknown team: " + teamId));
  }
}
