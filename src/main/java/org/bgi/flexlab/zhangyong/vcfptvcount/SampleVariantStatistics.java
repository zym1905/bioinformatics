package org.bgi.flexlab.zhangyong.vcfptvcount;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyong on 2018/9/9.
 */
public class SampleVariantStatistics {

    public Map<String, Map<VariantStatistics.VariantType, VariantStatistics>> getSampleVariantStatistics() {
        return sampleVariantStatistics;
    }

    Map<String, Map<VariantStatistics.VariantType, VariantStatistics>> sampleVariantStatistics = new HashMap<>();

    public SampleVariantStatistics(List<String> samples) {
        for(String sample :samples) {
            Map<VariantStatistics.VariantType, VariantStatistics> variantStatistics = new HashMap<>();
            sampleVariantStatistics.put(sample, variantStatistics);
        }
    }

    public void countGenotypes(GenotypesContext genotypes, boolean isSNP, boolean isDel, boolean isIns,
                               VariantStatistics.VariantType variantType) {
        int alleleNumber = 0;
        int altAlleleNumber = 0;
        boolean isSingleton = false;
        double alleleFrequency = 0;
        for (Genotype genotype : genotypes) {
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

        for (Genotype genotype : genotypes) {
            if (sampleVariantStatistics.get(genotype.getSampleName()).get(variantType) == null) {
                VariantStatistics variantStatistics = new VariantStatistics();
                sampleVariantStatistics.get(genotype.getSampleName()).put(variantType, variantStatistics);
            }
            sampleVariantStatistics.get(genotype.getSampleName()).get(variantType).countGenotype(genotype, isSNP, isDel, isIns);
        }

        }
}
