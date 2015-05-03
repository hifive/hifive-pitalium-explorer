package com.htmlhifive.testexplorer.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class EdgeDetectorTest {
    @Test
    public void testColors() throws IOException {
        EdgeDetector e = new EdgeDetector(0.5);
        Assert.assertEquals(e.getSigma(), 0.5, 0.0);

        BufferedImage image = null;
        BufferedImage expected = null;

        image = ImageIO.read(new File("src/test/resources/images/edge_detector_0.png"));
        expected = ImageIO.read(new File("src/test/resources/images/edge_detector_0_edge.png"));

        Color testBackgroundColor = Color.red;
        Color testForegroundColor = Color.blue;

        e.setBackgroundColor(testBackgroundColor);
        e.setForegroundColor(testForegroundColor);
        Assert.assertEquals(testBackgroundColor, e.getBackgroundColor());
        Assert.assertEquals(testForegroundColor, e.getForegroundColor());

        e.setThreshold(1.0/20, 1.0/60);
        BufferedImage result = e.DetectEdge(image);
        Assert.assertEquals(result.getWidth(), image.getWidth());
        Assert.assertEquals(result.getHeight(), image.getHeight());
        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                if (expected.getRGB(i, j) == Color.black.getRGB()) {
                    /* this is background */
                    Assert.assertEquals(testBackgroundColor.getRGB(), result.getRGB(i, j));
                } else {
                    Assert.assertEquals(testForegroundColor.getRGB(), result.getRGB(i, j));
                }
            }
        }
    }

    @Test
    public void testDetector() throws IOException {
        EdgeDetector e = new EdgeDetector();
        BufferedImage image = null;
        BufferedImage expected = null;

        image = ImageIO.read(new File("src/test/resources/images/edge_detector_0.png"));
        expected = ImageIO.read(new File("src/test/resources/images/edge_detector_0_edge.png"));

        e.setSigma(0.5);
        Assert.assertEquals(e.getSigma(), 0.5, 0.0);
        e.setThreshold(1.0/20, 1.0/60);
        
        e.setBackgroundColor(Color.black);
        e.setForegroundColor(Color.white);
        
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
