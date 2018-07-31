package org.bgi.flexlab.zhangyong.vcfdpcount;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by zhangyong on 2018/7/25.
 */
public class VCFDPCount {

    private VCFFileReader reader;

    private FileWriter outputDPFileWriter;

    public VCFDPCount(String vcfFile, String outputDPFile) throws IOException {
        reader = new VCFFileReader(new File(vcfFile), false);
        outputDPFileWriter = new FileWriter(outputDPFile);
    }

    private void countVCF() throws IOException {
        CloseableIterator<VariantContext> variantContexts =  reader.iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("CHROM");
        sb.append("\t");
        sb.append("POS");
        sb.append("\t");
        sb.append("newDP");
        sb.append("\t");
        sb.append("newAN");
        sb.append("\t");
        sb.append("newADP");
        sb.append("\n");
        outputDPFileWriter.write(sb.toString());
        while(variantContexts.hasNext()) {
            VariantContext variantContext = variantContexts.next();
            GenotypesContext genotypes = variantContext.getGenotypes();
            Iterator<Genotype> genotypeIterator=  genotypes.iterator();

            int dpSum = 0;
            int anSum = 0;
            int sampleSum = 0;
            //遍历genotypes
            while(genotypeIterator.hasNext()) {
                Genotype genotype = genotypeIterator.next();
                if(genotype.isCalled()) {
                    dpSum += genotype.getDP();
                    anSum += 2;
                    sampleSum++;
                }
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(variantContext.getContig());
            stringBuffer.append("\t");
            stringBuffer.append(variantContext.getStart());
            stringBuffer.append("\t");
            stringBuffer.append(dpSum);
            stringBuffer.append("\t");
            stringBuffer.append(anSum);
            stringBuffer.append("\t");
            stringBuffer.append(dpSum / (double) sampleSum);
            stringBuffer.append("\n");
            outputDPFileWriter.write(stringBuffer.toString());
        }
        outputDPFileWriter.close();
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 2) {
            System.out.println("java -jar program.jar inputVcf outDPResultFile.");
        }

        VCFDPCount vcfDPCount = new VCFDPCount(args[0], args[1]);
        vcfDPCount.countVCF();
    }
}
