package com.example.ecoruta;

import com.example.ecoruta.models.EcoCategory;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ExampleUnitTest {
    @Test
    public void categoriaConservaSusDatos() {
        EcoCategory categoria = new EcoCategory(
                "cat1",
                "Reciclar",
                "Dale una nueva vida a los materiales.",
                "reciclar"
        );

        assertEquals("cat1", categoria.getId());
        assertEquals("Reciclar", categoria.getNombre());
        assertEquals("Dale una nueva vida a los materiales.", categoria.getDescripcion());
        assertEquals("reciclar", categoria.getIcono());
    }
}