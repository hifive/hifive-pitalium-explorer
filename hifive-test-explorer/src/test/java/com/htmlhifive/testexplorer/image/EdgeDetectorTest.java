package com.htmlhifive.testexplorer.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class EdgeDetectorTest {
    @Test
    public void testDetector() throws IOException {
        EdgeDetector e = new EdgeDetector();
        BufferedImage image = null;
        BufferedImage expected = null;

        try {
            image = ImageIO.read(new File("src/test/resources/images/edge_detector_0.png"));
            expected = ImageIO.read(new File("src/test/resources/images/edge_detector_0_edge.png"));
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        e.setSigma(0.5);
        Assert.assertEquals(e.getSigma(), 0.5, 0.0);
        e.setThreshold(1.0/20, 1.0/60);
        BufferedImage result = e.DetectEdge(image);
        Assert.assertEquals(result.getWidth(), image.getWidth());
        Assert.assertEquals(result.getHeight(), image.getHeight());
        /* To generate new expected image, uncomment the following. */
        /* ImageIO.write(result, "png", new File("src/test/resources/images/edge_detector_0_edge.png")); */
        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                Assert.assertEquals(result.getRGB(i, j), expected.getRGB(i, j));
            }
        }
    }
}
