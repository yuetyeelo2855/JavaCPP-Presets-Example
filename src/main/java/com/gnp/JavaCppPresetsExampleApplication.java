package com.gnp;

import org.bytedeco.javacpp.FlyCapture2;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.CV_INTER_NN;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;

@SpringBootApplication
public class JavaCppPresetsExampleApplication {
    private static final Logger logger = LoggerFactory.getLogger(JavaCppPresetsExampleApplication.class);
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final int FRAME_RATE = 30;

    public static void main(String[] args) {
        SpringApplication.run(JavaCppPresetsExampleApplication.class, args);
        try {
//            String originalPath = args.length > 1 ? args[0] : JavaCppPresetsExampleApplication.class.getResource("/lena.png").getFile();
//            File originalFile = new File(originalPath);
//            File destFile = args.length > 2 ? new File(args[1]) : new File(System.getProperty("user.home"), originalFile.getName());
//            resize(originalFile, destFile);
            File videoFie = args.length > 1 ? new File(args[0]) : new File(System.getProperty("user.home"), "test.mp4");
            screenshot(videoFie);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void resize(File original, File destFile) throws IOException {
        logger.debug("Start to Resize");
        Java2DFrameConverter frameConverter = new Java2DFrameConverter();
        OpenCVFrameConverter.ToIplImage iplImageConverter = new OpenCVFrameConverter.ToIplImage();
        logger.debug("Open File: {}", original.getAbsolutePath());
        BufferedImage bufferedImage = ImageIO.read(original);
        Frame frame = frameConverter.convert(bufferedImage);
        opencv_core.IplImage source = iplImageConverter.convert(frame);
        if (source != null) {
            opencv_core.IplImage dest = cvCreateImage(cvSize(source.width() / 2, source.height() / 2), source.depth(), source.nChannels());
            cvResize(source, dest, CV_INTER_NN);
            frame = iplImageConverter.convert(dest);
            bufferedImage = frameConverter.convert(frame);
            ImageIO.write(bufferedImage, "png", destFile);
            logger.debug("Save Image: {}", destFile.getAbsolutePath());
            cvReleaseImage(source);
            cvReleaseImage(dest);
        }
    }

    private static void screenshot(File videoFile) throws FrameRecorder.Exception, FrameGrabber.Exception {
        FlyCapture2FrameGrabber frameGrabber = new FlyCapture2FrameGrabber(0);
        frameGrabber.setImageMode(FrameGrabber.ImageMode.COLOR);
        frameGrabber.setPixelFormat(FlyCapture2.PIXEL_FORMAT_422YUV8);
        frameGrabber.setFrameRate(FRAME_RATE);
        frameGrabber.setImageWidth(WIDTH);
        frameGrabber.setImageHeight(HEIGHT);
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(videoFile, WIDTH, HEIGHT);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setVideoOption("preset", "veryfast");
        recorder.setFormat("mp4");
        recorder.setFrameRate(FRAME_RATE);
        recorder.setVideoBitrate(WIDTH * HEIGHT);

        frameGrabber.start();
        recorder.start();

        for (int i = 0; i < 30 * 30; i++) {
            logger.debug("Grab {}/150 Frame", i);
            Frame frame = frameGrabber.grab();
            recorder.record(frame);
        }

        frameGrabber.release();

        recorder.stop();
        recorder.release();
    }
}
