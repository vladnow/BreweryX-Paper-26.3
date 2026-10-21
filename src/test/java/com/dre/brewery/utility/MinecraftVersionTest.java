package com.dre.brewery.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MinecraftVersionTest {
    @Test void recognizesTargetWithoutPatch() {
        assertEquals(MinecraftVersion.V26_3, MinecraftVersion.get("26", "3", null));
    }
    @Test void recognizesTargetPatchRelease() {
        assertEquals(MinecraftVersion.V26_3, MinecraftVersion.get("26", "3", "1"));
    }
    @Test void recognizesPreviousCalendarRelease() {
        assertEquals(MinecraftVersion.V26_2, MinecraftVersion.get("26", "2", null));
    }
    @Test void preservesExactLegacyPatchMatch() {
        assertEquals(MinecraftVersion.V1_21_4, MinecraftVersion.get("1", "21", "4"));
    }
    @Test void preservesLegacyAlias() {
        assertEquals(MinecraftVersion.V1_21_10, MinecraftVersion.get("1", "21", "9"));
    }
    @Test void targetEnablesModernItemModelBranch() {
        assertTrue(MinecraftVersion.get("26", "3", null).isOrLater(MinecraftVersion.V1_21_4));
        assertTrue(MinecraftVersion.get("26", "3", null).isOrLater(MinecraftVersion.V1_21_10));
    }
    @Test void calendarVersionsStayOrdered() {
        assertTrue(MinecraftVersion.V26_3.isOrLater(MinecraftVersion.V26_2));
        assertFalse(MinecraftVersion.V26_2.isOrLater(MinecraftVersion.V26_3));
    }
    @Test void doesNotClaimUnknownFutureRelease() {
        assertEquals(MinecraftVersion.UNKNOWN, MinecraftVersion.get("26", "4", null));
    }
}
