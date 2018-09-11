package org.bgi.flexlab.zhangyong.vcfptvcount;

import htsjdk.samtools.util.CloseableIterator;
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

    public VCFCount(String vcfFile, String outputDir) throws IOException {
        reader = new VCFFileReader(new File(vcfFile), false);
        this.outputDir = outputDir;

        VCFHeader header = reader.getFileHeader();
        samples = header.getGenotypeSamples();

        geneVariantStatistics = new HashMap<>();
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

            if(!variantContext.isBiallelic() || geneName == null || geneName == "")
                continue;
            VariantStatistics.VariantType variantType;
            if (consequence.equals("missense_variant") || consequence.equals("non_conservative_missense_variant")
                    || consequence.equals("conservative_missense_variant")) {
                variantType = VariantStatistics.VariantType.MISSENSE;
            } else
            if(consequence.equals("synonymous_variant") || consequence.equals("start_retained_variant")
                    || consequence.equals("stop_retained_variant")) {
                variantType = VariantStatistics.VariantType.SYNONYMOUS;
            } else
            if(lof.equals("HC")) {
                variantType = VariantStatistics.VariantType.PTV;
            } else
                continue;

            if(!geneVariantStatistics.containsKey(geneName)) {
                SampleVariantStatistics sampleVariantStatistics = new SampleVariantStatistics(samples);
                geneVariantStatistics.put(geneName, sampleVariantStatistics);
            }

            GenotypesContext genotypes = variantContext.getGenotypes();
            geneVariantStatistics.get(geneName).countGenotypes(genotypes, variantContext.isSNP(),
                    variantContext.isSimpleDeletion(), variantContext.isSimpleInsertion(), variantType);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("java -jar program.jar inputVcf outputDir AF1-AF2.");
        }

        VCFCount vcfStatistic = new VCFCount(args[0], args[1]);
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
