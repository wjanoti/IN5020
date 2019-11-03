#!/bin/bash

python3 DataExtractor.py output_ring_c30.txt ring 30
python3 DataExtractor.py output_ring_c50.txt ring 50
python3 DataExtractor.py output_star_c30.txt star 30
python3 DataExtractor.py output_star_c50.txt star 50
python3 DataExtractor.py output_random_c30.txt random 30
python3 DataExtractor.py output_random_c50.txt random 50


gnuplot plotRingAvg.plt
gnuplot plotStarAvg.plt
gnuplot plotRingCC.plt
gnuplot plotStarCC.plt
gnuplot plotInDegreeStar.plt
gnuplot plotInDegreeRing.plt
