package org.bgi.flexlab.zhangyong;

import org.bgi.flexlab.zhangyong.allelesharing.AlleleShare;

import java.io.IOException;

/**
 * Created by zhangyong on 2017/5/28.
 */
public class Main {
    public static void main(String[] args) throws IOException {

        if(args.length < 4) {
            System.out.println("java -jar program.jar sampleNumber variantNumber inputVcf outResultFile.");
        }
        int sampleNumber = Integer.parseInt(args[0]);
        int variantNumber = Integer.parseInt(args[1]);
        String inputPath = args[2];
        String outputPath = args[3];

        AlleleShare alleleShare = new AlleleShare(sampleNumber, variantNumber);

        alleleShare.loadVCF(inputPath);
        alleleShare.countAlleleShare(outputPath);
    }
}
