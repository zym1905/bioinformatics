package org.bgi.flexlab.zhangyong.vcfptvcount;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyong on 2018/9/4.
 * count statistics of ptv, missense and synonymous
 */
public class VCFCount {

    private VCFFileReader reader;

    private String outputDir;

    private List<String> samples;

    private Map<String, SampleVariantStatistics> geneVariantStatistics;

    private Region regions = null;

    public VCFCount(String vcfFile, String outputDir, String regionString) throws IOException {
        reader = new VCFFileReader(new File(vcfFile), false);
        this.outputDir = outputDir;

        VCFHeader header = reader.getFileHeader();
        samples = header.getGenotypeSamples();

        geneVariantStatistics = new HashMap<>();

        regions = new Region(regionString);

    }

    public void countVCF() throws IOException {

        CloseableIterator<VariantContext> variantContexts =  reader.iterator();
        while(variantContexts.hasNext()) {
            VariantContext variantContext = variantContexts.next();

            String csq = (String) variantContext.getAttribute("CSQ");
            String csq1 = csq.split(",")[0];
            String[] csqSplit = csq1.split("|");
            String consequence = csqSplit[1];
            String geneName = csqSplit[3];
            String lof = csqSplit[75];

            if(geneName == null || geneName == "")
                continue;

            VariantStatistics.VariantType variantType;
            if(lof.equals("HC")) {
                variantType = VariantStatistics.VariantType.PTV;
            } else
            if (consequence.equals("missense_variant") || consequence.equals("non_conservative_missense_variant")
                    || consequence.equals("conservative_missense_variant")) {
                variantType = VariantStatistics.VariantType.MISSENSE;
            } else
            if(consequence.equals("synonymous_variant") || consequence.equals("start_retained_variant")
                    || consequence.equals("stop_retained_variant")) {
                variantType = VariantStatistics.VariantType.SYNONYMOUS;
            } else
                continue;

            boolean isSNP = false;
            boolean isDel = false;
            boolean isIns = false;
            int alleleNumber = 0;
            int altAlleleNumber = 0;
            boolean isSingleton = false;
            double alleleFrequency = 0;

            if(variantContext.isBiallelic()) {
                isSNP = variantContext.isSNP();
                isDel = variantContext.isSimpleDeletion();
                isIns = variantContext.isSimpleInsertion();
                for (Genotype genotype : variantContext.getGenotypes()) {
                    //cal af or ac info
                    if(genotype.isNoCall())
                        continue;
                    alleleNumber += 2;
                    if(genotype.isHet())
                        altAlleleNumber++;
                    if(genotype.isHomVar())
                        altAlleleNumber += 2;
                }
                alleleFrequency = altAlleleNumber / (double) alleleNumber;
                if(altAlleleNumber == 1)
                    isSingleton = true;
            } else { //由于多碱基的alt，可能会被过滤掉一个导致重新变为二碱基，所以判断二碱基需要重新count
                int[] altAlleleCount = new int[variantContext.getAlternateAlleles().size()];
                List<Allele> variantAlleles = variantContext.getAlternateAlleles();
                for (Genotype genotype : variantContext.getGenotypes()) {
                    if(genotype.isNoCall())
                        continue;
                    alleleNumber += 2;

                    Allele sampleAltAllele = genotype.getAllele(1);
                    if(genotype.isHet())
                        altAlleleCount[variantAlleles.lastIndexOf(sampleAltAllele)]++;
                    if(genotype.isHomVar())
                        altAlleleCount[variantAlleles.lastIndexOf(sampleAltAllele)] += 2;
                }

                int noneZeroCount = 0;
                int noneZeroIndex = -1;
                for(int count = 0; count < altAlleleCount.length; count++) {
                    if(altAlleleCount[count] > 0) {
                        noneZeroCount++;
                        noneZeroIndex = count;
                    }
                }
                if(noneZeroCount != 1)
                    continue;

                Allele refAllele = variantContext.getReference();
                Allele altAllele = variantAlleles.get(noneZeroIndex);
                if(refAllele.length() == 1 && altAllele.length() == 1)
                    isSNP =true;
                if(refAllele.length() > 0           // ref is not null or symbolic
                        && altAllele.length() > 0    // alt is not null or symbolic
                        && refAllele.getBases()[0] == altAllele.getBases()[0] ) {
                    if(refAllele.length() > altAllele.length()) {
                        isDel = true;
                    }
                    if(refAllele.length() < altAllele.length()) {
                        isIns = true;
                    }
                }

                altAlleleNumber = altAlleleCount[noneZeroIndex];
                alleleFrequency = altAlleleNumber / (double) alleleNumber;
                if(altAlleleNumber == 1)
                    isSingleton = true;
            }

            if(!geneVariantStatistics.containsKey(geneName)) {
                SampleVariantStatistics sampleVariantStatistics = new SampleVariantStatistics(samples);
                geneVariantStatistics.put(geneName, sampleVariantStatistics);
            }

            GenotypesContext genotypes = variantContext.getGenotypes();
            geneVariantStatistics.get(geneName).countGenotypes(genotypes, isSNP, isDel, isIns, variantType,
                    altAlleleNumber, alleleFrequency, isSingleton, regions);
        }
    }

    public static void main(String[] args) throws IOException {
        VCFCount vcfStatistic = null;
        if(args.length == 2)
            vcfStatistic = new VCFCount(args[0], args[1], null);
        if(args.length == 3)
            vcfStatistic = new VCFCount(args[0], args[1], args[2]);
        if (vcfStatistic == null) {
            System.out.println("java -jar program.jar inputVcf outputDir AF1,AF2/AC1,AC2.");
            System.exit(1);
        }
        vcfStatistic.countVCF();

        //output
        for (String geneName : vcfStatistic.geneVariantStatistics.keySet()) {
            FileWriter genePTVFileWriter = new FileWriter(args[1] + "/" + geneName + "_PTV.txt");
            FileWriter geneMissenseFileWriter = new FileWriter(args[1] + "/" + geneName + "_Missense.txt");
            FileWriter geneSynoFileWriter = new FileWriter(args[1] + "/" + geneName + "_Synonymous.txt");
            for (String sampleName : vcfStatistic.geneVariantStatistics.get(geneName).getSampleVariantStatistics().keySet()) {
                for (VariantStatistics.VariantType variantType : vcfStatistic.geneVariantStatistics.get(geneName).getSampleVariantStatistics().get(sampleName).keySet()) {
                    VariantStatistics variantStatistics = vcfStatistic.geneVariantStatistics.get(geneName).getSampleVariantStatistics().get(sampleName).get(variantType);
                    if(variantType == VariantStatistics.VariantType.PTV) {
                        genePTVFileWriter.write(variantStatistics.toString());
                    }
                    if(variantType == VariantStatistics.VariantType.MISSENSE) {
                        geneMissenseFileWriter.write(variantStatistics.toString());
                    }
                    if(variantType == VariantStatistics.VariantType.SYNONYMOUS) {
                        geneSynoFileWriter.write(variantStatistics.toString());
                    }
                }
            }
            genePTVFileWriter.close();
            geneMissenseFileWriter.close();
            geneSynoFileWriter.close();
        }
    }
}
