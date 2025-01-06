package com.zencode.book.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "zenFikry",
                        email = "zenFikry@gmail.com",
                        url = "https://my-url.com"
                ),
                description = "OpenApi documentation for Book Social Network app",
                title = "OpenApi specification",
                version = "1.0",
                license = @License(
                        name = "License Name",
                        url = "https://license-url.com"
                ),
                termsOfService = "Term of services"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8088"
                ),
                @Server(
                        description = "Prod ENV",
                        url = "https://some-url.com"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Auth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
