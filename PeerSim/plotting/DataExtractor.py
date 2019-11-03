
# control.graphPL: 0.999 1.8999099909991

import re
import sys

if __name__ == "__main__":
    f  = open(sys.argv[1], "r")
    lines = f.readlines()
    
    layout = sys.argv[2]
    c = sys.argv[3]

    clustering_file = "clustering_" + layout + "_c" + c 
    path_len_file = "path_len_" + layout + "_c" + c
    in_deg_file = "in_deg_" + layout + "_c" + c

    c_file = open(clustering_file, "w")
    p_file = open(path_len_file, "w")
    i_file = open(in_deg_file, "w")

    cycle = 0
    for line in lines:
        if re.match("control\.graphPL: (.*) (.*)", line):
            match = re.search("control\.graphPL: (.*) (.*)", line)
            coeff = match.group(1)
            average = match.group(2)

            c_file.write(str(cycle) + " " + coeff + '\n')
            p_file.write(str(cycle) + " " + average + '\n')

            cycle = cycle + 1
        elif re.match("([0-9]+) ([0-9]+)", line):
            i_file.write(line + '\n')

    c_file.close()
    p_file.close()
    i_file.close()



