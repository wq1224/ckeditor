/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  net.coobird.thumbnailator.Thumbnails
 *  net.coobird.thumbnailator.Thumbnails$Builder
 *  org.apache.commons.fileupload.FileItem
 */
package com.ckfinder.connector.utils;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.utils.FileUtils;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.fileupload.FileItem;

public class ImageUtils {
    private static final String[] ALLOWED_EXT = new String[]{"gif", "jpeg", "jpg", "png", "bmp", "xbm"};
    private static final int MAX_BUFF_SIZE = 1024;

    private static void resizeImage(BufferedImage sourceImage, int width, int height, float quality, File destFile) throws IOException {
        try {
            Thumbnails.of((BufferedImage[])new BufferedImage[]{sourceImage}).size(width, height).keepAspectRatio(false).outputQuality(quality).toFile(destFile);
        }
        catch (IllegalStateException e) {
            Thumbnails.of((BufferedImage[])new BufferedImage[]{sourceImage}).size(width, height).keepAspectRatio(false).toFile(destFile);
        }
    }

    public static void createThumb(File orginFile, File file, IConfiguration conf) throws IOException {
        BufferedImage image = ImageIO.read(orginFile);
        if (image != null) {
            Dimension dimension = ImageUtils.createThumbDimension(image, conf.getMaxThumbWidth(), conf.getMaxThumbHeight());
            FileUtils.createPath(file, true);
            if (image.getHeight() == dimension.height && image.getWidth() == dimension.width) {
                ImageUtils.writeUntouchedImage(orginFile, file);
            } else {
                ImageUtils.resizeImage(image, dimension.width, dimension.height, conf.getThumbsQuality(), file);
            }
        } else if (conf.isDebugMode()) {
            throw new IOException("Wrong image file");
        }
    }

    public static void createTmpThumb(InputStream stream, File file, String fileName, IConfiguration conf) throws IOException {
        BufferedInputStream bufferedIS = new BufferedInputStream(stream);
        bufferedIS.mark(Integer.MAX_VALUE);
        BufferedImage image = ImageIO.read(bufferedIS);
        if (image == null) {
            throw new IOException("Wrong file");
        }
        Dimension dimension = ImageUtils.createThumbDimension(image, conf.getImgWidth(), conf.getImgHeight());
        if (dimension.width == 0 || dimension.height == 0 || image.getHeight() == dimension.height && image.getWidth() == dimension.width) {
            bufferedIS.reset();
            ImageUtils.writeUntouchedImage(bufferedIS, file);
        } else {
            ImageUtils.resizeImage(image, dimension.width, dimension.height, conf.getImgQuality(), file);
        }
        stream.close();
    }

    public static void createResizedImage(File sourceFile, File destFile, int width, int height, float quality) throws IOException {
        BufferedImage image = ImageIO.read(sourceFile);
        Dimension dimension = new Dimension(width, height);
        if (image.getHeight() == dimension.height && image.getWidth() == dimension.width) {
            ImageUtils.writeUntouchedImage(sourceFile, destFile);
        } else {
            ImageUtils.resizeImage(image, dimension.width, dimension.height, quality, destFile);
        }
    }

    private static Dimension createThumbDimension(BufferedImage image, int maxWidth, int maxHeight) {
        Dimension dimension = new Dimension();
        if (image.getWidth() >= image.getHeight()) {
            if (image.getWidth() >= maxWidth) {
                dimension.width = maxWidth;
                dimension.height = Math.round((float)maxWidth / (float)image.getWidth() * (float)image.getHeight());
            } else {
                dimension.height = image.getHeight();
                dimension.width = image.getWidth();
            }
        } else if (image.getHeight() >= maxHeight) {
            dimension.height = maxHeight;
            dimension.width = Math.round((float)maxHeight / (float)image.getHeight() * (float)image.getWidth());
        } else {
            dimension.height = image.getHeight();
            dimension.width = image.getWidth();
        }
        return dimension;
    }

    public static boolean isImage(File file) {
        List<String> list = Arrays.asList(ALLOWED_EXT);
        if (file != null) {
            String fileExt = FileUtils.getFileExtension(file.getName().toLowerCase());
            return fileExt != null ? list.contains(fileExt) : false;
        }
        return false;
    }

    public static boolean checkImageSize(InputStream stream, IConfiguration conf) throws IOException {
        Integer maxWidth = conf.getImgWidth();
        Integer maxHeight = conf.getImgHeight();
        if (maxHeight == 0 && maxWidth == 0) {
            return true;
        }
        BufferedImage bi = ImageIO.read(stream);
        stream.close();
        if (bi == null) {
            return false;
        }
        return bi.getHeight() <= maxHeight && bi.getWidth() <= maxWidth;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean checkImageFile(FileItem item) {
        BufferedImage bi;
        InputStream is = null;
        try {
            is = item.getInputStream();
            bi = ImageIO.read(is);
        }
        catch (IOException e) {
            boolean bl = false;
            return bl;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception var5_6) {}
            }
        }
        return bi != null;
    }

    private static void writeUntouchedImage(File sourceFile, File destFile) throws IOException {
        FileInputStream fileIS = new FileInputStream(sourceFile);
        ImageUtils.writeUntouchedImage(fileIS, destFile);
    }

    private static void writeUntouchedImage(InputStream stream, File destFile) throws IOException {
        int readNum;
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while ((readNum = stream.read(buffer)) != -1) {
            byteArrayOS.write(buffer, 0, readNum);
        }
        byte[] bytes = byteArrayOS.toByteArray();
        byteArrayOS.close();
        FileOutputStream fileOS = new FileOutputStream(destFile);
        fileOS.write(bytes);
        fileOS.flush();
        fileOS.close();
    }
}

