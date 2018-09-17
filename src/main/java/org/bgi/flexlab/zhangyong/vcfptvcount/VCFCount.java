package org.bgi.flexlab.zhangyong.vcfptvcount;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.io.*;
import java.util.*;

/**
 * Created by zhangyong on 2018/9/4.
 * count statistics of ptv, missense and synonymous
 */
public class VCFCount {

    private VCFFileReader reader;

    private String outputDir;

    private List<String> samples;

    private Map<String, SampleVariantStatistics> geneVariantStatistics;

    private Region acRegions = null;
    private Region afRegions = null;

    private Set<String> geneList = null;

    public VCFCount(String vcfFile, String outputDir, String acRegionString, String afRegionString, String geneListFiles) throws IOException {
        reader = new VCFFileReader(new File(vcfFile), false);
        this.outputDir = outputDir;

        VCFHeader header = reader.getFileHeader();
        samples = header.getSampleNamesInOrder();

        geneVariantStatistics = new HashMap<>();

        if(acRegionString != null) {
            acRegions = new Region(acRegionString);
            if(!acRegions.isAC()) {
                throw new RuntimeException("AC region should be > 1");
            }
        }
        if(afRegionString != null) {
            afRegions = new Region(afRegionString);
            if(afRegions.isAC()) {
                throw new RuntimeException("AF region should be < 1");
            }
        }

        if(geneListFiles != null) {
            geneList = new HashSet<>();

            BufferedReader reader = new BufferedReader(new FileReader(geneListFiles));
            String geneLine;
            while((geneLine = reader.readLine()) != null) {
                geneList.add(geneLine.trim());
            }
        }
    }

    public void countVCF() throws IOException {
        CloseableIterator<VariantContext> variantContexts =  reader.iterator();

        int ptvNumber = 0,missenseNumber = 0,synonymousNumber = 0, filtered = 0;
        while(variantContexts.hasNext()) {
            VariantContext variantContext = variantContexts.next();

            String csq = (String) variantContext.getAttribute("CSQ");
            String csq1 = csq.split(",")[0];
            //System.out.println("csq1:" +csq1);
            String[] csqSplit = csq1.split("\\|");
            String consequence = csqSplit[1];
            String geneName = csqSplit[3];
            String lof = csqSplit[76];

            //System.out.println("geneName:" + geneName + "\tconsequence:" + consequence + "\tlof:" + csqSplit[76]);
            if(geneName == null || geneName == "" || (geneList != null && !geneList.contains(geneName)))
                continue;

            VariantStatistics.VariantType variantType;
            if(lof.equals("HC")) {
                ptvNumber++;
                variantType = VariantStatistics.VariantType.PTV;
            } else
            if (consequence.contains("missense_variant") || consequence.contains("non_conservative_missense_variant")
                    || consequence.contains("conservative_missense_variant")) {
                variantType = VariantStatistics.VariantType.MISSENSE;
                missenseNumber++;
            } else
            if(consequence.contains("synonymous_variant") || consequence.contains("start_retained_variant")
                    || consequence.contains("stop_retained_variant")) {
                variantType = VariantStatistics.VariantType.SYNONYMOUS;
                synonymousNumber++;
            } else {
                filtered++;
                continue;
            }

            boolean isSNP = false;
            boolean isDel = false;
            boolean isIns = false;
            int alleleNumber = 0;
            int altAlleleNumber = 0;
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
            } else {
                System.out.println(variantContext.toStringWithoutGenotypes());
                continue;
            }
            /*else { //由于多碱基的alt，可能会被过滤掉一个导致重新变为二碱基，所以判断二碱基需要重新count
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
            }*/

            if(!geneVariantStatistics.containsKey(geneName)) {
                SampleVariantStatistics sampleVariantStatistics = new SampleVariantStatistics(samples);
                geneVariantStatistics.put(geneName, sampleVariantStatistics);
            }

            GenotypesContext genotypes = variantContext.getGenotypes();
            geneVariantStatistics.get(geneName).countGenotypes(genotypes, isSNP, isDel, isIns, variantType,
                    altAlleleNumber, alleleFrequency, acRegions, afRegions);
        }
        System.out.println("ptvnumber:" + ptvNumber + "\tmissenseNumber:" + missenseNumber + "\tsynonymousNumber:" + synonymousNumber + "\tfiltered:" + filtered);

    }

    public String outputHeader() {
        StringBuffer sb = new StringBuffer();
        sb.append("sampleName\thet\thom\tsnp\tindel\tins\tdel\t");
        if(acRegions != null) {
            sb.append(acRegions.toString());
        }
        sb.append("\t");
        if(afRegions != null) {
            sb.append(afRegions.toString());
        }
        sb.append("\n");

        return sb.toString();
    }

    public void outputResult() throws IOException {
        //output
        for (String geneName : geneVariantStatistics.keySet()) {
            FileWriter genePTVFileWriter = new FileWriter(outputDir + "/" + geneName + "_PTV.txt");
            FileWriter geneMissenseFileWriter = new FileWriter(outputDir + "/" + geneName + "_Missense.txt");
            FileWriter geneSynoFileWriter = new FileWriter(outputDir + "/" + geneName + "_Synonymous.txt");

            String header = outputHeader();
            genePTVFileWriter.write(header);
            geneMissenseFileWriter.write(header);
            geneSynoFileWriter.write(header);

            for (String sampleName : samples) {
                //for (String sampleName : vcfStatistic.geneVariantStatistics.get(geneName).getSampleVariantStatistics().keySet()) {
                for (VariantStatistics.VariantType variantType : geneVariantStatistics.get(geneName).getSampleVariantStatistics().get(sampleName).keySet()) {
                    VariantStatistics variantStatistics = geneVariantStatistics.get(geneName).getSampleVariantStatistics().get(sampleName).get(variantType);
                    if(variantType == VariantStatistics.VariantType.PTV) {
                        genePTVFileWriter.write(sampleName);
                        genePTVFileWriter.write("\t");
                        genePTVFileWriter.write(variantStatistics.toString());
                    }
                    if(variantType == VariantStatistics.VariantType.MISSENSE) {
                        geneMissenseFileWriter.write(sampleName);
                        geneMissenseFileWriter.write("\t");
                        geneMissenseFileWriter.write(variantStatistics.toString());
                    }
                    if(variantType == VariantStatistics.VariantType.SYNONYMOUS) {
                        geneSynoFileWriter.write(sampleName);
                        geneSynoFileWriter.write("\t");
                        geneSynoFileWriter.write(variantStatistics.toString());
                    }
                }
            }
            genePTVFileWriter.close();
            geneMissenseFileWriter.close();
            geneSynoFileWriter.close();
        }
    }

    public static void main(String[] args) throws IOException {
        VCFCount vcfStatistic = null;
        if(args.length == 2)
            vcfStatistic = new VCFCount(args[0], args[1], null, null, null);
        if(args.length == 3)
            vcfStatistic = new VCFCount(args[0], args[1], args[2], null, null);
        if(args.length == 4)
            vcfStatistic = new VCFCount(args[0], args[1], args[2], args[3], null);
        if(args.length == 5)
            vcfStatistic = new VCFCount(args[0], args[1], args[2], args[3], args[4]);

        if (vcfStatistic == null) {
            System.out.println("java -jar program.jar inputVcf   outputDir   AC1,AC2   AF1,AF2   genelist.");
            System.exit(1);
        }
        vcfStatistic.countVCF();

        vcfStatistic.outputResult();
    }
}
