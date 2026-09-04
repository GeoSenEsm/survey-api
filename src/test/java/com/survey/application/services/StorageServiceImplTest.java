package com.survey.application.services;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageServiceImplTest {

    @Test
    void toUrlPath_normalizesWindowsSeparators() {
        assertThat(StorageServiceImpl.toUrlPath(Paths.get("\\uploads\\survey_settings\\logo.png")))
                .isEqualTo("/uploads/survey_settings/logo.png");
    }

    @Test
    void validateFileType_rejectsUnsupportedLogoExtension() {
        FileValidationServiceImpl validationService = new FileValidationServiceImpl();

        assertThatThrownBy(() -> validationService.validateFileType("logo.gif"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void requireDecodableLogo_rejectsNonImageBytesWithAnImageExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", new byte[] {1, 2, 3, 4});

        assertThatThrownBy(() -> StorageServiceImpl.requireDecodableLogo(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a readable");
    }

    @Test
    void requireDecodableLogo_rejectsOversizedUploads() {
        byte[] oversized = new byte[(int) (5L * 1024 * 1024 + 1)];
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", oversized);

        assertThatThrownBy(() -> StorageServiceImpl.requireDecodableLogo(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too large");
    }

    @Test
    void requireDecodableLogo_rejectsDimensionsOverTheLimitWithoutDecodingPixelData() throws Exception {
        // Cheap to allocate (one side is 10px) but still exercises the header-only dimension
        // check ahead of the pixel decode — a real oversized-both-sides raster would be
        // gigabytes and isn't needed to prove the check runs before reader.read(0).
        BufferedImage oversized = new BufferedImage(8300, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream pngBytes = new ByteArrayOutputStream();
        ImageIO.write(oversized, "png", pngBytes);
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", pngBytes.toByteArray());

        assertThatThrownBy(() -> StorageServiceImpl.requireDecodableLogo(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dimensions must be at most");
    }

    @Test
    void requireDecodableLogo_acceptsAValidPng() throws Exception {
        BufferedImage source = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream pngBytes = new ByteArrayOutputStream();
        ImageIO.write(source, "png", pngBytes);
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", pngBytes.toByteArray());

        BufferedImage decoded = StorageServiceImpl.requireDecodableLogo(file);

        assertThat(decoded.getWidth()).isEqualTo(64);
        assertThat(decoded.getHeight()).isEqualTo(32);
    }

    @Test
    void imageIoFormatName_mapsJpgExtensionToTheJpegFormatName() {
        assertThat(StorageServiceImpl.imageIoFormatName(Paths.get("logo.jpg"))).isEqualTo("jpeg");
        assertThat(StorageServiceImpl.imageIoFormatName(Paths.get("logo.jpeg"))).isEqualTo("jpeg");
        assertThat(StorageServiceImpl.imageIoFormatName(Paths.get("logo.png"))).isEqualTo("png");
    }

    @Test
    void boundedDimensions_downscalesAnOversizedImageWhileKeepingAspectRatio() {
        BufferedImage oversized = new BufferedImage(1600, 800, BufferedImage.TYPE_INT_RGB);

        Dimension bounded = StorageServiceImpl.boundedDimensions(oversized, 512);

        assertThat(bounded.width).isEqualTo(512);
        assertThat(bounded.height).isEqualTo(256);
    }

    @Test
    void boundedDimensions_leavesAnAlreadySmallImageUnchanged() {
        BufferedImage small = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);

        Dimension bounded = StorageServiceImpl.boundedDimensions(small, 512);

        assertThat(bounded.width).isEqualTo(200);
        assertThat(bounded.height).isEqualTo(100);
    }

    @Test
    void render_dropsAlphaOntoAWhiteBackgroundWhenTheTargetFormatDoesNotSupportTransparency() {
        BufferedImage transparentSource = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);

        BufferedImage jpegOutput = StorageServiceImpl.render(transparentSource, new Dimension(2, 2), false);
        BufferedImage pngOutput = StorageServiceImpl.render(transparentSource, new Dimension(2, 2), true);

        assertThat(jpegOutput.getColorModel().hasAlpha()).isFalse();
        assertThat(jpegOutput.getWidth()).isEqualTo(2);
        assertThat(jpegOutput.getHeight()).isEqualTo(2);
        assertThat(pngOutput.getColorModel().hasAlpha()).isTrue();
    }
}
