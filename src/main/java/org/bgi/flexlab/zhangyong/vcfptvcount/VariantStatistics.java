package org.bgi.flexlab.zhangyong.vcfptvcount;

import htsjdk.variant.variantcontext.Genotype;

import java.util.ArrayList;

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
    private ArrayList<Integer> acNums;
    private ArrayList<Integer> afNums;

    public void countGenotype( Genotype genotype, boolean isSNP, boolean isDel, boolean isIns) {
        if(genotype.isHomVar())
            homNum++;
        if(genotype.isHet())
            hetNum++;
        if(isSNP & (genotype.isHomVar() || genotype.isHet()))
            snpNum++;
        if(isDel & (genotype.isHomVar() || genotype.isHet()))
            delNum++;
        if(isIns & (genotype.isHomVar() || genotype.isHet()))
            insNum++;
    }

    public void countACAF() {

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

        if(acNums != null & acNums.size() > 0) {
            for (int acNum : acNums) {
                sb.append("\t");
                sb.append(acNum);
            }
        }
        if(afNums != null & afNums.size() > 0) {
            for (int afNum : afNums) {
                sb.append("\t");
                sb.append(afNum);
            }
        }

        return sb.toString();
    }

/*
    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getGenename() {
        return genename;
    }

    public void setGenename(String genename) {
        this.genename = genename;
    }
*/
    public VariantType getVariantType() {
        return variantType;
    }

    public void setVariantType(VariantType variantType) {
        this.variantType = variantType;
    }

    public int getHetNum() {
        return hetNum;
    }

    public void setHetNum(int hetNum) {
        this.hetNum = hetNum;
    }

    public void addHetNum(int hetNum) {
        this.hetNum += hetNum;
    }

    public int getHomNum() {
        return homNum;
    }

    public void setHomNum(int homNum) {
        this.homNum = homNum;
    }

    public void addHomNum(int homNum) {
        this.homNum += homNum;
    }

    public int getSnpNum() {
        return snpNum;
    }

    public void setSnpNum(int snpNum) {
        this.snpNum = snpNum;
    }

    public void addSnpNum(int snpNum) {
        this.snpNum += snpNum;
    }

    public int getInsNum() {
        return insNum;
    }

    public void setInsNum(int insNum) {
        this.insNum = insNum;
    }

    public void addInsNum(int insNum) {
        this.insNum += insNum;
    }

    public int getDelNum() {
        return delNum;
    }

    public void setDelNum(int delNum) {
        this.delNum = delNum;
    }

    public void addDelNum(int delNum) {
        this.delNum += delNum;
    }

    public int getIndelNum() {
        return indelNum;
    }

    public void setIndelNum(int indelNum) {
        this.indelNum = indelNum;
    }

    public void addIndelNum(int indelNum) {
        this.indelNum += indelNum;
    }

    public int getSingletonNum() {
        return singletonNum;
    }

    public void setSingletonNum(int singletonNum) {
        this.singletonNum = singletonNum;
    }

    public void addSingletonNum(int singletonNum) {
        this.singletonNum += singletonNum;
    }

    public ArrayList<Integer> getAcNums() {
        return acNums;
    }

    public void setAcNums(ArrayList<Integer> acNums) {
        this.acNums = acNums;
    }

    public void addAcNums(int num, int index) {
        acNums.set(index, acNums.get(index) + num);
    }

    public ArrayList<Integer> getAfNums() {
        return afNums;
    }

    public void setAfNums(ArrayList<Integer> afNums) {
        this.afNums = afNums;
    }

    public void addAfNums(int num, int index) {
        afNums.set(index, afNums.get(index) + num);
    }
}
