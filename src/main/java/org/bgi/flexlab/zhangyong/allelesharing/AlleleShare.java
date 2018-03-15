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

    public static int alleleBitSize = 2;

    public static int genotypeNumPeRound = 6;

    public static int countCacheSize = (int) Math.pow(2, alleleBitSize * genotypeNumPeRound);

    public static int genotypeShift;

    /**
     * allele share count cache array
     */
    public static byte[][][] alleleCountCache = new byte[countCacheSize][countCacheSize][2];

    static {
        int tmp = 1;
        for(int i = 1; i < alleleBitSize; i++) {
            tmp = (tmp << 1) + 1;
        }
        genotypeShift = tmp;

        //calculate the allele count cache
        for(int i = 0; i < countCacheSize; i++) {
            for(int j = 0; j < countCacheSize; j++) {
                byte totalAlleleCount = 0;
                byte shareAlleleCount = 0;

                for(int k = 0; k < genotypeNumPeRound; k++) {
                    int g1 = (i >> (alleleBitSize * k)) & genotypeShift;
                    int g2 = (j >> (alleleBitSize * k)) & genotypeShift;

                    if(g1 == 0 || g2 == 0)
                        continue;

                    totalAlleleCount += 2;
                    shareAlleleCount += countShareAllele(g1, g2);
                }
                //System.err.println(i + "\t" + j + "\t" + totalAlleleCount + "\t" + shareAlleleCount);
                alleleCountCache[i][j][0] = totalAlleleCount;
                alleleCountCache[i][j][1] = shareAlleleCount;
            }
        }
    }

    /**
     * count share allele number
     * @param g1 genotype 1
     * @param g2 genotype 2
     * @return share allele count
     */
    public static byte countShareAllele(int g1, int g2) {
        if(g1 == g2)
            return 2;
        else if(g1 == 1 || g2 == 1)
            return 1;

        return 0;
    }


    /**
     * total sample number in vcf
     */
    private int sampleNumber;

    /**
     * total variant number in vcf
     */
    private int variantNumber;

    /**
     * multi sample genotype matrix
     */
    private short[][] genotypes;

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
     * @param variantNumber total variant number
     */
    public AlleleShare(int sampleNumber, int variantNumber) {
        this.sampleNumber = sampleNumber;
        this.variantNumber = variantNumber;
        genotypes = new short[sampleNumber][variantNumber / genotypeNumPeRound + 1];
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
        int variantCount = 0;
        int variantIndex;
        while(variantContexts.hasNext()) {
            VariantContext variantContext = variantContexts.next();
            GenotypesContext genotypes = variantContext.getGenotypes();
            Iterator<Genotype> genotypeIterator=  genotypes.iterator();
            variantIndex = variantCount / genotypeNumPeRound;

            int sampleIndex = 0;
            while(genotypeIterator.hasNext()) {
                Genotype genotype = genotypeIterator.next();
                GenotypeType type = genotype.getType();

                byte genotypeVal = 0;
                switch (type) {
                    case HET:
                        genotypeVal = 1;
                        break;
                    case HOM_REF:
                        genotypeVal = 2;
                        break;
                    case HOM_VAR:
                        genotypeVal = 3;
                        break;
                }
                //System.err.println(type.toString() + "\t" + genotypeVal);

                int originalGenotype = this.genotypes[sampleIndex][variantIndex];
                if(variantCount % genotypeNumPeRound != 0)
                    this.genotypes[sampleIndex][variantIndex] = (short) (originalGenotype << alleleBitSize | genotypeVal);
                else {
                    this.genotypes[sampleIndex][variantIndex] = genotypeVal;
                }

                sampleIndex++;
            }

            variantCount++;
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
                for(int variantIndex = 0; variantIndex < variantNumber / genotypeNumPeRound + 1; variantIndex++) {
                    short genotype1 = genotypes[sampleIndex1][variantIndex];
                    short genotype2 = genotypes[sampleIndex2][variantIndex];

                   //System.err.println(genotype1 + "\t" + genotype2);

                    totalAlleleNumber += alleleCountCache[genotype1][genotype2][0];
                    shareAlleleNumber += alleleCountCache[genotype1][genotype2][1];
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
