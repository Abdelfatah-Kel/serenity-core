package net.thucydides.core.images;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;

public class ResizableImage {

    private final File screenshotFile;
    private final SimpleImageInfo imageInfo;
    private final int MAX_SUPPORTED_HEIGHT = 4000;

    private final Logger logger = LoggerFactory.getLogger(ResizableImage.class);

    protected Logger getLogger() {
        return logger;
    }

    public ResizableImage(final File screenshotFile) throws IOException {
        this.screenshotFile = screenshotFile;
        this.imageInfo = new SimpleImageInfo(screenshotFile);
    }

    public static ResizableImage loadFrom(final File screenshotFile) throws IOException {
        return new ResizableImage(screenshotFile);
    }

    public int getWitdh() {
        return imageInfo.getWidth();
    }

    public int getHeight() {
        return imageInfo.getHeight();
    }

    public ResizableImage rescaleCanvas(final int height) throws IOException {

        if (skipRescale(height)) {
            return this;
        }

        int targetHeight = Math.min(height, MAX_SUPPORTED_HEIGHT);

        try {
            waitForCreationOfFile();
            BufferedImage image = ImageIO.read(screenshotFile);
            int width = new SimpleImageInfo(screenshotFile).getWidth();
            return resizeImage(width, targetHeight, image);
        } catch (Throwable e) {
            getLogger().warn("Could not resize screenshot, so leaving original version: " + screenshotFile, e);
            return this;
        }
    }

    private void waitForCreationOfFile() {
        await().atMost(30, TimeUnit.SECONDS).until(screenshotIsProcessed());
    }

    private Callable<Boolean> screenshotIsProcessed() {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return (screenshotFile.exists() && screenshotFile.length() > 0);
            }
        };
    }

    protected ResizableImage resizeImage(int width, int targetHeight, BufferedImage image) throws IOException {
        try {
            int imageType = (image.getType() > 0) ? image.getType() : BufferedImage.TYPE_4BYTE_ABGR;
			BufferedImage resizedImage = new BufferedImage(width, targetHeight, imageType);
			fillWithWhiteBackground(resizedImage);
			resizedImage.setData(image.getRaster());
	        return new ResizedImage(resizedImage, screenshotFile);
		} catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
    }

    private boolean skipRescale(int height) {
        if (getHeight() > MAX_SUPPORTED_HEIGHT) {
            return true;
        }

        if (getHeight() >= height) {
            return true;
        }

        return false;
    }

    private void fillWithWhiteBackground(final BufferedImage resizedImage) {
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fill(new Rectangle2D.Float(0, 0, resizedImage.getWidth(), resizedImage.getHeight()));
        g2d.dispose();
    }

    /**
     * If no resize operation has been done, just copy the file.
     * Otherwise we should be applying the saveTo() method on the ResizedImage class.
     */
    public void saveTo(final File savedFile) throws IOException {
        Files.copy(screenshotFile.toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
