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
    private int[] acNums;
    private int[] afNums;
    private Region acRegions = null;
    private Region afRegions = null;

    public VariantStatistics(Region acRegions, Region afRegions) {
        if(acRegions != null && acRegions.isAC()) {
            this.acRegions = acRegions;
            acNums = new int[acRegions.size()];
        }
        if(afRegions != null && !afRegions.isAC()) {
            this.afRegions = afRegions;
            afNums = new int[afRegions.size()];
        }
    }

    public void countGenotype( Genotype genotype, boolean isSNP, boolean isDel, boolean isIns,
                               int alleleNumber, double alleleFrequency) {
        if(genotype.isHomVar())
            homNum++;
        if(genotype.isHet()) {
            hetNum++;
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
                if(index >= 0)
                    acNums[index]++;
            }
            if(afRegions != null) {
                int index = afRegions.getRegionIndex(alleleFrequency);
                if(index >= 0)
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
        sb.append(indelNum);
        sb.append("\t");
        sb.append(insNum);
        sb.append("\t");
        sb.append(delNum);

        if(acRegions != null) {
            for (int acNum : acNums) {
                sb.append("\t");
                sb.append(acNum);
            }
        }
        if(afRegions != null) {
            for (int afNum : afNums) {
                sb.append("\t");
                sb.append(afNum);
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
