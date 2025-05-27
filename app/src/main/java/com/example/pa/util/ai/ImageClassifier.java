package com.example.pa.util.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImageClassifier {
    private Interpreter tflite;
    private TensorImage inputImageBuffer;
    private int imageSizeX;
    private int imageSizeY;
    private TensorBuffer outputProbabilityBuffer;
    private List<String> labels;
    ImageProcessor imageProcessor;

    public ImageClassifier(Context context) throws IOException {
        // 加载模型
        tflite = new Interpreter(FileUtil.loadMappedFile(context, "mobilenet_v1_1.0_224_quant.tflite"));

        // 读取标签
        labels = FileUtil.loadLabels(context, "labels_mobilenet_quant_v1_224.txt");

        // 获取输入输出张量信息
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape();
        imageSizeX = imageShape[1];
        imageSizeY = imageShape[2];

        int probabilityTensorIndex = 0;
        int[] probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape();

        // 准备输入输出缓冲区
        inputImageBuffer = new TensorImage(tflite.getInputTensor(imageTensorIndex).dataType());
        outputProbabilityBuffer = TensorBuffer.createFixedSize(
                probabilityShape, tflite.getOutputTensor(probabilityTensorIndex).dataType());

        // 创建图像处理器
        imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.BILINEAR))
                //.add(new NormalizeOp(0.0f, 255.0f))  // 根据模型需求调整
                .build();
    }

    public String classify(Bitmap bitmap) {
        // 预处理图像

        inputImageBuffer.load(bitmap);
        inputImageBuffer = imageProcessor.process(inputImageBuffer);

        // 运行推理
        tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());

        // 获取结果
        Map<String, Float> labeledProbability = new TensorLabel(labels, outputProbabilityBuffer)
                .getMapWithFloatValue();


        // 返回最高概率的标签
        return Collections.max(labeledProbability.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public void close() {
        tflite.close();
    }
}