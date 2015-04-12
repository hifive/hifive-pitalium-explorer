package com.htmlhifive.testexplorer.image;

import java.awt.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.Stack;

/**
 * This class detects edges from images.
 */
public class EdgeDetector {

    /**
     * The value used for gaussian filter.
     */
    private double sigma;

    /**
     * threshold for strong edges. Its proper range is [0, 1]
     */
    private double thresholdHigh;

    /**
     * threshold for maybe edges. Its proper range is [0, 1]
     */
    private double thresholdLow;

    /**
     * gaussian filters in each dimension.
     */
    private ConvolveOp gaussianX, gaussianY;

    /**
     * color used to fill background
     */
    private Color backgroundColor;

    /**
     * color used to paint edges
     */
    private Color foregroundColor;

    /**
     * Default Constructor
     */
    public EdgeDetector() {
        this(Math.sqrt(2));
    }

    /**
     * Constructor accepting sigma value
     *
     * @param sigma This value is used for gaussian filter.
     */
    public EdgeDetector(double sigma) {
        this.sigma = sigma;
        this.gaussianX = ConvolveOpGenerator.GenerateGaussianX(sigma);
        this.gaussianY = ConvolveOpGenerator.GenerateGaussianY(sigma);
        this.thresholdHigh = 1.0 / 40 / sigma;
        this.thresholdLow = 1.0 / 90 / sigma;

        this.backgroundColor = Color.black;
        this.foregroundColor = Color.white;
    }

    /**
     * Check if a is the maximum which should not be suppressed in non maximum suppression stage.
     *
     * @param a Current value
     * @param b Value of one neighbor
     * @param c Value of the other neighbor
     * @return true iff a is the maximum value.
     */
    private boolean testMaximum(float a, float b, float c) {
        return b <= a && c < a;
    }

