package org.bgi.flexlab.zhangyong.vcfptvcount;

import htsjdk.variant.variantcontext.Genotype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyong on 2018/9/4.
 * statistics index
 */
public class VariantStatistics {
    public enum  VariantType{
        PTV, MISSENSE, SYNONYMOUS
    }

//    private String sampleName;
//    private String genename;
    private VariantType variantType = null;
    private int hetNum;
    private int homNum;
    private int snpNum;
    private int insNum;
    private int delNum;
    private int indelNum;
    private int singletonNum;
    private int[] acNums;
    private int[] afNums;
    private Region acRegions = null;
    private Region afRegions = null;

    public VariantStatistics(Region regions) {
        if(regions != null) {
            if(regions.isAC()) {
                this.acRegions = regions;
                acNums = new int[acRegions.size()];
            } else  {
                this.afRegions = regions;
                afNums = new int[acRegions.size()];
            }
        }
    }

    public void countGenotype( Genotype genotype, boolean isSNP, boolean isDel, boolean isIns,
                               int alleleNumber, double alleleFrequency, boolean isSingleton) {
        if(genotype.isHomVar())
            homNum++;
        if(genotype.isHet()) {
            hetNum++;
            if(isSingleton)
                singletonNum++;
        }
        if((genotype.isHomVar() || genotype.isHet())) {
            if(isSNP)
                snpNum++;
            if(isDel) {
                delNum++;
                indelNum++;
            }
            if(isIns) {
                insNum++;
                indelNum++;
            }
            if(acRegions != null) {
                int index = acRegions.getRegionIndex(alleleNumber);
                acNums[index]++;
            }
            if(afRegions != null) {
                int index = afRegions.getRegionIndex(alleleFrequency);
                afNums[index]++;
            }
        }
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(hetNum);
        sb.append("\t");
        sb.append(homNum);
        sb.append("\t");
        sb.append(snpNum);
        sb.append("\t");
        sb.append(insNum);
        sb.append("\t");
        sb.append(delNum);
        sb.append("\t");
        sb.append(indelNum);
        sb.append("\t");
        sb.append(singletonNum);

        if(acNums != null & acNums.length > 0) {
            for (int acNum : acNums) {
                sb.append("\t");
                sb.append(acNum);
            }
        }
        if(afNums != null & afNums.length > 0) {
            for (int afNum : afNums) {
                sb.append("\t");
                sb.append(afNum);
            }
        }

        return sb.toString();
    }
}
