import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ColorHistEq {

    //Number of bins parameter for improving smoothness.
    static final int nBins = 100;
    //Use these labels to instantiate you timers.  You will need 8 invocations of now()
    static String[] labels = {"getRGB", "convert to HSB",
            "create brightness map", "probability array", "parallel prefix",
            "equalize pixels", "setRGB"};

    static Timer colorHistEq_serial(BufferedImage image, BufferedImage newImage) {
        Timer times = new Timer(labels);
        int w = image.getWidth();
        int h = image.getHeight();

        //GetRGB
        times.now();
        int[] sourcePixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);

        //Convert to HSB
        times.now();
        float[][] hsbVals = IntStream.range(0, sourcePixelArray.length)
                .mapToObj(index -> {
                    Color c = new Color(sourcePixelArray[index]);
                    return Color.RGBtoHSB(c.getRed(), c.getGreen(), c
                            .getBlue(), null);
                }).toArray(float[][]::new);

        //Create Brightness Map
        // The brightness value is multiplied by nBins-1 because the
        // brightnes value is can be [0,1] inclusive. So if brightness value
        // is 1 and we multiply it with nBins, then it may throw an
        // IndexOutOfBound exception. With 100 bins, the result is exactly
        // similar to the image uploaded in canvas.
        times.now();
        Map<Integer, Long> bHist = Stream.of(hsbVals)
                .map(hsbPixel -> hsbPixel[2])
                .collect(
                        Collectors.groupingBy(
                                v -> (int) (v * (nBins - 1)),
                                Collectors.counting())
                );

        //Probability Array
        times.now();
        long numOfPixels = hsbVals.length;
        long[] bins = new long[nBins];
        for (int i = 0; i < nBins; i++) {
            Long val = bHist.get(i);
            bins[i] = val != null ? val : 0L;
        }

        //Parallel prefix
        times.now();
        float[] cumProbs = new float[nBins];
        Arrays.parallelPrefix(bins, (x, y) -> x + y);
        IntStream.range(0, bins.length).forEach(
                i -> cumProbs[i] = ((float) bins[i]) / numOfPixels);

        //Equalize Pixels
        times.now();
        int[] rgbArray = Arrays.stream(hsbVals).mapToInt(pixel -> {
            float brightness = pixel[2];
            int binIndex = (int) (brightness * (nBins - 1));
            pixel[2] = cumProbs[binIndex];
            return Color.HSBtoRGB(pixel[0], pixel[1], pixel[2]);
        }).toArray();

        //Set RGB
        times.now();
        newImage.setRGB(0, 0, w, h, rgbArray, 0, w);
        times.now();
        return times;
    }

    static Timer colorHistEq_parallel(FJBufferedImage image, FJBufferedImage
            newImage) {
        Timer times = new Timer(labels);
        int w = image.getWidth();
        int h = image.getHeight();

        //GetRGB
        times.now();
        int[] sourcePixelArray = image.getRGB(0, 0, w, h, new int[w * h],
                0, w);

        //Convert to HSB
        times.now();
        float[][] hsbVals = IntStream.range(0, sourcePixelArray.length)
                .parallel()
                .mapToObj(index -> {
                    Color c = new Color(sourcePixelArray[index]);
                    return Color.RGBtoHSB(c.getRed(), c.getGreen(), c
                            .getBlue(), null);
                }).toArray(float[][]::new);

        //Create Brightness Map
        times.now();
        Map<Integer, Long> bHist = Stream.of(hsbVals)
                .parallel()
                .map(hsbPixel -> hsbPixel[2])
                .collect(
                        Collectors.groupingBy(
                                v -> (int) (v * (nBins - 1)),
                                Collectors.counting())
                );

        //Probability Array. This is just a simple for loop for the number of
        // bins specified. Parallelism won't make a significant difference here.
        times.now();
        long numOfPixels = hsbVals.length;
        long[] bins = new long[nBins];
        for (int i = 0; i < nBins; i++) {
            Long val = bHist.get(i);
            bins[i] = val != null ? val : 0L;
        }

        //Parallel prefix
        times.now();
        float[] cumProbs = new float[nBins];
        Arrays.parallelPrefix(bins, (x, y) -> x + y);
        IntStream.range(0, bins.length)
                .parallel()
                .forEach(i -> cumProbs[i] = ((float) bins[i]) / numOfPixels);

        //Equalize Pixels
        times.now();
        int[] rgbArray = Arrays.stream(hsbVals)
                .parallel()
                .mapToInt(pixel -> {
                    float brightness = pixel[2];
                    int binIndex = (int) (brightness * (nBins - 1));
                    pixel[2] = cumProbs[binIndex];
                    return Color.HSBtoRGB(pixel[0], pixel[1], pixel[2]);
                }).toArray();

        //Set RGB
        times.now();
        newImage.setRGB(0, 0, w, h, rgbArray, 0, w);
        times.now();
        return times;
    }

}
