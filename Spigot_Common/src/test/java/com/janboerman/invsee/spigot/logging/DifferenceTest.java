package com.janboerman.invsee.spigot.logging;

import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.logging.ItemType;
import org.bukkit.Material;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class DifferenceTest {

    @Test
    public void testAccumulate() {
        Difference difference = new Difference();

        difference.accumulate(new ItemType(Material.EMERALD, null), 10);
        assertEquals(Map.of(new ItemType(Material.EMERALD, null), 10), difference.getDifference());

        difference.accumulate(new ItemType(Material.EMERALD, null), -3);
        assertEquals(Map.of(new ItemType(Material.EMERALD, null), 7), difference.getDifference());
    }

}
