package com.example.variantajersey.config;

import com.example.variantajersey.resource.CategoryResource;
import com.example.variantajersey.resource.ItemResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(CategoryResource.class);
        register(ItemResource.class);
    }
}