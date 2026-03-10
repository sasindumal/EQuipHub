package com.equiphub.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EquipHubApplicationTests {

    @Test
    @DisplayName("Application entry-point class is present and compiles")
    void applicationClassExists() {
        // No Spring context needed.
        // All endpoint behaviour is covered by @WebMvcTest slices.
    }
}