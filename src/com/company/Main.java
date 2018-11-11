package com.company;


import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String AUDIO_MP3_CODEC = "mp3";
    private static File a = new File("C:\\Users\\user\\Documents\\Repositorio\\testEncodeAudio09.wav");
    private static String b = new String("C:\\Users\\user\\Documents\\Repositorio\\SlickConvert\\audioOutput.wav");

    private static JFrame frame = new JFrame();
    private static JProgressBar progBar = new JProgressBar();

    public static void main(String[] args) throws IOException {
//        convertWaveToMp3(a,b);


        progBar.setStringPainted(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(2,1));
        frame.setSize(400,100);
        frame.setLocationRelativeTo(null);
        frame.add(progBar);
        frame.setVisible(true);
        prueba();
    }

    public static File convertWaveToMp3(final File wavFile, final String mp3Filename) throws IOException {
        // will read out path to executables from environment variables FFMPEG and FFPROBE
        // take care of those variables being set in your system
        final FFmpeg ffmpeg = new FFmpeg();
        System.out.println(FFmpeg.DEFAULT_PATH);
        final FFprobe ffprobe = new FFprobe();
        final FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(wavFile.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(mp3Filename)
                .setAudioCodec(AUDIO_MP3_CODEC)
                .setAudioChannels(FFmpeg.AUDIO_MONO)
                .setAudioBitRate(FFmpeg.AUDIO_SAMPLE_48000)
                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                .done();
        final FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        // Run a one-pass encode
        executor.createJob(builder).run();
        return new File(mp3Filename);
    }

    public static void prueba() throws IOException {
        final FFmpeg ffmpeg = new FFmpeg();
        FFprobe ffprobe = new FFprobe("ffprobe");
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        FFmpegProbeResult in = ffprobe.probe("C:\\Users\\user\\Downloads\\Video\\Farruko-Coolant.mp4");

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(in) // Or filename
                .overrideOutputFiles(true) // Override the output if it exists
                .addOutput("C:\\Users\\user\\Documents\\Repositorio\\output.mp4")  // Filename for the destination

                .setFormat("mp4")        // Format is inferred from filename, or can be set
                .setTargetSize(250_000)  // Aim for a 250KB file

                .disableSubtitle()       // No subtiles

                .setAudioChannels(1)         // Mono audio
                .setAudioCodec("aac")        // using the aac codec
                .setAudioSampleRate(48_000)  // at 48KHz
                .setAudioBitRate(32768)      // at 32 kbit/s

                .setVideoCodec("libx264")     // Video using x264
                .setVideoFrameRate(24, 1)     // at 24 frames per second
                .setVideoResolution(640, 480) // at 640x480 resolution
                .done();

        FFmpegJob job = executor.createJob(builder, new ProgressListener() {

            // Using the FFmpegProbeResult determine the duration of the input
            final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

            @Override
            public void progress(Progress progress) {

                progBar.setMaximum(100);
                double percentage = progress.out_time_ns / duration_ns;
                progBar.setValue(Integer.parseInt(String.format(
                        "%.0f",
                        percentage * 100,
                        progress.status
                )));

                // Print out interesting information about the progress
                System.out.println(String.format(
                        "[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx",
                        percentage * 100,
                        progress.status,
                        progress.frame,
                        FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                        progress.fps.doubleValue(),
                        progress.speed
                ));
            }
        });

        job.run();
    }

}

