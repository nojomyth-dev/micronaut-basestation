package de.riversroses;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "Micronaut Workshop Server", version = "2.0"))
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
