import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by takirala on 11/28/2016.
 */
public class Test {
    private static String path = "C:\\Users\\Lavenger\\git\\cpAssign3\\src\\cop5618\\";
    private static String inputName = "LotsaGators3.jpg";
    private static String outputName = "LotsaGators3mod.jpg";

    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    System.out.println("child");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.setName("thread1");
        t.start();
        Thread t1 = new Thread(() -> System.out.println("new thar"));
        t1.setName("thread2");
        t1.start();
        System.out.println("main");
    }


    public static void main2(String[] args) throws IOException {


        System.out.println("ancd".substring(1, 1));
        File src = new File(path + inputName);
        BufferedImage srcImage = ImageIO.read(src);
        System.out.println(srcImage.getHeight());
        System.out.println(srcImage.getWidth());
        int maxOffset = srcImage.getHeight() * srcImage.getWidth();
        srcImage.setRGB(1000, 1000, 500, 500, new int[150000], 1000, 10);
        ImageIO.write(srcImage, "jpg", new File(path + outputName));
    }

    static void gray_SS(BufferedImage src, BufferedImage dest) {

        ColorModel colorModel = ColorModel.getRGBdefault();
        int[] rgb = src.getRGB(0, 0, src.getWidth(), src.getHeight(), null, 0, src.getWidth());
        int[] rgb_gray = Arrays.stream(rgb)
                .map(pixel -> (int) (colorModel.getRed(pixel) * 0.299) +
                        (int) (colorModel.getGreen(pixel) * .587) +
                        (int) (colorModel.getBlue(pixel) * 0.114))
                .map(grayPixel -> makeRGBPixel(grayPixel,
                        grayPixel, grayPixel)).toArray();
        dest.setRGB(0, 0, dest.getWidth(), dest.getHeight(), rgb_gray, 0,
                dest.getWidth());
    }

    static int makeRGBPixel(int red, int green, int blue) {
        return ((255 & 0xFF) << 24)
                | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8)
                | ((blue & 0xFF) << 0);
    }
}
