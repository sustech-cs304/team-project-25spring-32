// app/src/test/java/com/example/pa/TransitionTypeTest.java
package com.example.pa.memory;

import com.example.pa.ui.memory.TransitionType;
import org.junit.Test;
import static org.junit.Assert.*;

public class TransitionTypeTest {

    @Test
    public void testFADETransitionType() {
        assertEquals("fade", TransitionType.FADE.getFfmpegFilterName());
    }

    @Test
    public void testSLIDE_LEFTTransitionType() {
        assertEquals("slideleft", TransitionType.SLIDE_LEFT.getFfmpegFilterName());
    }

    @Test
    public void testSLIDE_RIGHTTransitionType() {
        assertEquals("slideright", TransitionType.SLIDE_RIGHT.getFfmpegFilterName());
    }

    @Test
    public void testSLIDE_UPTransitionType() {
        assertEquals("slideup", TransitionType.SLIDE_UP.getFfmpegFilterName());
    }

    @Test
    public void testSLIDE_DOWNTransitionType() {
        assertEquals("slidedown", TransitionType.SLIDE_DOWN.getFfmpegFilterName());
    }

    @Test
    public void testWIPE_LEFTTransitionType() {
        assertEquals("wipeleft", TransitionType.WIPE_LEFT.getFfmpegFilterName());
    }

    @Test
    public void testWIPE_RIGHTTransitionType() {
        assertEquals("wiperight", TransitionType.WIPE_RIGHT.getFfmpegFilterName());
    }

    @Test
    public void testDistanceTransitionType() {
        assertEquals("distance", TransitionType.Distance.getFfmpegFilterName());
    }

    @Test
    public void testDISSOLVETransitionType() {
        assertEquals("dissolve", TransitionType.DISSOLVE.getFfmpegFilterName());
    }

    @Test
    public void testPIXELIZETTransitionType() {
        assertEquals("pixelize", TransitionType.PIXELIZE.getFfmpegFilterName());
    }

    // 确保所有枚举值都被覆盖
    @Test
    public void testAllTransitionTypesMappedCorrectly() {
        for (TransitionType type : TransitionType.values()) {
            assertNotNull(type.getFfmpegFilterName());
            assertFalse(type.getFfmpegFilterName().isEmpty());
        }
    }
}