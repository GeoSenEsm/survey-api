package com.survey.application.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.Comparator;

import static com.survey.application.services.FileValidationServiceImpl.getFileExtension;


@Service
public class StorageServiceImpl implements StorageService {
    private static final String BASE_DIRECTORY = "/uploads";

    /** Longest side after server-side downscale for the mobile header/splash. */
    private static final int MAX_LOGO_DIMENSION_PX = 512;
    /** Reject uploads larger than this before decoding (multipart default is 50MB). */
    private static final long MAX_LOGO_UPLOAD_BYTES = 5L * 1024 * 1024;
    /** Reject decoded rasters larger than this on either side (decompression-bomb guard). */
    private static final int MAX_LOGO_SOURCE_DIMENSION_PX = 8192;

    private final FileValidationService fileValidationService;

    public StorageServiceImpl(FileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
    }

    @Override
    public String store(MultipartFile file, String surveyName, String sectionOrder, String questionOrder, String optionOrder) throws IOException {
        fileValidationService.validateFileType(file.getOriginalFilename());

        Path directoryPath = Paths.get(BASE_DIRECTORY, formatSurveyName(surveyName), "sections", sectionOrder, "questions", questionOrder, "options");

        Files.createDirectories(directoryPath);

        String fileName = optionOrder + getFileExtension(file.getOriginalFilename());
        Path filePath = directoryPath.resolve(fileName);

        file.transferTo(filePath.toFile());

        return toUrlPath(filePath);
    }

    @Override
    public String storeSurveySettingsLogo(MultipartFile file) throws IOException {
        fileValidationService.validateFileType(file.getOriginalFilename());
        BufferedImage original = requireDecodableLogo(file);

        Path directoryPath = Paths.get(BASE_DIRECTORY, "survey_settings");
        Files.createDirectories(directoryPath);

        String fileName = "logo" + getFileExtension(file.getOriginalFilename());
        Path filePath = directoryPath.resolve(fileName);
        writeResizedLogo(original, filePath);
        return toUrlPath(filePath);
    }

    static BufferedImage requireDecodableLogo(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_LOGO_UPLOAD_BYTES) {
            throw new IllegalArgumentException(
                    "Logo file is too large (max " + (MAX_LOGO_UPLOAD_BYTES / (1024 * 1024)) + " MB).");
        }
        BufferedImage original;
        try (InputStream in = file.getInputStream()) {
            original = ImageIO.read(in);
        }
        if (original == null) {
            throw new IllegalArgumentException("Logo file is not a readable PNG or JPEG image.");
        }
        if (original.getWidth() > MAX_LOGO_SOURCE_DIMENSION_PX
                || original.getHeight() > MAX_LOGO_SOURCE_DIMENSION_PX) {
            throw new IllegalArgumentException(
                    "Logo image dimensions must be at most " + MAX_LOGO_SOURCE_DIMENSION_PX + "px on each side.");
        }
        return original;
    }

    private void writeResizedLogo(BufferedImage original, Path filePath) throws IOException {
        String formatName = imageIoFormatName(filePath);
        boolean supportsTransparency = "png".equals(formatName);
        BufferedImage output = render(original, boundedDimensions(original, MAX_LOGO_DIMENSION_PX), supportsTransparency);
        Path tempPath = Files.createTempFile(filePath.getParent(), "logo-", ".tmp");
        try {
            if (!ImageIO.write(output, formatName, tempPath.toFile())) {
                throw new IOException("Failed to encode resized logo as " + formatName + ".");
            }
            Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    static String imageIoFormatName(Path filePath) {
        String extension = getFileExtension(filePath.getFileName().toString()).replace(".", "").toLowerCase();
        return "jpg".equals(extension) ? "jpeg" : extension;
    }

    static Dimension boundedDimensions(BufferedImage source, int maxDimensionPx) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= maxDimensionPx && height <= maxDimensionPx) {
            return new Dimension(width, height);
        }
        double scale = Math.min((double) maxDimensionPx / width, (double) maxDimensionPx / height);
        return new Dimension(
                Math.max(1, Math.round((float) (width * scale))),
                Math.max(1, Math.round((float) (height * scale))));
    }

    /**
     * Always re-renders onto a fresh buffer of the target size and pixel format, even when the
     * source is already small enough: this is what lets a JPEG output drop any alpha channel
     * (onto a white background) instead of failing or producing color artifacts.
     */
    static BufferedImage render(BufferedImage source, Dimension targetSize, boolean supportsTransparency) {
        BufferedImage target = new BufferedImage(
                targetSize.width, targetSize.height,
                supportsTransparency ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!supportsTransparency) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, targetSize.width, targetSize.height);
            }
            g.drawImage(source, 0, 0, targetSize.width, targetSize.height, null);
        } finally {
            g.dispose();
        }
        return target;
    }

    @Override
    public void deleteFile(String path) {
        Path filePath = resolveStoredUploadPath(path);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete file: " + filePath, e);
        }
    }

    @Override
    public void deleteSurveyImages(String surveyName) {
        Path surveyDirectory = Paths.get(BASE_DIRECTORY, formatSurveyName(surveyName));

        if (!Files.exists(surveyDirectory)) {
            return;
        }

        if (!Files.isDirectory(surveyDirectory)) {
            throw new IllegalStateException("Path is not a directory: " + surveyDirectory);
        }

        try (var paths = Files.walk(surveyDirectory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed to delete path: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Error while deleting survey directory: " + surveyDirectory, e);
        }
    }

    private String formatSurveyName(String name) {
        return Normalizer.normalize(name.trim(), Normalizer.Form.NFKC)
                .replaceAll(" ", "_");
    }

    static String toUrlPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private Path resolveStoredUploadPath(String path) {
        Path basePath = Paths.get(BASE_DIRECTORY).toAbsolutePath().normalize();
        Path requestedPath = Paths.get(path).normalize();
        Path filePath = requestedPath.isAbsolute()
                ? requestedPath.toAbsolutePath().normalize()
                : basePath.resolve(requestedPath).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new IllegalArgumentException("Stored upload path is outside the upload directory.");
        }
        return filePath;
    }

}
