package org.bgi.flexlab.zhangyong.vcfstatistic;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by zhangyong on 2018/3/14.
 */
public class VCFStatistic {
    private Map<String, GenotypeIndex> sampleVCFCount = new HashMap<>();

    private Map<String, GenotypeIndex> variantVCFCount = new HashMap<>();

    private VCFFileReader reader;

    private FileWriter outputSampleFileWriter;

    private FileWriter outputVariantFileWriter;

    public VCFStatistic(String vcfFile, String outputSampleFile, String outputVariantFile) throws IOException {
        reader = new VCFFileReader(new File(vcfFile), false);
        VCFHeader header = reader.getFileHeader();
        List<String> samples = header.getGenotypeSamples();

        outputSampleFileWriter = new FileWriter(outputSampleFile);
        outputVariantFileWriter = new FileWriter(outputVariantFile);

        for(String sample : samples) {
            GenotypeIndex genotypeIndex = new GenotypeIndex();
            sampleVCFCount.put(sample, genotypeIndex);
        }
    }

    public void countVCF() throws IOException {
        CloseableIterator<VariantContext> variantContexts =  reader.iterator();

        while(variantContexts.hasNext()) {
            VariantContext variantContext = variantContexts.next();
            countGenotypes(variantContext);
        }

        for(String sample : sampleVCFCount.keySet()) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(sample);
            stringBuffer.append("\t");
            stringBuffer.append(sampleVCFCount.get(sample).toString());
            stringBuffer.append("\n");

            outputSampleFileWriter.write(stringBuffer.toString());
        }

        for(String var : variantVCFCount.keySet()) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(var);
            stringBuffer.append("\t");
            stringBuffer.append(variantVCFCount.get(var).toString());
            stringBuffer.append("\n");

            outputVariantFileWriter.write(stringBuffer.toString());
        }

        reader.close();
        outputSampleFileWriter.close();
        outputVariantFileWriter.close();
    }

    public void countGenotypes(VariantContext variantContext) {
        GenotypesContext genotypes = variantContext.getGenotypes();
        Iterator<Genotype> genotypeIterator=  genotypes.iterator();
        GenotypeIndex variantGenotypeIndex = new GenotypeIndex();

        if(!variantContext.isBiallelic()) return;

        //遍历genotypes
        while(genotypeIterator.hasNext()) {
            Genotype genotype = genotypeIterator.next();

            //统计基本基因型信息
            if(genotype.isHomRef()) {
                sampleVCFCount.get(genotype.getSampleName()).addHomeRef();
                variantGenotypeIndex.addHomeRef();
            } else if(genotype.isHet()) {
                sampleVCFCount.get(genotype.getSampleName()).addHet();
                variantGenotypeIndex.addHet();
            } else if(genotype.isHomVar()) {
                sampleVCFCount.get(genotype.getSampleName()).addHomVar();
                variantGenotypeIndex.addHomVar();
            } else if(genotype.isNoCall()) {
                sampleVCFCount.get(genotype.getSampleName()).addNonCall();
                variantGenotypeIndex.addNonCall();
            }
            if(genotype.isHetNonRef()) {
                sampleVCFCount.get(genotype.getSampleName()).addHetNonRef();
                variantGenotypeIndex.addHetNonRef();
            }
            if(genotype.isMixed()) {
                sampleVCFCount.get(genotype.getSampleName()).addMixed();
                variantGenotypeIndex.addMixed();
            }

            //统计ti tv
            List<Allele> alleles =  genotype.getAlleles();
            if(genotype.isNoCall() || genotype.isHomRef() || alleles == null || alleles.size() != 2) {
                continue;
            }
            char ref = variantContext.getReference().getDisplayString().charAt(0);;
            char alt = '\0';
            for(Allele allele : alleles) {
                if(allele.isReference()) continue;
                if(allele.isNoCall()) continue;
                if(allele.isSymbolic()) continue;
                String display = allele.getDisplayString().toUpperCase();
                char c = display.charAt(0);
                if(!isATGC(c)) continue;
                alt = c;
            }

            if(alt == '\0') continue;

            if((ref == 'A' && alt == 'G') || (ref == 'G' && alt == 'A')
                    || (ref == 'C' && alt == 'T') || (ref == 'T' && alt == 'C')) {
                sampleVCFCount.get(genotype.getSampleName()).addTi();
                variantGenotypeIndex.addTi();
            } else {
                sampleVCFCount.get(genotype.getSampleName()).addTv();
                variantGenotypeIndex.addTv();
            }
        }

        variantVCFCount.put(variantContext.getContig() + "\t" + variantContext.getStart(), variantGenotypeIndex);
    }

    private boolean isATGC(char c) {
        return c=='A' || c=='T' || c=='G' || c=='C';
    }

    public class GenotypeIndex {
        private int genotypeHomRefCount = 0;
        private int genotypeHetCount = 0;
        private int genotypeHomVarCount = 0;
        private int genotypeHetNonRef = 0;
        private int genotypeNonCallCount = 0;
        private int genotypeMixedCount = 0;
        private int genotypeTiCount = 0;
        private int genotypeTvCount = 0;


        public double getTiTv() {
            return genotypeTiCount / (double) genotypeTvCount;
        }

        public void addHomeRef() {
            genotypeHomRefCount++;
        }

        public void addHet() {
            genotypeHetCount++;
        }

        public void addHomVar() {
            genotypeHomVarCount++;
        }

        public void addNonCall() {
            genotypeNonCallCount++;
        }

        public void addHetNonRef() {
            genotypeHetNonRef++;
        }

        public void addMixed() {
            genotypeMixedCount++;
        }

        public void addTi() {
            genotypeTiCount++;
        }

        public void addTv() {
            genotypeTvCount++;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(genotypeHomRefCount);
            sb.append("\t");
            sb.append(genotypeHetCount);
            sb.append("\t");
            sb.append(genotypeHomVarCount);
            sb.append("\t");
            sb.append(genotypeHetNonRef);
            sb.append("\t");
            sb.append(genotypeNonCallCount);
            sb.append("\t");
            sb.append(genotypeMixedCount);
            sb.append("\t");
            sb.append(genotypeTiCount);
            sb.append("\t");
            sb.append(genotypeTvCount);

            return sb.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 3) {
            System.out.println("java -jar program.jar inputVcf outSampleResultFile outVariantResultFile.");
        }

        VCFStatistic vcfStatistic = new VCFStatistic(args[0], args[1], args[2]);
        vcfStatistic.countVCF();
    }
}
