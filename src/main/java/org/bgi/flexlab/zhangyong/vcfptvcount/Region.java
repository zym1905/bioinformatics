package org.bgi.flexlab.zhangyong.vcfptvcount;

import java.util.List;

/**
 * Created by zhangyong on 2018/9/12.
 */
public class Region {
    private List<double[]> regions;

    public Region(String regionString) {
        String[] regionSplit = regionString.split(",");
        double[] regionNumber = new double[regionSplit.length];
        int i = 0;
        for(String rs : regionSplit) {
            regionNumber[i++] = Double.valueOf(rs);
        }

        for(int j = 0; j < regionNumber.length - 1; j++) {
            double[] region = new double[2];
            region[0] = regionNumber[j];
            region[1] = regionNumber[j+1];
            regions.add(region);
        }
    }

    public int getRegionIndex(double number) {
        int index = -1;
        for(double[] region : regions) {
            index++;
            if (number >= region[0] && number < region[1]);
                break;
        }

        return index;
    }

    public int size() {
        return regions.size();
    }

    public boolean isAC() {
        if(regions.get(0)[0] >= 1)
            return true;
        return false;
    }
}
