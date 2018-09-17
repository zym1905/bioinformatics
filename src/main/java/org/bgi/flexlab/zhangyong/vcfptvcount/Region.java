package org.bgi.flexlab.zhangyong.vcfptvcount;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangyong on 2018/9/12.
 */
public class Region {
    private List<double[]> regions;

    public Region(String regionString) {
        regions = new ArrayList<>();
        String[] regionSplit = regionString.split(",");
        double[] regionNumber = new double[regionSplit.length];
        int i = 0;
        for(String rs : regionSplit) {
            regionNumber[i++] = Double.valueOf(rs);
        }

        double[] firstRegion = new double[2];
        firstRegion[0] = 0;
        firstRegion[1] = regionNumber[0];
        regions.add(firstRegion);
        System.out.println("add region:>" + firstRegion[0]);

        for(int j = 0; j < regionNumber.length - 1; j++) {
            double[] region = new double[2];
            region[0] = regionNumber[j];
            region[1] = regionNumber[j+1];
            regions.add(region);
            System.out.println("add region:" + region[0] + "-" + region[1]);
        }

        //last region > last number
        double[] lastRegion = new double[2];
        lastRegion[0] = regionNumber[regionNumber.length -1];
        lastRegion[1] = Double.MAX_VALUE;
        regions.add(lastRegion);
        System.out.println("add region:>" + lastRegion[0]);
    }

    public int getRegionIndex(double number) {
        int index;
        for(index = regions.size() - 1; index >= 0;index--) {
            double[] region = regions.get(index);
            if (number > region[0] && number <= region[1]) {
                break;
            }
        }

        return index;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for(int j = 0; j < regions.size() - 1; j++) {
            sb.append("(");
            sb.append(regions.get(j)[0]);
            sb.append("-");
            sb.append(regions.get(j)[1]);
            sb.append("]\t");
        }
        sb.append(">");
        sb.append(regions.get(regions.size() - 1)[0]);

        return sb.toString();
    }

    public int size() {
        return regions.size();
    }

    public boolean isAC() {
        if(regions.get(1)[0] >= 1)
            return true;
        return false;
    }
}
