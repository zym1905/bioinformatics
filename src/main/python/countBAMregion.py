import sys
import re

BaseIndex = {'A': 0, 'T': 1, 'C': 2, 'G': 3}


def main(pileupfile):
    if len(sys.argv) != 2:
        print("please input the mpipeup file from samtools")
        sys.exit(1)

    pipupfile = open(pileupfile, 'r')
    lines = pipupfile.readlines()
    for i in range(0, len(lines)):
        line = lines[i].rstrip("\n")
        print(line)
        splits = line.split("\t")
        basecount = countpileup(splits)
        print("%s\t%s\t%s\t%s\t%d\t%d\t%d\t%d]\t" % (
            splits[0], splits[1], splits[2], splits[3], basecount[0], basecount[1], basecount[2],
            basecount[3]))

    return 0


def countpileup(splits):
    depthrecords = [0, 0, 0, 0]

    bases = splits[4]
    for i in range(0, len(bases)):
        upperBase = bases[i].upper()

        #处理是否添加reference
        if splits[2] != 'N':
            if bases[i] == '.' or bases[i] == ',':
                upperBase = splits[2].upper()

        # 处理正常碱基
        if BaseIndex.__contains__(upperBase):
            index = BaseIndex.get(upperBase)
            depthrecords[index] += 1
        # 处理Insert 或者 Delete，直接跳过(需要修正bug：目前这个支持10以内的跳跃，还是需要学习正则来支持更大的)
        if upperBase == '-' or upperBase == '+':
            i += 1
            i += int(bases[i]) + 1

    return depthrecords


if __name__ == "__main__":
    sys.exit(main(sys.argv[1]))
