package com.example.pa;

import android.net.Uri;

import com.example.pa.ui.memory.TransitionType;
import com.example.pa.ui.memory.VideoCreationOptions;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class VideoCreationOptionsTest {

    private List<Uri> mockImageUris;
    private String testOutputFilePath;

    @Before
    public void setUp() {
        mockImageUris = new ArrayList<>(Arrays.asList(mock(Uri.class), mock(Uri.class)));
        testOutputFilePath = "/test/output/path/video.mp4";
    }

    // --- Builder Constructor Tests ---

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenImageUrisIsNull() {
        new VideoCreationOptions.Builder(null, testOutputFilePath).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenImageUrisIsEmpty() {
        new VideoCreationOptions.Builder(new ArrayList<>(), testOutputFilePath).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenOutputFilePathIsNull() {
        new VideoCreationOptions.Builder(mockImageUris, null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenOutputFilePathIsEmpty() {
        new VideoCreationOptions.Builder(mockImageUris, "").build();
    }

    @Test
    public void builder_shouldSetRequiredFieldsAndDefaultOptionalFields() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath).build();

        assertEquals(mockImageUris, options.getImageUris());
        assertEquals(testOutputFilePath, options.getOutputFilePath());

        // Check default values
        assertNull(options.getMusicUri());
        assertEquals(1.0f, options.getMusicVolume(), 0.001f);
        assertEquals(TransitionType.FADE, options.getTransitionType());
        assertEquals(3000, options.getImageDisplayDurationMs());
        assertEquals(1000, options.getTransitionDurationMs());
        assertEquals("1280x720", options.getVideoResolution());
        assertEquals(4000 * 1000, options.getVideoBitrate());
        assertEquals(128 * 1000, options.getAudioBitrate());
        assertEquals(25, options.getFrameRate());
    }

    // --- Setter Tests ---

    @Test
    public void builder_shouldSetMusicUri() {
        Uri mockMusicUri = mock(Uri.class);
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setMusicUri(mockMusicUri)
                .build();
        assertEquals(mockMusicUri, options.getMusicUri());
    }

    @Test
    public void builder_shouldSetMusicVolume() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setMusicVolume(0.75f)
                .build();
        assertEquals(0.75f, options.getMusicVolume(), 0.001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenMusicVolumeIsTooLow() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setMusicVolume(-0.1f)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenMusicVolumeIsTooHigh() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setMusicVolume(1.1f)
                .build();
    }

    @Test
    public void builder_shouldSetTransitionType() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setTransitionType(TransitionType.SLIDE_RIGHT)
                .build();
        assertEquals(TransitionType.SLIDE_RIGHT, options.getTransitionType());
    }

    @Test
    public void builder_shouldSetImageDisplayDurationMs() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setImageDisplayDurationMs(5000)
                .build();
        assertEquals(5000, options.getImageDisplayDurationMs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenImageDisplayDurationMsIsZero() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setImageDisplayDurationMs(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenImageDisplayDurationMsIsNegative() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setImageDisplayDurationMs(-100)
                .build();
    }

    @Test
    public void builder_shouldSetTransitionDurationMs() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setTransitionDurationMs(1500)
                .build();
        assertEquals(1500, options.getTransitionDurationMs());
    }

    @Test
    public void builder_shouldAllowZeroTransitionDurationMs() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setTransitionDurationMs(0)
                .build();
        assertEquals(0, options.getTransitionDurationMs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenTransitionDurationMsIsNegative() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setTransitionDurationMs(-100)
                .build();
    }

    @Test
    public void builder_shouldSetVideoResolution() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setVideoResolution("1920x1080")
                .build();
        assertEquals("1920x1080", options.getVideoResolution());
    }

    @Test
    public void builder_shouldSetVideoBitrate() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setVideoBitrate(8000 * 1000)
                .build();
        assertEquals(8000 * 1000, options.getVideoBitrate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenVideoBitrateIsZero() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setVideoBitrate(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenVideoBitrateIsNegative() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setVideoBitrate(-1000)
                .build();
    }

    @Test
    public void builder_shouldSetAudioBitrate() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setAudioBitrate(256 * 1000)
                .build();
        assertEquals(256 * 1000, options.getAudioBitrate());
    }

    @Test
    public void builder_shouldAllowZeroAudioBitrate() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setAudioBitrate(0)
                .build();
        assertEquals(0, options.getAudioBitrate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenAudioBitrateIsNegative() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setAudioBitrate(-100)
                .build();
    }

    @Test
    public void builder_shouldSetFrameRate() {
        VideoCreationOptions options = new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setFrameRate(30)
                .build();
        assertEquals(30, options.getFrameRate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenFrameRateIsZero() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setFrameRate(0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_shouldThrowException_whenFrameRateIsNegative() {
        new VideoCreationOptions.Builder(mockImageUris, testOutputFilePath)
                .setFrameRate(-10)
                .build();
    }

    @Test
    public void builder_shouldCreateOptionsWithAllCustomValues() {
        Uri customMusicUri = mock(Uri.class);
        List<Uri> customImageUris = Arrays.asList(mock(Uri.class));
        String customOutputFilePath = "/custom/path/video.mp4";

        VideoCreationOptions options = new VideoCreationOptions.Builder(customImageUris, customOutputFilePath)
                .setMusicUri(customMusicUri)
                .setMusicVolume(0.8f)
                .setTransitionType(TransitionType.WIPE_LEFT)
                .setImageDisplayDurationMs(4000)
                .setTransitionDurationMs(800)
                .setVideoResolution("1024x768")
                .setVideoBitrate(6000 * 1000)
                .setAudioBitrate(160 * 1000)
                .setFrameRate(20)
                .build();

        assertEquals(customImageUris, options.getImageUris());
        assertEquals(customOutputFilePath, options.getOutputFilePath());
        assertEquals(customMusicUri, options.getMusicUri());
        assertEquals(0.8f, options.getMusicVolume(), 0.001f);
        assertEquals(TransitionType.WIPE_LEFT, options.getTransitionType());
        assertEquals(4000, options.getImageDisplayDurationMs());
        assertEquals(800, options.getTransitionDurationMs());
        assertEquals("1024x768", options.getVideoResolution());
        assertEquals(6000 * 1000, options.getVideoBitrate());
        assertEquals(160 * 1000, options.getAudioBitrate());
        assertEquals(20, options.getFrameRate());
    }
}