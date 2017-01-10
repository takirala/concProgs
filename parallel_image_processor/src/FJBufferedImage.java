import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

public class FJBufferedImage extends BufferedImage {

    /**
     * Constructors
     */
    public FJBufferedImage(int width, int height, int imageType) {
        super(width, height, imageType);
    }

    public FJBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
        super(width, height, imageType, cm);
    }

    public FJBufferedImage(ColorModel cm, WritableRaster raster,
                           boolean isRasterPremultiplied,
                           Hashtable<?, ?> properties) {
        super(cm, raster, isRasterPremultiplied, properties);
    }

    /**
     * Creates a new FJBufferedImage with the same fields as source.
     *
     * @param source
     * @return
     */
    public static FJBufferedImage BufferedImageToFJBufferedImage(
            BufferedImage source) {
        Hashtable<String, Object> properties = null;
        String[] propertyNames = source.getPropertyNames();
        if (propertyNames != null) {
            properties = new Hashtable<String, Object>();
            for (String name : propertyNames) {
                properties.put(name, source.getProperty(name));
            }
        }
        return new FJBufferedImage(source.getColorModel(), source
                .getRaster(), source.isAlphaPremultiplied(), properties);
    }

    /**
     * After various experimentation, the choice of row splitting seemed
     * apt. This also makes sense because when we are working by row,
     * if we have an array of 100 elements and each row size is 10 then
     * those 10 memory locations would be accessed during same
     * instruction thus decreasing the number of cache misses.
     * <p>
     * Other methods I have tried to split the work load were to split by
     * column and to use a grid of size 1% of width x 1% of height.
     */
    @Override
    public void setRGB(int xStart, int yStart, int w, int h, int[]
            rgbArray, int offset, int scansize) {
        /**
         * I have implemented the setRGBFJ using the ForkJoinPool and
         * setRGBStream using the stream. Streams gives relatively faster
         * performance.
         */
        setRGBFJ(xStart, yStart, w, h, rgbArray, offset, scansize);
    }

    @Override
    public int[] getRGB(int xStart, int yStart, int w, int h, int[] rgbArray,
                        int offset, int scansize) {
        return getRGBFJ(xStart, yStart, w, h, rgbArray, offset, scansize);
    }

    // This getRGB uses ForkJoinPool to parallelize.
    public int[] getRGBFJ(int xStart, int yStart, int w, int h, int[] rgbArray,
                          int offset, int scansize) {
        final int[] result = rgbArray != null ? rgbArray : new int[offset +
                h * scansize];
        // Each with workload of one row. [from, to]
        // These from, to represent y parameter.
        class RecursiveGetter extends RecursiveAction {
            int from;
            int to;

            RecursiveGetter(int from, int to) {
                this.from = from;
                this.to = to;
            }

            @Override
            protected void compute() {
                if (from == to) {
                    getRealRGB(xStart, from, w, 1, result, offset + from *
                            scansize, scansize);
                } else {
                    int mid = from + (to - from) / 2;
                    invokeAll(
                            new RecursiveGetter(from, mid),
                            new RecursiveGetter(mid + 1, to));
                }
            }
        }
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        pool.invoke(new RecursiveGetter(0, h - 1));
        return result;
    }

    // This setRGB uses ForkJoinPool to parallelize.
    public void setRGBFJ(int xStart, int yStart, int w, int h, int[]
            rgbArray, int offset, int scansize) {
        class RecursiveSetter extends RecursiveAction {
            int from;
            int to;

            RecursiveSetter(int from, int to) {
                this.from = from;
                this.to = to;
            }

            @Override
            protected void compute() {
                if (from == to) { //Implicit threshold = 1
                    setRealRGB(xStart, from, w, 1, rgbArray, offset + from *
                            scansize, scansize);
                } else {
                    int mid = from + (to - from) / 2;
                    invokeAll(
                            new RecursiveSetter(from, mid),
                            new RecursiveSetter(mid + 1, to));
                }
            }
        }
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        pool.invoke(new RecursiveSetter(0, h - 1));
    }

    // This getRGB uses Streams to parallelize.
    public int[] getRGBStream(int xStart, int yStart, int w, int h, int[] rgbArray,
                              int offset, int scansize) {
        final int[] result = rgbArray != null ? rgbArray : new int[offset +
                h * scansize];
        IntStream.range(yStart, yStart + h).parallel()
                .forEach(y -> getRealRGB(xStart, y, w, 1, result, offset +
                        y * scansize, scansize));
        return result;
    }

    // This setRGB uses Streams to parallelize.
    public void setRGBStream(int xStart, int yStart, int w, int h, int[]
            rgbArray, int offset, int scansize) {
        IntStream.range(yStart, yStart + h).parallel()
                .forEach(y -> setRealRGB(xStart, y, w, 1, rgbArray, offset +
                        y * scansize, scansize));
    }

    public int[] getRealRGB(int xStart, int yStart, int w, int h, int[]
            rgbArray, int offset, int scansize) {
        return super.getRGB(xStart, yStart, w, h, rgbArray, offset, scansize);
    }

    public void setRealRGB(int xStart, int yStart, int w, int h, int[]
            rgbArray, int offset, int scansize) {
        super.setRGB(xStart, yStart, w, h, rgbArray, offset, scansize);
    }
}
