package org.bgi.flexlab.zhangyong.allelesharing;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by zhangyong on 2017/5/28.
 * for allele share count.
 * zhangyong2@genomics.cn
 */
public class AlleleShare {

    /**
     * bit 1 count for a byte for quick calculation
     */
    public static byte[] bitCountCache = new byte[256];

    /**
     * allele share count cache, the size is 256 * 256
     */
    public static byte[][] alleleCountCache = new byte[256][256];

    static {
        //calculate the bit count cache
        for(int i = 0; i < 256; i++) {
            bitCountCache[i] = count1bits(i);
        }

        //calculate the allele count cache
        for(int i = 0; i < 256; i++) {
            for(int j = 0; j < 256; j++) {
                alleleCountCache[i][j] = bitCountCache[(i&j)];
            }
        }
    }

    /**
     * count bit 1 number
     * @param val input value
     * @return bit 1 count
     */
    public static byte count1bits(int val) {
        int count;

        for (count = 0; val != 0; count++) {
            val &= (val - 1);
        }

        return (byte) count;
    }


    /**
     * total sample number in vcf
     */
    private int sampleNumber;

    /**
     * total variant number in vcf
     */
    private int positionNumber;

    /**
     * multi sample genotype matrix
     */
    private byte[][] genotypes;

    /**
     *
     */
    private List<String> samples;

    /**
     * results: sample1-sample2 totalAlleleNumber   totalShareAlleleNumber
     */
    //private Map<String, int[]> results;

    /**
     * constructor
     * @param sampleNumber total sample number
     * @param positionNumber total variant number
     */
    public AlleleShare(int sampleNumber, int positionNumber) {
        this.sampleNumber = sampleNumber;
        this.positionNumber = positionNumber;
        genotypes = new byte[sampleNumber][positionNumber];
        samples = new ArrayList<>();
        //results = new HashMap<>();
    }

    /**
     * load vcf and convert genotye into byte
     * @param vcfFile vcf file path
     */
    public void loadVCF(String vcfFile) {
        VCFFileReader reader = new VCFFileReader(new File(vcfFile), false);
        VCFHeader header = reader.getFileHeader();
        samples = header.getGenotypeSamples();

        CloseableIterator<VariantContext> variantContexts =  reader.iterator();
        int variantIndex = 0;
        while(variantContexts.hasNext()) {
            VariantContext variantContext = variantContexts.next();
            GenotypesContext genotypes = variantContext.getGenotypes();
            Iterator<Genotype> genotypeIterator=  genotypes.iterator();

            int sampleIndex = 0;
            while(genotypeIterator.hasNext()) {
                Genotype genotype = genotypeIterator.next();
                GenotypeType type = genotype.getType();

                byte genotypeVal = 0;
                switch (type) {
                    case HET:
                        genotypeVal = (1 << 4) | 2;
                        break;
                    case HOM_REF:
                        genotypeVal = (1 << 4) | 1;
                        break;
                    case HOM_VAR:
                        genotypeVal = (2 << 4) | 2;
                        break;
                }
                //System.err.println(type.toString() + "\t" + genotypeVal);
                this.genotypes[sampleIndex][variantIndex] = genotypeVal;

                sampleIndex++;
            }

            variantIndex++;
        }

        reader.close();
    }

    /**
     * count share allele number
     */
    public void countAlleleShare(String filePath) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath);
        StringBuilder stringBuilder = new StringBuilder();

        for (int sampleIndex1 = 0; sampleIndex1 < sampleNumber; sampleIndex1 ++) {
            for (int sampleIndex2 = sampleIndex1 + 1; sampleIndex2 < sampleNumber; sampleIndex2++) {
                int totalAlleleNumber = 0;
                int shareAlleleNumber = 0;
                for(int variantIndex = 0; variantIndex < positionNumber; variantIndex++) {
                    byte genotype1 = genotypes[sampleIndex1][variantIndex];
                    byte genotype2 = genotypes[sampleIndex2][variantIndex];

                    //System.err.println(genotype1 + "\t" + genotype2);

                    if(genotype1 == 0 || genotype2 == 0)
                        continue;

                    totalAlleleNumber += 2;
                    shareAlleleNumber += alleleCountCache[genotype1][genotype2];
                }
                stringBuilder.setLength(0);
                stringBuilder.append(samples.get(sampleIndex1));
                stringBuilder.append("\t");
                stringBuilder.append(samples.get(sampleIndex2));
                stringBuilder.append("\t");
                stringBuilder.append(shareAlleleNumber);
                stringBuilder.append("\t");
                stringBuilder.append(totalAlleleNumber);
                stringBuilder.append("\t");
                stringBuilder.append(shareAlleleNumber/(double)totalAlleleNumber);
                stringBuilder.append("\n");

                fileWriter.write(stringBuilder.toString());
            }
        }

        fileWriter.close();
    }

}
