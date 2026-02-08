package de.riversroses.world.rest;

import java.util.Optional;
import de.riversroses.infra.error.DomainException;
import de.riversroses.infra.error.ErrorCode;
import de.riversroses.kernel.engine.CommandBus;
import de.riversroses.ship.model.Ship;
import de.riversroses.world.dto.RadarScanResponse;
import de.riversroses.world.business.ScannerService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.QueryValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Controller("/scan")
@AllArgsConstructor
@Data
public class RadarController {
  private final CommandBus commandBus;
  private final ScannerService scannerService;

  @Get
  public RadarScanResponse scan(
      @Header("X-Token") String token,
      @QueryValue Optional<String> shipId) {

    ScannerService.ScanResult result = commandBus.submitAndWait(
        "scan",
        ctx -> {
          Ship ship;

          if (shipId.isPresent()) {
            ship = ctx.shipRepo.findById(shipId.get())
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "Ship not found"));

            if (!ship.getToken().equals(token)) {
              throw new DomainException(ErrorCode.FORBIDDEN, "You do not own this ship");
            }
          } else {
            ship = ctx.requireShipByToken(token);
          }

          return scannerService.performScan(ship, 300.0);
        },
        1500);

    return RadarScanResponse.builder()
        .ships(result.ships().stream()
            .map(s -> RadarScanResponse.FoundShip.builder()
                .shipId(s.getShipId())
                .teamName(s.getTeamName())
                .x(s.getPosition().x())
                .y(s.getPosition().y())
                .speed(s.getSpeed())
                .heading(s.getHeadingDeg())
                .build())
            .toList())
        .resources(result.resources().stream()
            .map(r -> RadarScanResponse.FoundResource.builder()
                .id(r.id())
                .oreId(r.oreId())
                .value(r.value())
                .x(r.position().x())
                .y(r.position().y())
                .build())
            .toList())
        .build();
  }
}