    /**
     * Extract edges from image.
     *
     * @param image An image to process edge detection.
     * @return Binary Image containing edges. Edges are in white color.
     */
    public BufferedImage DetectEdge(BufferedImage image) {
        /* Apply gaussian filter */
        image = gaussianY.filter(gaussianX.filter(image, null), null);

        int width = image.getWidth(), height = image.getHeight();

        int[] imageRGBs = image.getRGB(0, 0, width, height, null, 0, width);

        int[][] gradientX = new int[height][width];
        int[][] gradientY = new int[height][width];
        float[][] gradientAbs = new float[height][width];

        float maximumValue = (float) (4080 * Math.sqrt(2) * 6);

        /* Calculate gradients for each pixel */
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color p00 = new Color(imageRGBs[Math.max(0, i - 1) * width + Math.max(0, j - 1)], true);
                Color p01 = new Color(imageRGBs[Math.max(0, i - 1) * width + j], true);
                Color p02 = new Color(imageRGBs[Math.max(0, i - 1) * width + Math.min(width - 1, j + 1)], true);
                Color p10 = new Color(imageRGBs[i * width + Math.max(0, j - 1)], true);
                Color p12 = new Color(imageRGBs[i * width + Math.min(width - 1, j + 1)], true);
                Color p20 = new Color(imageRGBs[Math.min(height - 1, i + 1) * width + Math.max(0, j - 1)], true);
                Color p21 = new Color(imageRGBs[Math.min(height - 1, i + 1) * width + j], true);
                Color p22 = new Color(imageRGBs[Math.min(height - 1, i + 1) * width + Math.min(width - 1, j + 1)], true);
                int rX = (p02.getRed() * 3 + p12.getRed() * 10 + p22.getRed() * 3) - (p00.getRed() * 3 + p10.getRed() * 10 + p20.getRed() * 3);
                int gX = (p02.getGreen() * 3 + p12.getGreen() * 10 + p22.getGreen() * 3) - (p00.getGreen() * 3 + p10.getGreen() * 10 + p20.getGreen() * 3);
                int bX = (p02.getBlue() * 3 + p12.getBlue() * 10 + p22.getBlue() * 3) - (p00.getBlue() * 3 + p10.getBlue() * 10 + p20.getBlue() * 3);

                int rY = (p20.getRed() * 3 + p21.getRed() * 10 + p22.getRed() * 3) - (p00.getRed() * 3 + p01.getRed() * 10 + p02.getRed() * 3);
                int gY = (p20.getGreen() * 3 + p21.getGreen() * 10 + p22.getGreen() * 3) - (p00.getGreen() * 3 + p01.getGreen() * 10 + p02.getGreen() * 3);
                int bY = (p20.getBlue() * 3 + p21.getBlue() * 10 + p22.getBlue() * 3) - (p00.getBlue() * 3 + p01.getBlue() * 10 + p02.getBlue() * 3);

                int x = 2*rX + 3*gX + bX, y = 2*rY + 3*gY + bY;
                gradientX[i][j] = x;
                gradientY[i][j] = y;
                gradientAbs[i][j] = (float) Math.hypot(x, y);
            }
        }

        float absoluteThresholdHigh = (float) (maximumValue * this.thresholdHigh);
        float absoluteThresholdLow = (float) (maximumValue * this.thresholdLow);

        /* Non maximal suppression */
        float[][] maximumEdges = new float[height][width];
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                if (gradientAbs[i][j] < absoluteThresholdLow)
                    continue;
                double angle = Math.atan2(gradientY[i][j], gradientX[i][j]);
                if (angle < 0) angle += Math.PI;
                boolean isMaximum;
                if (angle <= Math.PI / 8 || angle > Math.PI * 7 / 8) {
                    isMaximum = testMaximum(gradientAbs[i][j], gradientAbs[i][j - 1], gradientAbs[i][j + 1]);
                } else if (angle <= Math.PI * 3 / 8) {
                    isMaximum = testMaximum(gradientAbs[i][j], gradientAbs[i - 1][j - 1], gradientAbs[i + 1][j + 1]);
                } else if (angle <= Math.PI * 5 / 8) {
                    isMaximum = testMaximum(gradientAbs[i][j], gradientAbs[i - 1][j], gradientAbs[i + 1][j]);
                } else {
                    isMaximum = testMaximum(gradientAbs[i][j], gradientAbs[i + 1][j - 1], gradientAbs[i - 1][j + 1]);
                }
                maximumEdges[i][j] = isMaximum ? gradientAbs[i][j] : 0f;
            }
        }

        /* Edge detection */

        /* value used for painting */
        int backgroundNumber = this.backgroundColor.getRGB();
        int foregroundNumber = this.foregroundColor.getRGB();

        /* prepare result image */
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] cannyEdge = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
        Arrays.fill(cannyEdge, backgroundNumber);

        Stack<Coordinate2D<Integer>> follow = new Stack<>();
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                if (cannyEdge[i * width + j] != backgroundNumber) continue;
                if (maximumEdges[i][j] >= absoluteThresholdHigh) {
                    cannyEdge[i * width + j] = foregroundNumber;
                    follow.push(new Coordinate2D<>(j, i));
                    while (!follow.isEmpty()) {
                        Coordinate2D<Integer> top = follow.pop();
                        for (int ny = Math.max(0, top.y - 1); ny <= Math.min(height - 1, top.y + 1); ny++) {
                            for (int nx = Math.max(0, top.x - 1); nx <= Math.min(height - 1, top.x + 1); nx++) {
                                if (cannyEdge[ny * width + nx] != backgroundNumber) continue;
                                if (maximumEdges[ny][nx] >= absoluteThresholdLow) {
                                    cannyEdge[ny * width + nx] = foregroundNumber;
                                    follow.push(new Coordinate2D<>(nx, ny));
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Get sigma
     * @return current sigma value
     */
    public double getSigma() {
        return sigma;
    }

    /**
     * Set sigma for gaussian filter.
     * @param sigma sigma value
     */
    public void setSigma(double sigma) {
        this.sigma = sigma;
        this.gaussianX = ConvolveOpGenerator.GenerateGaussianX(sigma);
        this.gaussianY = ConvolveOpGenerator.GenerateGaussianY(sigma);
    }

    /**
     * Set threshold value. The proper range for thresholds is [0, 1].
     * @param thresholdHigh threshold value for strong edge.
     * @param thresholdLow threshold value for weak edge.
     */
    public void setThreshold(double thresholdHigh, double thresholdLow) {
        this.thresholdHigh = thresholdHigh;
        this.thresholdLow = thresholdLow;
    }

    /**
     * Get color used to fill background
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Set color used to fill background
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Get color used to paint edges
     */
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Set color used to paint edges
     */
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }
}
